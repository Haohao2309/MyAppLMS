package com.example.myapplms.ui.teacher;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.CourseAdapter;
import com.example.myapplms.LMSApplication;
import com.example.myapplms.R;
import com.example.myapplms.data.repository.CourseRepository;
import com.example.myapplms.data.repository.TeacherRepository;
import com.example.myapplms.databinding.FragmentTeacherHomeBinding;
import com.example.myapplms.model.Course;
import com.example.myapplms.ui.base.BaseFragment;
import com.example.myapplms.ui.explore.TeacherCourseViewModel;
import com.example.myapplms.ui.explore.TeacherCourseViewModelFactory;
import com.example.myapplms.ui.student.course_detail.CourseDetailActivity;
import com.example.myapplms.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TeacherHomeFragment extends BaseFragment<FragmentTeacherHomeBinding> {

    // ViewModel cho Tab My Courses
    private TeacherCourseViewModel courseViewModel;

    // ViewModel cho Tab Overview (Thống kê Dashboard)
    private TeacherHomeViewModel dashboardViewModel;

    private CourseAdapter courseAdapter;
    private final List<Course> myCourseList = new ArrayList<>();
    private boolean coursesLoaded = false;

    @NonNull
    @Override
    protected FragmentTeacherHomeBinding inflateBinding(@NonNull LayoutInflater inflater,
                                                        @Nullable ViewGroup container) {
        return FragmentTeacherHomeBinding.inflate(inflater, container, false);
    }

    @Override
    protected void setupViews() {
        showTab(0); // Mặc định mở tab Overview
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LMSApplication app = (LMSApplication) requireActivity().getApplication();
        SessionManager session = new SessionManager(requireContext());
        Integer teacherId = session.getTeacherId();

        // 1. Khởi tạo ViewModel cho Danh sách khóa học
        CourseRepository courseRepo = new CourseRepository(app.getRetrofitClient().getApiService());
        courseViewModel = new ViewModelProvider(this,
                new TeacherCourseViewModelFactory(courseRepo))
                .get(TeacherCourseViewModel.class);

        // 2. Khởi tạo ViewModel cho Thống kê Dashboard
        TeacherRepository teacherRepo = new TeacherRepository(app.getRetrofitClient().getApiService());
        dashboardViewModel = new ViewModelProvider(this,
                new TeacherHomeViewModelFactory(teacherRepo))
                .get(TeacherHomeViewModel.class);

        // 3. Khởi tạo Adapter cho RecyclerView (Tab My Courses)
        courseAdapter = new CourseAdapter(myCourseList, course -> {
            Intent intent = new Intent(requireContext(), CourseDetailActivity.class);
            intent.putExtra("COURSE_ID", course.id);
            startActivity(intent);
        });

        // Add RecyclerView vào layoutMyCourses (vì trong XML layoutMyCourses đang là FrameLayout trống)
        RecyclerView rvCourses = new RecyclerView(requireContext());
        rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCourses.setAdapter(courseAdapter);
        getBinding().layoutMyCourses.addView(rvCourses);

        // 4. Lắng nghe dữ liệu
        observeMyCourses();
        observeDashboardData();

        // 5. Gọi API lấy thống kê ngay khi vào màn hình
        if (teacherId != null) {
            dashboardViewModel.loadDashboard(teacherId);
        }
    }

    @Override
    protected void setupListeners() {
        getBinding().tabOverview.setOnClickListener(v -> showTab(0));
        getBinding().tabMyCourses.setOnClickListener(v -> {
            showTab(1);
            if (!coursesLoaded) loadMyCourses(); // Chỉ gọi API load course 1 lần
        });
        getBinding().tabStudents.setOnClickListener(v -> showTab(2));
    }

    private void showTab(int index) {
        int colorSelected   = 0xFF6C63FF;
        int colorUnselected = 0xFF888888;

        // Reset màu tab
        getBinding().tabOverview.setTextColor(colorUnselected);
        getBinding().tabMyCourses.setTextColor(colorUnselected);
        getBinding().tabStudents.setTextColor(colorUnselected);

        // Reset background
        getBinding().tabOverview.setBackgroundResource(0);
        getBinding().tabMyCourses.setBackgroundResource(0);
        getBinding().tabStudents.setBackgroundResource(0);

        // Ẩn tất cả nội dung
        getBinding().layoutOverview.setVisibility(View.GONE);
        getBinding().layoutMyCourses.setVisibility(View.GONE);
        getBinding().layoutStudents.setVisibility(View.GONE);

        // Reset font chữ
        getBinding().tabOverview.setTypeface(null, Typeface.NORMAL);
        getBinding().tabMyCourses.setTypeface(null, Typeface.NORMAL);
        getBinding().tabStudents.setTypeface(null, Typeface.NORMAL);

        // Active tab được chọn
        switch (index) {
            case 0:
                getBinding().tabOverview.setTextColor(colorSelected);
                getBinding().tabOverview.setBackgroundResource(R.drawable.bg_tab_selected);
                getBinding().tabOverview.setTypeface(null, Typeface.BOLD);
                getBinding().layoutOverview.setVisibility(View.VISIBLE);
                break;
            case 1:
                getBinding().tabMyCourses.setTextColor(colorSelected);
                getBinding().tabMyCourses.setBackgroundResource(R.drawable.bg_tab_selected);
                getBinding().tabMyCourses.setTypeface(null, Typeface.BOLD);
                getBinding().layoutMyCourses.setVisibility(View.VISIBLE);
                break;
            case 2:
                getBinding().tabStudents.setTextColor(colorSelected);
                getBinding().tabStudents.setBackgroundResource(R.drawable.bg_tab_selected);
                getBinding().tabStudents.setTypeface(null, Typeface.BOLD);
                getBinding().layoutStudents.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void loadMyCourses() {
        SessionManager session = new SessionManager(requireContext());
        Integer teacherId = session.getTeacherId();
        if (teacherId != null) {
            coursesLoaded = true;
            courseViewModel.loadMyCourses(teacherId);
        } else {
            Toast.makeText(getContext(), "Không tìm thấy ID giáo viên", Toast.LENGTH_SHORT).show();
        }
    }

    private void observeMyCourses() {
        courseViewModel.myCourses.observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case SUCCESS:
                    if (result.data != null) {
                        myCourseList.clear();
                        myCourseList.addAll(result.data);
                        courseAdapter.updateData(result.data); // Cập nhật adapter
                    }
                    break;
                case ERROR:
                    Toast.makeText(getContext(), result.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    // ── ĐÂY LÀ PHẦN MAP DỮ LIỆU VỚI CÁC ID TRONG XML ──
    private void observeDashboardData() {
        dashboardViewModel.dashboardData.observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    break;
                case SUCCESS:
                    if (result.data != null) {
                        // Gọi đúng ID tvEarnings, tvTotalStudents... trong file fragment_teacher_home.xml
                        getBinding().tvEarnings.setText(String.format(Locale.US, "$%,.0f", result.data.totalRevenue));
                        getBinding().tvTotalStudents.setText(String.valueOf(result.data.totalStudents));
                        getBinding().tvRevenue.setText(String.format(Locale.US, "$%,.0f", result.data.totalRevenue));
                        getBinding().tvAvgRating.setText(String.valueOf(result.data.avgRating));
                        getBinding().tvTotalCourses.setText(String.valueOf(result.data.totalCourses));

                        // Cập nhật các dòng text phụ (ví dụ: +18%, 834 enrollments)
                        if (result.data.earningsChange != null) {
                            getBinding().tvEarningsChange.setText(result.data.earningsChange);
                        }
                        getBinding().tvEnrollments.setText(result.data.enrollmentsThisMonth + " enrollments");
                    }
                    break;
                case ERROR:
                    Toast.makeText(getContext(), result.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}