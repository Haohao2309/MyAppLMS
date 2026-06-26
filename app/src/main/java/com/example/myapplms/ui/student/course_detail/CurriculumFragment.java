package com.example.myapplms.ui.student.course_detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.R;
import com.example.myapplms.ui.student.course_detail.adapter.ModuleAdapter;
import com.example.myapplms.ui.student.learning.LearningActivity;

public class CurriculumFragment extends Fragment {

    private CourseDetailViewModel sharedViewModel;
    private RecyclerView rvModules;
    private ProgressBar progressBar;
    private TextView tvStats;

    private boolean isPurchased = true;
    private com.example.myapplms.ui.student.course_detail.adapter.ModuleAdapter adapter;

    public static CurriculumFragment newInstance(int courseId) {
        CurriculumFragment f = new CurriculumFragment();
        Bundle args = new Bundle();
        args.putInt("COURSE_ID", courseId);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_curriculum, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvModules = view.findViewById(R.id.rv_modules);
        rvModules.setLayoutManager(new LinearLayoutManager(getContext()));

        tvStats = view.findViewById(R.id.tv_stats);
        TextView tvCollapseAll = view.findViewById(R.id.tv_collapse_all);

        // 🔥 FIX CRASH 1: Ánh xạ ProgressBar (Thiếu dòng này App sẽ sập ngay khi mở)
        progressBar = view.findViewById(R.id.progress_bar);

        boolean[] isExpanded = {false};
        tvCollapseAll.setOnClickListener(v -> {
            isExpanded[0] = !isExpanded[0];
            if (isExpanded[0]) {
                tvCollapseAll.setText("Expand All");
            } else {
                tvCollapseAll.setText("Collapse All");
            }
        });

        sharedViewModel = new ViewModelProvider(requireActivity()).get(CourseDetailViewModel.class);

        int courseId = requireActivity().getIntent().getIntExtra("COURSE_ID", -1);
        if (courseId != -1) {
            observeCourseContent(courseId);
        }
    }

    private void observeCourseContent(int courseId) {
        sharedViewModel.getCourseContent(courseId).observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (resource.data != null && resource.data.modules != null) {

                        int totalModules = resource.data.modules.size();
                        int totalLessons = 0;
                        for (com.example.myapplms.model.course_content.CourseModule m : resource.data.modules) {
                            if (m.lessons != null) totalLessons += m.lessons.size();
                        }
                        tvStats.setText(totalModules + " chương • " + totalLessons + " bài học");

                        adapter = new ModuleAdapter(resource.data.modules, lesson -> {
                            if (isPurchased) {
                                Intent intent = new Intent(getContext(), LearningActivity.class);
                                intent.putExtra("COURSE_ID", courseId);
                                intent.putExtra("LESSON_ID", lesson.lessonId);
                                intent.putExtra("LESSON_TYPE", lesson.type);
                                intent.putExtra("LESSON_TITLE", lesson.title);
                                if (lesson.content != null) {
                                    intent.putExtra("CONTENT_JSON", new com.google.gson.Gson().toJson(lesson.content));
                                }
                                startActivity(intent);
                            } else {
                                // 🔥 FIX CRASH 2: Kiểm tra an toàn cho Boolean isPreview để tránh NullPointer
                                if (Boolean.TRUE.equals(lesson.isPreview)) {
                                    Toast.makeText(getContext(), "Tính năng xem thử đang phát triển: " + lesson.title, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Vui lòng mua khóa học để xem bài giảng này!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        rvModules.setAdapter(adapter);

                        // 🔥 FIX CRASH 3: Dùng luôn biến courseId truyền vào thay vì gọi requireActivity() lần nữa
                        if (isPurchased) loadProgressIntoAdapter(adapter, courseId);
                    } else {
                        Toast.makeText(getContext(), "Khóa học này chưa có video bài giảng!", Toast.LENGTH_LONG).show();
                    }
                    break;
                case ERROR:
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi: " + resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void loadProgressIntoAdapter(ModuleAdapter adapter, int courseId) {
        if (getContext() == null) return; // Bảo vệ an toàn Context

        com.example.myapplms.utils.SessionManager sessionManager = new com.example.myapplms.utils.SessionManager(getContext());
        com.example.myapplms.data.remote.api.LmsApiService apiService = com.example.myapplms.data.RetrofitClient.getInstance(sessionManager).create(com.example.myapplms.data.remote.api.LmsApiService.class);

        apiService.getProgress(courseId).enqueue(new retrofit2.Callback<com.example.myapplms.data.remote.dto.response.ProgressResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myapplms.data.remote.dto.response.ProgressResponse> call, retrofit2.Response<com.example.myapplms.data.remote.dto.response.ProgressResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setCompletedLessons(response.body().completedLessons);
                }
            }
            @Override
            public void onFailure(retrofit2.Call<com.example.myapplms.data.remote.dto.response.ProgressResponse> call, Throwable t) {}
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null && isPurchased) {
            // Kiểm tra Activity khác null trước khi lấy Intent để bảo vệ cực đại
            if (getActivity() != null && getActivity().getIntent() != null) {
                int courseId = getActivity().getIntent().getIntExtra("COURSE_ID", -1);
                if (courseId != -1) {
                    loadProgressIntoAdapter(adapter, courseId);
                }
            }
        }
    }
}