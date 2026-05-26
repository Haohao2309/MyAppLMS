package com.example.myapplms.ui.student.course_detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplms.R;
import com.example.myapplms.ui.student.course_detail.adapter.ModuleAdapter;

public class CurriculumFragment extends Fragment {

    private CourseDetailViewModel sharedViewModel;
    private RecyclerView rvModules;
    private ProgressBar progressBar;
    private TextView tvStats;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_curriculum, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvModules = view.findViewById(R.id.rv_modules);
        progressBar = view.findViewById(R.id.progress_bar);
        tvStats = view.findViewById(R.id.tv_stats); // Hiển thị "X sections • Y lessons"

        rvModules.setLayoutManager(new LinearLayoutManager(getContext()));

        // Lấy lại ViewModel đã được khởi tạo ở Activity
        sharedViewModel = new ViewModelProvider(requireActivity()).get(CourseDetailViewModel.class);

        // Lắng nghe dữ liệu Content MongoDB
        // Lưu ý: Lấy courseId từ Bundle arguments hoặc Activity intent nếu cần gọi trực tiếp
        int courseId = requireActivity().getIntent().getIntExtra("COURSE_ID", 2);

        sharedViewModel.getCourseContent(courseId).observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case LOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    if (resource.data != null) {
                        tvStats.setText(resource.data.modules.size() + " sections • " + resource.data.metadata.totalLessons + " lessons");
                        ModuleAdapter adapter = new ModuleAdapter(resource.data.modules);
                        rvModules.setAdapter(adapter);
                    }
                    break;
                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    break;
            }
        });
    }
}