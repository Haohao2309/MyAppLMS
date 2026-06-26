package com.example.myapplms.ui.student.learning;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.R;
import com.example.myapplms.data.RetrofitClient;
import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.request.CreateDiscussionRequest;
import com.example.myapplms.data.repository.CourseDetailRepository;
import com.example.myapplms.data.repository.LearningRepository;
import com.example.myapplms.ui.student.course_detail.CourseDetailViewModel;
import com.example.myapplms.ui.student.course_detail.CourseDetailViewModelFactory;
import com.example.myapplms.ui.student.course_detail.adapter.ModuleAdapter;
import com.example.myapplms.ui.student.learning.adapter.DiscussionAdapter;
import com.example.myapplms.utils.Resource;
import com.example.myapplms.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONObject;

public class LearningActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private LearningViewModel viewModel;
    private int courseId;

    private ModuleAdapter menuAdapter;
    private String currentLessonId;
    private ExtendedFloatingActionButton fabDiscussion;
    private ImageView ivMenuToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);

        courseId = getIntent().getIntExtra("COURSE_ID", -1);
        View dl = findViewById(R.id.drawer_layout);
        if (dl instanceof DrawerLayout) {
            drawerLayout = (DrawerLayout) dl;
        } else {
            drawerLayout = null; // Trên Tablet là giao diện phẳng, không có Drawer
        }
        fabDiscussion = findViewById(R.id.fab_discussion);
        ivMenuToggle = findViewById(R.id.iv_menu_toggle);

        SessionManager sessionManager = new SessionManager(this);
        LmsApiService apiService = RetrofitClient.getInstance(sessionManager).create(LmsApiService.class);

        // 🔴 Xử lý Fullscreen khi xoay ngang (Landscape) lúc mới mở màn hình
        handleOrientationChange(getResources().getConfiguration().orientation);

        // 1. Khởi tạo LearningViewModel (Để quản lý điểm, tiến độ, Q&A)
        LearningRepository learningRepo = new LearningRepository(apiService);
        LearningViewModelFactory learningFactory = new LearningViewModelFactory(learningRepo);
        viewModel = new ViewModelProvider(this, learningFactory).get(LearningViewModel.class);

        // 2. Khởi tạo CourseDetailViewModel (Để tải danh sách Sidebar Menu giống hệt CurriculumFragment)
        CourseDetailRepository detailRepo = new CourseDetailRepository(apiService);
        CourseDetailViewModelFactory detailFactory = new CourseDetailViewModelFactory(detailRepo);
        CourseDetailViewModel detailViewModel = new ViewModelProvider(this, detailFactory).get(CourseDetailViewModel.class);

        // 3. Xử lý mở/đóng Sidebar Menu
        if (ivMenuToggle != null && drawerLayout != null) {
            ivMenuToggle.setOnClickListener(v -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    drawerLayout.openDrawer(GravityCompat.END);
                }
            });
        }

        // 🟢 FIX LỖI MẤT MENU: Gọi API lấy Course Content và đổ vào Sidebar
        RecyclerView rvMenu = findViewById(R.id.rv_curriculum_menu);
        rvMenu.setLayoutManager(new LinearLayoutManager(this));

        detailViewModel.getCourseContent(courseId).observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null && resource.data.modules != null) {

                // Đổ dữ liệu vào Adapter
                menuAdapter = new ModuleAdapter(resource.data.modules, lesson -> {
                    String contentStr = lesson.content != null ? new Gson().toJson(lesson.content) : "{}";
                    openLessonFragment(lesson.lessonId, lesson.type, contentStr, lesson.title, lesson.duration);
                });
                rvMenu.setAdapter(menuAdapter);

                // Load tiến độ tích xanh NGAY SAU KHI Sidebar đã có danh sách
                loadProgress();
            }
        });

        // 4. Xử lý BottomSheet Thảo luận
        fabDiscussion.setOnClickListener(v -> {
            if (currentLessonId != null) {
                showDiscussionBottomSheet();
            } else {
                Toast.makeText(this, "Vui lòng chọn một bài học trước", Toast.LENGTH_SHORT).show();
            }
        });

        // 5. Nạp bài học đầu tiên từ ngoài truyền vào
        String initialLessonId = getIntent().getStringExtra("LESSON_ID");
        String initialType = getIntent().getStringExtra("LESSON_TYPE");
        String initialContent = getIntent().getStringExtra("CONTENT_JSON");
        String initialTitle = getIntent().getStringExtra("LESSON_TITLE");
        int initialDuration = getIntent().getIntExtra("LESSON_DURATION", 0);

        if (initialLessonId != null && initialType != null) {
            openLessonFragment(initialLessonId, initialType, initialContent, initialTitle, initialDuration);
        }
    }

    public void loadProgress() {
        viewModel.getProgress(courseId).observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                // Cập nhật dấu tích xanh cho danh sách bên Sidebar Menu
                if (menuAdapter != null) {
                    menuAdapter.setCompletedLessons(resource.data.completedLessons);
                }
            }
        });
    }

    public void openLessonFragment(String lessonId, String type, String contentJson, String lessonTitle, int duration) {
        this.currentLessonId = lessonId;
        Fragment fragment = null;

        switch (type.toLowerCase()) {
            case "video":
                String videoId = "";
                try {
                    JSONObject contentObj = new JSONObject(contentJson);
                    if (contentObj.has("videoId")) {
                        videoId = contentObj.getString("videoId");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                fragment = VideoFragment.newInstance(courseId, lessonId, videoId, lessonTitle);
                break;

            case "quiz":
                fragment = QuizFragment.newInstance(courseId, lessonId, contentJson, duration);
                break;

            case "assignment":
                fragment = AssignmentFragment.newInstance(courseId, lessonId);
                break;
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.learning_fragment_container, fragment)
                    .commit();

            if (drawerLayout != null) {
                drawerLayout.closeDrawers();
            }
        }
    }

    private void showDiscussionBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_discussions, null);
        bottomSheetDialog.setContentView(view);

        RecyclerView rvDiscussions = view.findViewById(R.id.rv_discussions);
        rvDiscussions.setLayoutManager(new LinearLayoutManager(this));

        DiscussionAdapter adapter = new DiscussionAdapter();

        EditText etTitle = view.findViewById(R.id.et_discussion_title);
        EditText etContent = view.findViewById(R.id.et_discussion_content);
        Button btnPost = view.findViewById(R.id.btn_post_discussion);

        adapter.setReplyListener(authorName -> {
            etContent.setText("@" + authorName + " ");
            etContent.setSelection(etContent.getText().length());
            etContent.requestFocus();

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(etContent, InputMethodManager.SHOW_IMPLICIT);
        });

        rvDiscussions.setAdapter(adapter);

        viewModel.getDiscussions(courseId, currentLessonId).observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                adapter.setDiscussions(resource.data);
            }
        });

        btnPost.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tiêu đề và nội dung", Toast.LENGTH_SHORT).show();
                return;
            }

            CreateDiscussionRequest request = new CreateDiscussionRequest(title, content);

            viewModel.createDiscussion(courseId, currentLessonId, request).observe(this, resource -> {
                switch (resource.status) {
                    case LOADING:
                        btnPost.setEnabled(false);
                        btnPost.setText("Đang gửi...");
                        break;
                    case SUCCESS:
                        btnPost.setEnabled(true);
                        btnPost.setText("Gửi câu hỏi");
                        etTitle.setText("");
                        etContent.setText("");
                        Toast.makeText(this, "Gửi thảo luận thành công!", Toast.LENGTH_SHORT).show();

                        viewModel.getDiscussions(courseId, currentLessonId).observe(this, listResource -> {
                            if (listResource.status == Resource.Status.SUCCESS) {
                                adapter.setDiscussions(listResource.data);
                                rvDiscussions.smoothScrollToPosition(0);
                            }
                        });
                        break;
                    case ERROR:
                        btnPost.setEnabled(true);
                        btnPost.setText("Gửi câu hỏi");
                        Toast.makeText(this, "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
                        break;
                }
            });
        });

        bottomSheetDialog.show();
    }

    private boolean isFullscreen = false;

    public void toggleFullscreen() {
        isFullscreen = !isFullscreen;
        applyFullscreenState();
    }

    @Override
    public void onConfigurationChanged(@NonNull android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        View rightColumn = findViewById(R.id.nav_view_curriculum);
        boolean isPhone = rightColumn instanceof com.google.android.material.navigation.NavigationView;
        
        // Trên điện thoại, xoay ngang tự động bật Fullscreen. Trên Tablet thì không.
        if (isPhone) {
            isFullscreen = (newConfig.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE);
            applyFullscreenState();
        }
    }

    private void handleOrientationChange(int orientation) {
        // Chỉ chạy lúc onCreate() để set trạng thái ban đầu
        View rightColumn = findViewById(R.id.nav_view_curriculum);
        boolean isPhone = rightColumn instanceof com.google.android.material.navigation.NavigationView;
        if (isPhone && orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            isFullscreen = true;
        }
        applyFullscreenState();
    }

    private void applyFullscreenState() {
        View appBar = findViewById(R.id.toolbar_learning);
        View rightColumn = findViewById(R.id.nav_view_curriculum);
        View separator = findViewById(R.id.view_separator);
        boolean isPhone = rightColumn instanceof com.google.android.material.navigation.NavigationView;

        // Báo cho VideoFragment biết trạng thái Fullscreen
        androidx.fragment.app.Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.learning_fragment_container);
        if (fragment instanceof VideoFragment) {
            ((VideoFragment) fragment).setFullscreenState(isFullscreen);
        }

        if (isFullscreen) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            if (appBar != null && appBar.getParent() instanceof View) {
                ((View) appBar.getParent()).setVisibility(View.GONE);
            }
            if (fabDiscussion != null) fabDiscussion.setVisibility(View.GONE);
            
            // Ẩn Menu giáo trình trên Tablet để Video chiếm 100%
            if (!isPhone && rightColumn != null) rightColumn.setVisibility(View.GONE);
            if (separator != null) separator.setVisibility(View.GONE);
            
            // Ép xoay ngang nếu là điện thoại
            if (isPhone) {
                setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            if (appBar != null && appBar.getParent() instanceof View) {
                ((View) appBar.getParent()).setVisibility(View.VISIBLE);
            }
            if (fabDiscussion != null) fabDiscussion.setVisibility(View.VISIBLE);
            
            if (!isPhone && rightColumn != null) rightColumn.setVisibility(View.VISIBLE);
            if (separator != null) separator.setVisibility(View.VISIBLE);
            
            // Khôi phục dọc nếu là điện thoại
            if (isPhone) {
                setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    }

    public LearningViewModel getViewModel() {
        return viewModel;
    }
}