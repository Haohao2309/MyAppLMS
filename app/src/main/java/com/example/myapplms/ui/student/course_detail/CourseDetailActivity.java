package com.example.myapplms.ui.student.course_detail;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.example.myapplms.R;
import com.example.myapplms.data.RetrofitClient;
import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.repository.CourseDetailRepository;
import com.example.myapplms.ui.student.course_detail.adapter.CoursePagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CourseDetailActivity extends AppCompatActivity {

    private CourseDetailViewModel viewModel;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TextView tvCourseTitle, tvCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        int courseId = getIntent().getIntExtra("COURSE_ID", -1);

        initViews();
        setupViewModel();

        // ĐƯA DÒNG OBSERVE NÀY LÊN TRƯỚC
        observeHeaderData(courseId);

        // SAU ĐÓ MỚI SET UP VIEW PAGER
        setupViewPager();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        tvCourseTitle = findViewById(R.id.tv_course_title);
        tvCategory = findViewById(R.id.tv_category);
        
        android.widget.ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupViewModel() {
        // Khởi tạo SessionManager (truyền context của Activity)
        com.example.myapplms.utils.SessionManager sessionManager =
                new com.example.myapplms.utils.SessionManager(this);

        // Truyền sessionManager vào getInstance()
        LmsApiService apiService = RetrofitClient.getInstance(sessionManager).create(LmsApiService.class);

        CourseDetailRepository repository = new CourseDetailRepository(apiService);
        viewModel = new ViewModelProvider(this, new CourseDetailViewModelFactory(repository))
                .get(CourseDetailViewModel.class);
    }

    private void setupViewPager() {
        CoursePagerAdapter adapter = new CoursePagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Overview"); break;
                case 1: tab.setText("Curriculum"); break;
                case 2: tab.setText("Reviews"); break;
            }
        }).attach();
    }

    private void observeHeaderData(int courseId) {
        // Gọi API và lắng nghe data cho phần Header
        viewModel.getCourseDetail(courseId).observe(this, resource -> {
            switch (resource.status) {
                case SUCCESS:
                    if (resource.data != null) {
                        tvCourseTitle.setText(resource.data.title);
                        tvCategory.setText(resource.data.category);
                        // Bind thêm các text khác...
                    }
                    break;
                case ERROR:
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        // Gọi API lấy Content (Fragment Curriculum sẽ tự hứng kết quả này)
        viewModel.getCourseContent(courseId);
    }
}