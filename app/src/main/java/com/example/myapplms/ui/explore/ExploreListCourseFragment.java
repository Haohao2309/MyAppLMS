package com.example.myapplms.ui.explore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplms.CourseAdapter;
import com.example.myapplms.LMSApplication;
import com.example.myapplms.data.repository.CourseRepository;
import com.example.myapplms.databinding.FragmentExploreListCourseBinding;
import com.example.myapplms.model.Course;
import com.example.myapplms.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExploreListCourseFragment extends BaseFragment<FragmentExploreListCourseBinding> {

    private ExploreViewModel viewModel;
    private CourseAdapter adapter;
    private final List<Course> courseList = new ArrayList<>();

    @NonNull
    @Override
    protected FragmentExploreListCourseBinding inflateBinding(@NonNull LayoutInflater inflater,
                                                              @Nullable ViewGroup container) {
        return FragmentExploreListCourseBinding.inflate(inflater, container, false);
    }

    @Override
    protected void setupViews() {
        // Thiết lập RecyclerView với list rỗng trước
        adapter = new CourseAdapter(courseList);
        getBinding().rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        getBinding().rvCourses.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState); // setupViews() chạy ở đây trong BaseFragment

        // Khởi tạo ViewModel SAU khi setupViews() đã chạy
        LMSApplication app = (LMSApplication) requireActivity().getApplication();
        CourseRepository repository = new CourseRepository(app.getRetrofitClient().getApiService());
        viewModel = new ViewModelProvider(this, new ExploreViewModelFactory(repository))
                .get(ExploreViewModel.class);

        // Observe TRƯỚC khi load — tránh miss data
        observeCourses();

        // Load data từ API
        viewModel.loadCourses();
    }

    private void observeCourses() {
        viewModel.getCourses().observe(getViewLifecycleOwner(), result -> {
            switch (result.status) {
                case LOADING:
                    // Có thể thêm ProgressBar ở đây nếu cần
                    break;

                case SUCCESS:
                    if (result.data != null) {
                        courseList.clear();
                        courseList.addAll(result.data);
                        adapter.notifyDataSetChanged();
                        getBinding().tvCourseCount.setText(
                                String.format(Locale.getDefault(), "%d courses found", courseList.size())
                        );
                    }
                    break;

                case ERROR:
                    Toast.makeText(getContext(), result.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    @Override
    protected void setupListeners() {
        getBinding().layoutSearch.setOnClickListener(v ->
                Toast.makeText(getContext(), "Tính năng tìm kiếm đang phát triển", Toast.LENGTH_SHORT).show()
        );
    }
}