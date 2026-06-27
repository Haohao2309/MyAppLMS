package com.example.myapplms.ui.student.course_detail;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.myapplms.R;
import com.example.myapplms.data.RetrofitClient;
import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.repository.CourseDetailRepository;
import com.example.myapplms.ui.student.course_detail.adapter.CoursePagerAdapter;
import com.example.myapplms.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CourseDetailActivity extends AppCompatActivity {

    private CourseDetailViewModel viewModel;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TextView tvCourseTitle, tvCategory;
    private ImageView ivCourse;

    private int courseId;
    private boolean isTeacher; // ← kiểm tra role từ SessionManager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail  );

        courseId = getIntent().getIntExtra("COURSE_ID", -1);

        // ✅ Lấy role trực tiếp từ SessionManager — không cần truyền qua Intent
        SessionManager sessionManager = new SessionManager(this);
        isTeacher = "TEACHER".equalsIgnoreCase(sessionManager.getRole());

        initViews();
        setupViewModel();
        observeHeaderData(courseId);
        setupViewPager();
    }

    private void initViews() {
        tabLayout      = findViewById(R.id.tab_layout);
        viewPager      = findViewById(R.id.view_pager);
        tvCourseTitle  = findViewById(R.id.tv_course_title);
        tvCategory     = findViewById(R.id.tv_category);
        ivCourse       = findViewById(R.id.img_course_cover);

        android.widget.ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private void setupViewModel() {
        SessionManager sessionManager = new SessionManager(this);
        LmsApiService apiService = RetrofitClient.getInstance(sessionManager).create(LmsApiService.class);
        CourseDetailRepository repository = new CourseDetailRepository(apiService);
        viewModel = new ViewModelProvider(this, new CourseDetailViewModelFactory(repository))
                .get(CourseDetailViewModel.class);
    }

    private void setupViewPager() {
        // Truyền isTeacher vào adapter để thay tab Curriculum → Chấm điểm
        CoursePagerAdapter adapter = new CoursePagerAdapter(this, courseId, isTeacher);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (isTeacher) {
                switch (position) {
                    case 0: tab.setText("Overview");    break;
                    case 1: tab.setText("Chấm điểm");  break; // ← thay Curriculum
                    case 2: tab.setText("Reviews");     break;
                }
            } else {
                switch (position) {
                    case 0: tab.setText("Overview");    break;
                    case 1: tab.setText("Curriculum");  break;
                    case 2: tab.setText("Reviews");     break;
                }
            }
        }).attach();
    }

    private void observeHeaderData(int courseId) {
        viewModel.getCourseDetail(courseId).observe(this, resource -> {
            switch (resource.status) {
                case SUCCESS:
                    if (resource.data != null) {
                        tvCourseTitle.setText(resource.data.title);
                        tvCategory.setText(resource.data.category);
                        Glide.with(this)
                                .load(resource.data.imageUrl)
                                .placeholder(R.drawable.ic_launcher_background)
                                .centerCrop()
                                .into(ivCourse);
                    }
                    break;
                case ERROR:
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getCourseContent(courseId);
    }
}