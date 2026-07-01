package com.example.myapplms.ui.teacher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplms.LMSApplication;
import com.example.myapplms.data.repository.CourseRepository;
import com.example.myapplms.data.repository.MediaRepository;
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
        adapter = new TeacherCourseAdapter(
                courseList,
                // ── Callback Edit ──────────────────────────────────────
                course -> {
                    Intent intent = new Intent(getContext(), CourseFormActivity.class);
                    intent.putExtra(CourseFormActivity.EXTRA_COURSE_ID, course.id);
                    intent.putExtra(CourseFormActivity.EXTRA_COURSE_TITLE, course.title);
                    intent.putExtra(CourseFormActivity.EXTRA_COURSE_DESCRIPTION, course.description);
                    intent.putExtra(CourseFormActivity.EXTRA_COURSE_PRICE, course.priceText);
                    intent.putExtra(CourseFormActivity.EXTRA_COURSE_IMAGE_URL, course.imageUrl != null ? course.imageUrl : "");
                    intent.putExtra(CourseFormActivity.EXTRA_COURSE_CATEGORY, course.category != null ? course.category : "");
                    courseFormLauncher.launch(intent);
                },
                // ── Callback Xóa mềm ──────────────────────────────────
                course -> showDeleteDialog(course),
                // ── Callback Khôi phục ────────────────────────────────
                course -> showRestoreDialog(course)
        );

        getBinding().rvMyCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        getBinding().rvMyCourses.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LMSApplication app = (LMSApplication) requireActivity().getApplication();
        CourseRepository repository = new CourseRepository(app.getRetrofitClient().getApiService());
        MediaRepository mediaRepo = new MediaRepository(
                app.getRetrofitClient().getApiService(),
                new SessionManager(requireContext())
        );
        viewModel = new ViewModelProvider(this,
                new TeacherCourseViewModelFactory(repository, mediaRepo))
                .get(TeacherCourseViewModel.class);

        // Lấy teacherId local để dùng lại
        SessionManager session = new SessionManager(requireContext());
        teacherId = session.getTeacherId();

        observeMyCourses();
        observeDeleteResult();
        observeRestoreResult();

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

    // ── Dialog xác nhận xóa mềm ──────────────────────────────
    private void showDeleteDialog(Course course) {
        if (getContext() == null) return;

        // EditText nhập lý do xóa
        EditText etReason = new EditText(getContext());
        etReason.setHint("Nhập lý do xóa khóa học...");
        etReason.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        etReason.setMaxLines(3);

        // Padding cho EditText bên trong dialog
        FrameLayout container = new FrameLayout(getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(48, 16, 48, 0);
        etReason.setLayoutParams(params);
        container.addView(etReason);

        new AlertDialog.Builder(getContext())
                .setTitle("Xóa mềm khóa học")
                .setMessage("Bạn muốn xóa \"" + course.title + "\"?\n\nKhóa học sẽ bị ẩn khỏi danh sách, sinh viên đã mua sẽ nhận thông báo.")
                .setView(container)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    String reason = etReason.getText().toString().trim();
                    if (reason.isEmpty()) {
                        reason = "Không dùng nữa";
                    }
                    // Lấy tên giảng viên làm deletedBy
                    SessionManager session = new SessionManager(requireContext());
                    String deletedBy = "Teacher_" + (session.getTeacherId() != null ? session.getTeacherId() : "Unknown");
                    viewModel.deleteCourse(course.id, deletedBy, reason);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ── Dialog xác nhận khôi phục ────────────────────────────
    private void showRestoreDialog(Course course) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Khôi phục khóa học")
                .setMessage("Bạn muốn khôi phục \"" + course.title + "\"?\n\nKhóa học sẽ được hiển thị trở lại và sinh viên đã mua sẽ nhận thông báo.")
                .setPositiveButton("Khôi phục", (dialog, which) -> {
                    SessionManager session = new SessionManager(requireContext());
                    String restoredBy = "Teacher_" + (session.getTeacherId() != null ? session.getTeacherId() : "Unknown");
                    viewModel.restoreCourse(course.id, restoredBy);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ── Observe danh sách khóa học ───────────────────────────
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

    // ── Observe kết quả xóa mềm ──────────────────────────────
    private void observeDeleteResult() {
        viewModel.deleteResult.observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    getBinding().progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    getBinding().progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),
                            "Xóa khóa học thành công! Sinh viên đã nhận thông báo.",
                            Toast.LENGTH_LONG).show();
                    // Reload danh sách sau khi xóa
                    if (teacherId != null) viewModel.loadMyCourses(teacherId);
                    break;
                case ERROR:
                    getBinding().progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi: " + result.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    // ── Observe kết quả khôi phục ────────────────────────────
    private void observeRestoreResult() {
        viewModel.restoreResult.observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    getBinding().progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    getBinding().progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),
                            "Khôi phục thành công! Sinh viên đã nhận thông báo.",
                            Toast.LENGTH_LONG).show();
                    // Reload danh sách sau khi khôi phục
                    if (teacherId != null) viewModel.loadMyCourses(teacherId);
                    break;
                case ERROR:
                    getBinding().progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi: " + result.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}