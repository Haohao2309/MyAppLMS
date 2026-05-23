// ui/explore/ExploreFragment.java
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

public class ExploreFragment extends BaseFragment<FragmentExploreListCourseBinding> {

    private ExploreViewModel viewModel;
    private CourseAdapter adapter;
    private List<Course> courseList;

    @NonNull
    @Override
    protected FragmentExploreListCourseBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentExploreListCourseBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo Repository & ViewModel thủ công (Do không dùng Hilt ở đây)
        LMSApplication app = (LMSApplication) requireActivity().getApplication();
        CourseRepository repository = new CourseRepository(app.getRetrofitClient().getApiService());

        viewModel = new ViewModelProvider(this, new ExploreViewModelFactory(repository))
                .get(ExploreViewModel.class);

        // 2. Lắng nghe dữ liệu (Observe)
        observeCourses();

        // 3. Kích hoạt gọi API
        viewModel.loadCourses();

        // Lắng nghe sau khi gọi hàm load để UI nhận tín hiệu
        observeCourses();
    }

    @Override
    protected void setupViews() {
        // Cài đặt khung rỗng cho RecyclerView
        courseList = new ArrayList<>();
        adapter = new CourseAdapter(courseList);
        getBinding().rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        getBinding().rvCourses.setAdapter(adapter);
    }

    private void observeCourses() {
        viewModel.getCourses().observe(getViewLifecycleOwner(), result -> {
            switch (result.status) {
                case LOADING:
                    // Tạm thời chưa có ProgressBar trong XML, nên mình để trống. 
                    // Bạn có thể thêm ProgressBar vào fragment_explore_list_course.xml sau.
                    break;

                case SUCCESS:
                    if (result.data != null) {
                        courseList.clear();
                        courseList.addAll(result.data);
                        adapter.notifyDataSetChanged();

                        // Cập nhật TextView tổng số khóa học
                        getBinding().tvCourseCount.setText(String.format(Locale.getDefault(), "%d courses found", courseList.size()));
                    }
                    break;

                case ERROR:
                    Toast.makeText(getContext(), result.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}