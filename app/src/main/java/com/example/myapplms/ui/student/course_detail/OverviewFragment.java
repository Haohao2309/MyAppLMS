package com.example.myapplms.ui.student.course_detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplms.R;

public class OverviewFragment extends Fragment {

    private CourseDetailViewModel sharedViewModel;
    private TextView tvDescription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Sử dụng layout fragment_overview.xml ở phần trước
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvDescription = view.findViewById(R.id.tv_course_description);

        // Lấy Shared ViewModel (Chung với Activity và CurriculumFragment)
        sharedViewModel = new ViewModelProvider(requireActivity()).get(CourseDetailViewModel.class);

        int courseId = requireActivity().getIntent().getIntExtra("COURSE_ID", 2);

        // Hứng data từ API 1 (PostgreSQL - getCourseDetail) thay vì API 2 (MongoDB)
        sharedViewModel.getCourseDetail(courseId).observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case SUCCESS:
                    if (resource.data != null) {
                        // Hiển thị đoạn văn giới thiệu khóa học từ bảng Course (PostgreSQL)
                        tvDescription.setText(resource.data.description);
                    }
                    break;
                case ERROR:
                    tvDescription.setText("Lỗi tải dữ liệu: " + resource.message);
                    break;
                case LOADING:
                    tvDescription.setText("Đang tải dữ liệu...");
                    break;
            }
        });
    }
}