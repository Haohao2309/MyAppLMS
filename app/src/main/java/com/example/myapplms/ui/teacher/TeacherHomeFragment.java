package com.example.myapplms.ui.teacher;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.LMSApplication;
import com.example.myapplms.R;
import com.example.myapplms.data.repository.CourseRepository;
import com.example.myapplms.databinding.FragmentTeacherHomeBinding;
import com.example.myapplms.model.Course;
import com.example.myapplms.ui.base.BaseFragment;
import com.example.myapplms.ui.explore.TeacherCourseViewModel;
import com.example.myapplms.ui.explore.TeacherCourseViewModelFactory;
import com.example.myapplms.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class TeacherHomeFragment extends BaseFragment<FragmentTeacherHomeBinding> {

    private TeacherCourseViewModel courseViewModel;
    private TeacherCourseAdapter courseAdapter;
    private final List<Course> myCourseList = new ArrayList<>();
    private boolean coursesLoaded = false; // tránh load lại nhiều lần

    @NonNull
    @Override
    protected FragmentTeacherHomeBinding inflateBinding(@NonNull LayoutInflater inflater,
                                                        @Nullable ViewGroup container) {
        return FragmentTeacherHomeBinding.inflate(inflater, container, false);
    }

    @Override
    protected void setupViews() {
        // Dữ liệu giả cho stats — sau thay bằng API
        getBinding().tvEarnings.setText("$12.450");
        getBinding().tvEarningsChange.setText("+18% from last month");
        getBinding().tvEnrollments.setText("834 enrollments");
        getBinding().tvTotalStudents.setText("87.650");
        getBinding().tvRevenue.setText("$125k");
        getBinding().tvAvgRating.setText("4.8");
        getBinding().tvTotalCourses.setText("4");

        showTab(0); // mặc định Overview
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LMSApplication app = (LMSApplication) requireActivity().getApplication();
        CourseRepository repo = new CourseRepository(app.getRetrofitClient().getApiService());
        courseViewModel = new ViewModelProvider(this,
                new TeacherCourseViewModelFactory(repo))
                .get(TeacherCourseViewModel.class);

        // Adapter mở CourseFormActivity với ĐỦ dữ liệu để điền vào form sửa
        courseAdapter = new TeacherCourseAdapter(myCourseList, course -> {
            Intent intent = new Intent(getContext(), CourseFormActivity.class);
            intent.putExtra(CourseFormActivity.EXTRA_COURSE_ID, course.id);
            intent.putExtra(CourseFormActivity.EXTRA_COURSE_TITLE, course.title);
            intent.putExtra(CourseFormActivity.EXTRA_COURSE_DESCRIPTION, course.instructor); // tạm dùng
            intent.putExtra(CourseFormActivity.EXTRA_COURSE_PRICE, course.priceText);
            intent.putExtra(CourseFormActivity.EXTRA_COURSE_IMAGE_URL, ""); // sau thêm imageUrl vào Course model
            startActivity(intent);
        });

        RecyclerView rvCourses =
                new RecyclerView(requireContext());
        rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCourses.setAdapter(courseAdapter);
        getBinding().layoutMyCourses.addView(rvCourses);

        observeMyCourses();
    }

    @Override
    protected void setupListeners() {
        getBinding().tabOverview.setOnClickListener(v -> showTab(0));
        getBinding().tabMyCourses.setOnClickListener(v -> {
            showTab(1);
            if (!coursesLoaded) loadMyCourses(); // chỉ load 1 lần
        });
        getBinding().tabStudents.setOnClickListener(v -> showTab(2));
    }

    private void showTab(int index) {
        int colorSelected   = 0xFF6C63FF;
        int colorUnselected = 0xFF888888;

        // 1. Reset màu chữ về mặc định
        getBinding().tabOverview.setTextColor(colorUnselected);
        getBinding().tabMyCourses.setTextColor(colorUnselected);
        getBinding().tabStudents.setTextColor(colorUnselected);

        // 2. Xóa background (viền ngoài) của tất cả các tab bằng cách truyền số 0
        getBinding().tabOverview.setBackgroundResource(0);
        getBinding().tabMyCourses.setBackgroundResource(0);
        getBinding().tabStudents.setBackgroundResource(0);

        // 3. Ẩn tất cả nội dung layout
        getBinding().layoutOverview.setVisibility(View.GONE);
        getBinding().layoutMyCourses.setVisibility(View.GONE);
        getBinding().layoutStudents.setVisibility(View.GONE);

        getBinding().tabOverview.setTypeface(null, Typeface.NORMAL);
        getBinding().tabMyCourses.setTypeface(null, Typeface.NORMAL);
        getBinding().tabStudents.setTypeface(null, Typeface.NORMAL);

        // 4. Áp dụng màu chữ, background và hiển thị layout cho tab được chọn
        switch (index) {
            case 0:
                getBinding().tabOverview.setTextColor(colorSelected);
                getBinding().tabOverview.setBackgroundResource(R.drawable.bg_tab_selected); // Thêm viền
                getBinding().tabOverview.setTypeface(null, Typeface.BOLD);
                getBinding().layoutOverview.setVisibility(View.VISIBLE);
                break;
            case 1:
                getBinding().tabMyCourses.setTextColor(colorSelected);
                getBinding().tabMyCourses.setBackgroundResource(R.drawable.bg_tab_selected); // Thêm viền
                getBinding().tabMyCourses.setTypeface(null, Typeface.BOLD);
                getBinding().layoutMyCourses.setVisibility(View.VISIBLE);
                break;
            case 2:
                getBinding().tabStudents.setTextColor(colorSelected);
                getBinding().tabStudents.setBackgroundResource(R.drawable.bg_tab_selected); // Thêm viền
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
            showToast("Không tìm thấy thông tin giảng viên");
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
                        courseAdapter.notifyDataSetChanged();
                    }
                    break;
                case ERROR:
                    showToast(result.message);
                    break;
            }
        });
    }
}