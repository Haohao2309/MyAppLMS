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
import com.example.myapplms.ui.student.learning.LearningActivity; // Import LearningActivity

public class CurriculumFragment extends Fragment {

    private CourseDetailViewModel sharedViewModel;
    private RecyclerView rvModules;
    private ProgressBar progressBar;
    private TextView tvStats;

    // TODO: BẠN CẦN LẤY GIÁ TRỊ NÀY TỪ VIEW MODEL HOẶC ACTIVITY (Biết user đã mua hay chưa)
    private boolean isPurchased = true; // Tạm thời để true để bạn test chuyển màn hình

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_curriculum, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvModules = view.findViewById(R.id.rv_modules);
        progressBar = view.findViewById(R.id.progress_bar);
        tvStats = view.findViewById(R.id.tv_stats);

        rvModules.setLayoutManager(new LinearLayoutManager(getContext()));
        sharedViewModel = new ViewModelProvider(requireActivity()).get(CourseDetailViewModel.class);

        int courseId = requireActivity().getIntent().getIntExtra("COURSE_ID", 6);

        sharedViewModel.getCourseContent(courseId).observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case LOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    if (resource.data != null && resource.data.modules != null && !resource.data.modules.isEmpty()) {
                        tvStats.setText(resource.data.modules.size() + " sections • " + resource.data.metadata.totalLessons + " lessons");

                        ModuleAdapter adapter = new ModuleAdapter(resource.data.modules, lesson -> {

                            // 1. KIỂM TRA NẾU ĐÃ MUA KHÓA HỌC -> CHUYỂN THẲNG SANG PHÒNG HỌC
                            if (isPurchased) {
                                Intent intent = new Intent(requireActivity(), LearningActivity.class);
                                intent.putExtra("COURSE_ID", courseId);

                                // THÊM 3 DÒNG NÀY ĐỂ TRUYỀN DATA BÀI HỌC SANG:
                                intent.putExtra("LESSON_ID", lesson.lessonId);
                                intent.putExtra("LESSON_TYPE", lesson.type);
                                if (lesson.content != null) {
                                    intent.putExtra("CONTENT_JSON", new com.google.gson.Gson().toJson(lesson.content));
                                }

                                startActivity(intent);
                            }
                            // 2. NẾU CHƯA MUA -> KIỂM TRA XEM CÓ ĐƯỢC HỌC THỬ KHÔNG
                            else {
                                if (lesson.isPreview) {
                                    Toast.makeText(getContext(), "Tính năng xem thử đang phát triển: " + lesson.title, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Vui lòng mua khóa học để xem bài giảng này!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        rvModules.setAdapter(adapter);
                        if (isPurchased) {
                            loadProgressIntoAdapter(adapter, courseId);
                        }
                    } else {
                        Toast.makeText(getContext(), "Khóa học này chưa có video bài giảng!", Toast.LENGTH_LONG).show();
                    }
                    break;
                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi: " + resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }
    private void loadProgressIntoAdapter(ModuleAdapter adapter, int courseId) {
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
}