package com.example.myapplms.ui.student.course_detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.R;
import com.example.myapplms.ui.student.course_detail.adapter.ReviewAdapter;

public class ReviewsFragment extends Fragment {

    private CourseDetailViewModel sharedViewModel;
    private RecyclerView rvReviews;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reviews, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvReviews = view.findViewById(R.id.rv_reviews);
        progressBar = view.findViewById(R.id.progress_bar);

        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));

        // Hứng ViewModel dùng chung từ Activity cha
        sharedViewModel = new ViewModelProvider(requireActivity()).get(CourseDetailViewModel.class);

        int courseId = requireActivity().getIntent().getIntExtra("COURSE_ID", -1);
        if (courseId != -1) {
            observeReviews(courseId);
        }
    }

    private void observeReviews(int courseId) {
        sharedViewModel.getCourseReviews(courseId).observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    if (resource.data != null && !resource.data.isEmpty()) {
                        ReviewAdapter adapter = new ReviewAdapter(resource.data);
                        rvReviews.setAdapter(adapter);
                    } else {
                        Toast.makeText(getContext(), "Chưa có đánh giá nào.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi tải đánh giá: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}