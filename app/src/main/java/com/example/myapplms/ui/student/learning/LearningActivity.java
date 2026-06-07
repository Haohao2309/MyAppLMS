package com.example.myapplms.ui.student.learning;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.LMSApplication;
import com.example.myapplms.R;
import com.example.myapplms.data.repository.LearningRepository;
import com.example.myapplms.ui.student.course_detail.adapter.ModuleAdapter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LearningActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private LearningViewModel viewModel;
    private int courseId;

    // ĐÃ SỬA: Đưa menuAdapter lên đây làm thuộc tính của Class để gọi được ở mọi hàm
    private ModuleAdapter menuAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);

        courseId = getIntent().getIntExtra("COURSE_ID", -1);
        drawerLayout = findViewById(R.id.drawer_layout);

        // Hứng dữ liệu từ Intent để tự động phát bài học khi vừa bấm vào từ bên ngoài
        String initialLessonId = getIntent().getStringExtra("LESSON_ID");
        String initialType = getIntent().getStringExtra("LESSON_TYPE");
        String initialContentJson = getIntent().getStringExtra("CONTENT_JSON");

        // Khởi tạo hệ thống mạng tích hợp Token bảo mật
        com.example.myapplms.utils.SessionManager sessionManager = new com.example.myapplms.utils.SessionManager(this);
        com.example.myapplms.data.remote.api.LmsApiService apiService =
                com.example.myapplms.data.RetrofitClient.getInstance(sessionManager).create(com.example.myapplms.data.remote.api.LmsApiService.class);

        LearningRepository repository = new LearningRepository(apiService);
        viewModel = new ViewModelProvider(this, new LearningViewModelFactory(repository)).get(LearningViewModel.class);

        // Bắt sự kiện bấm nút Menu góc phải trên Toolbar để mở ngăn kéo bài học
        android.widget.ImageView ivMenuToggle = findViewById(R.id.iv_menu_toggle);
        ivMenuToggle.setOnClickListener(v -> drawerLayout.openDrawer(androidx.core.view.GravityCompat.END));

        // Tải danh sách cấu trúc bài học lên thanh Menu trượt
        loadCurriculumMenu();

        if (initialLessonId != null && initialType != null) {
            openLessonFragment(initialLessonId, initialType, initialContentJson);
        } else {
            drawerLayout.openDrawer(androidx.core.view.GravityCompat.END);
        }
    }

    private void loadCurriculumMenu() {
        LMSApplication app = (LMSApplication) getApplication();

        app.getRetrofitClient().getApiService().getCourseContent(courseId).enqueue(new retrofit2.Callback<com.example.myapplms.data.remote.dto.response.course_content.CourseContentResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myapplms.data.remote.dto.response.course_content.CourseContentResponse> call, retrofit2.Response<com.example.myapplms.data.remote.dto.response.course_content.CourseContentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RecyclerView rvCurriculum = findViewById(R.id.rv_curriculum_menu);
                    rvCurriculum.setLayoutManager(new LinearLayoutManager(LearningActivity.this));

                    // Chuyển đổi dữ liệu DTO sang định dạng cấu trúc Model giao diện
                    String json = new com.google.gson.Gson().toJson(response.body().modules);
                    java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<java.util.List<com.example.myapplms.model.course_content.CourseModule>>(){}.getType();
                    java.util.List<com.example.myapplms.model.course_content.CourseModule> mappedModules = new com.google.gson.Gson().fromJson(json, listType);

                    // Gán trực tiếp vào thuộc tính toàn cục menuAdapter (Không thêm chữ ModuleAdapter ở đầu dòng)
                    menuAdapter = new ModuleAdapter(mappedModules, lesson -> {

                        com.example.myapplms.data.remote.dto.response.course_content.LessonResponse matchingLesson = null;
                        for (com.example.myapplms.data.remote.dto.response.course_content.ModuleResponse moduleResp : response.body().modules) {
                            for (com.example.myapplms.data.remote.dto.response.course_content.LessonResponse lessonResp : moduleResp.lessons) {
                                if (lessonResp.lessonId != null && lessonResp.lessonId.equals(lesson.lessonId)) {
                                    matchingLesson = lessonResp;
                                    break;
                                }
                            }
                            if (matchingLesson != null) break;
                        }

                        String contentJson = "";
                        String lessonType = "video";

                        if (matchingLesson != null) {
                            lessonType = matchingLesson.type;
                            if (matchingLesson.content != null) {
                                contentJson = new com.google.gson.Gson().toJson(matchingLesson.content);
                            }
                        }

                        openLessonFragment(lesson.lessonId, lessonType, contentJson);
                    });

                    rvCurriculum.setAdapter(menuAdapter);

                    // Đồng bộ và tải thông tin tiến độ học tập thực tế
                    loadProgress();
                } else {
                    Toast.makeText(LearningActivity.this, "Không thể tải danh sách bài học!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.myapplms.data.remote.dto.response.course_content.CourseContentResponse> call, Throwable t) {
                Toast.makeText(LearningActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm đồng bộ tiến độ, hiển thị % lên Toolbar và đánh tích xanh ✅ vào menu bài học
    public void loadProgress() {
        viewModel.getProgress(courseId).observe(this, result -> {
            if (result.status == com.example.myapplms.utils.Resource.Status.SUCCESS && result.data != null) {

                // 1. Cập nhật phần trăm học tập lên phụ đề Toolbar
                androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_learning);
                toolbar.setSubtitle("Đã học: " + result.data.overallProgress + "%");

                // 2. Truyền danh sách mã bài học đã hoàn thành vào Menu trượt
                if (menuAdapter != null) {
                    menuAdapter.setCompletedLessons(result.data.completedLessons);
                }
            }
        });
    }

    public void openLessonFragment(String lessonId, String type, String contentJson) {
        Fragment fragment = null;

        switch (type.toLowerCase()) {
            case "video":
                String videoId = "";
                try {
                    org.json.JSONObject contentObj = new org.json.JSONObject(contentJson);
                    if (contentObj.has("videoId")) {
                        videoId = contentObj.getString("videoId");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Lỗi đọc dữ liệu video!", Toast.LENGTH_SHORT).show();
                }

                fragment = VideoFragment.newInstance(courseId, lessonId, videoId);
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

            drawerLayout.closeDrawers();
        }
    }

    public LearningViewModel getViewModel() {
        return viewModel;
    }
}