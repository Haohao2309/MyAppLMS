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
        drawerLayout = findViewById(R.id.drawer_layout);
        fabDiscussion = findViewById(R.id.fab_discussion);
        ivMenuToggle = findViewById(R.id.iv_menu_toggle);

        SessionManager sessionManager = new SessionManager(this);
        LmsApiService apiService = RetrofitClient.getInstance(sessionManager).create(LmsApiService.class);

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
                    openLessonFragment(lesson.lessonId, lesson.type, contentStr, lesson.title);
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

        if (initialLessonId != null && initialType != null) {
            openLessonFragment(initialLessonId, initialType, initialContent, initialTitle);
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

    public void openLessonFragment(String lessonId, String type, String contentJson, String lessonTitle) {
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
                fragment = QuizFragment.newInstance(courseId, lessonId, contentJson);
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

    public LearningViewModel getViewModel() {
        return viewModel;
    }
}