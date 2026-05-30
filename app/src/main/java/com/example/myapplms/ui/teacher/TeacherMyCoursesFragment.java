package com.example.myapplms.ui.teacher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplms.LMSApplication;
import com.example.myapplms.data.repository.CourseRepository;
import com.example.myapplms.databinding.FragmentTeacherMyCoursesBinding;
import com.example.myapplms.model.Course;
import com.example.myapplms.ui.base.BaseFragment;
import com.example.myapplms.ui.explore.TeacherCourseViewModel;
import com.example.myapplms.ui.explore.TeacherCourseViewModelFactory;
import com.example.myapplms.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class TeacherMyCoursesFragment extends BaseFragment<FragmentTeacherMyCoursesBinding> {

    private TeacherCourseViewModel viewModel;
    private TeacherCourseAdapter adapter;
    private final List<Course> courseList = new ArrayList<>();
    private Integer teacherId;

    // Launcher để nhận kết quả từ CourseFormActivity
    // Khi thêm/sửa xong → tự reload danh sách
    private final ActivityResultLauncher<Intent> courseFormLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && teacherId != null) {
                    viewModel.loadMyCourses(teacherId); // reload sau khi tạo/sửa
                }
            });

    @NonNull
    @Override
    protected FragmentTeacherMyCoursesBinding inflateBinding(@NonNull LayoutInflater inflater,
                                                             @Nullable ViewGroup container) {
        return FragmentTeacherMyCoursesBinding.inflate(inflater, container, false);
    }

    @Override
    protected void setupViews() {
        adapter = new TeacherCourseAdapter(courseList, course -> {
            // Mở màn hình Edit với ĐỦ dữ liệu
            Intent intent = new Intent(getContext(), CourseFormActivity.class);
            intent.putExtra(CourseFormActivity.EXTRA_COURSE_ID, course.id);
            intent.putExtra(CourseFormActivity.EXTRA_COURSE_TITLE, course.title);
            intent.putExtra(CourseFormActivity.EXTRA_COURSE_PRICE, course.priceText);
            intent.putExtra(CourseFormActivity.EXTRA_COURSE_IMAGE_URL, "");
            courseFormLauncher.launch(intent);
        });

        getBinding().rvMyCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        getBinding().rvMyCourses.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LMSApplication app = (LMSApplication) requireActivity().getApplication();
        CourseRepository repository = new CourseRepository(app.getRetrofitClient().getApiService());
        viewModel = new ViewModelProvider(this, new TeacherCourseViewModelFactory(repository))
                .get(TeacherCourseViewModel.class);

        // Lấy teacherId local để dùng lại
        SessionManager session = new SessionManager(requireContext());
        teacherId = session.getTeacherId();

        observeMyCourses();

        if (teacherId != null) {
            viewModel.loadMyCourses(teacherId);
        } else {
            Toast.makeText(getContext(), "Không tìm thấy thông tin giảng viên", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void setupListeners() {
        // Nút tạo mới → mở CourseFormActivity ở chế độ tạo mới (không có EXTRA_COURSE_ID)
        getBinding().btnCreateCourse.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CourseFormActivity.class);
            // Không truyền EXTRA_COURSE_ID → mặc định -1 → chế độ tạo mới
            courseFormLauncher.launch(intent);
        });
    }

    private void observeMyCourses() {
        viewModel.myCourses.observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    getBinding().progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    getBinding().progressBar.setVisibility(View.GONE);
                    if (result.data != null) {
                        courseList.clear();
                        courseList.addAll(result.data);
                        adapter.notifyDataSetChanged();
                    }
                    break;
                case ERROR:
                    getBinding().progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), result.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}