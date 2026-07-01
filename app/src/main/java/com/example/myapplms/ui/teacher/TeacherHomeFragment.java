package com.example.myapplms.ui.teacher;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplms.LMSApplication;
import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.response.CourseResponse;
import com.example.myapplms.data.remote.dto.response.PagedResponse;
import com.example.myapplms.data.remote.dto.response.RecentActivityResponse;
import com.example.myapplms.data.remote.dto.response.TaskItemResponse;
import com.example.myapplms.data.remote.dto.response.WeeklyActivityResponse;
import com.example.myapplms.data.repository.CourseRepository;
import com.example.myapplms.data.repository.MediaRepository;
import com.example.myapplms.data.repository.TeacherRepository;
import com.example.myapplms.databinding.FragmentTeacherHomeBinding;
import com.example.myapplms.model.Course;
import com.example.myapplms.ui.base.BaseFragment;
import com.example.myapplms.ui.explore.TeacherCourseViewModel;
import com.example.myapplms.ui.explore.TeacherCourseViewModelFactory;
import com.example.myapplms.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TeacherHomeFragment extends BaseFragment<FragmentTeacherHomeBinding> {

    private TeacherCourseViewModel courseViewModel;
    private TeacherCourseAdapter courseAdapter;
    private TeacherHomeViewModel dashboardViewModel;

    private final List<Course> myCourseList = new ArrayList<>();
    private final List<RecentActivityResponse> activityList = new ArrayList<>();
    private final List<TaskItemResponse> taskList = new ArrayList<>();

    private RecentActivityAdapter activityAdapter;
    private TaskAdapter taskAdapter;

    private boolean coursesLoaded = false;
    private Integer teacherId;

    private boolean isActivitiesLoading = false;
    private boolean isCoursesLoading = false;
    private int currentTab = 0;

    @NonNull
    @Override
    protected FragmentTeacherHomeBinding inflateBinding(@NonNull LayoutInflater inflater,
                                                        @Nullable ViewGroup container) {
        return FragmentTeacherHomeBinding.inflate(inflater, container, false);
    }

    private final ActivityResultLauncher<Intent> courseFormLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && teacherId != null) {
                    courseViewModel.loadMyCourses(teacherId);
                }
            });

    @Override
    protected void setupViews() {
        showTab(0);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LMSApplication app = (LMSApplication) requireActivity().getApplication();
        CourseRepository repo = new CourseRepository(app.getRetrofitClient().getApiService());
        MediaRepository mediaRepo = new MediaRepository(
                app.getRetrofitClient().getApiService(),
                new SessionManager(requireContext())
        );

        TeacherRepository teacherRepo = new TeacherRepository(app.getRetrofitClient().getApiService());
        dashboardViewModel = new ViewModelProvider(this,
                new TeacherHomeViewModelFactory(teacherRepo))
                .get(TeacherHomeViewModel.class);

        courseViewModel = new ViewModelProvider(this,
                new TeacherCourseViewModelFactory(repo, mediaRepo))
                .get(TeacherCourseViewModel.class);

        teacherId = new SessionManager(requireContext()).getTeacherId();

        // ── Adapters ──────────────────────────────────────────────
        courseAdapter = new TeacherCourseAdapter(
                myCourseList,
                course -> {
                    Intent intent = new Intent(getContext(), CourseFormActivity.class);
                    intent.putExtra(CourseFormActivity.EXTRA_COURSE_ID,          course.id);
                    intent.putExtra(CourseFormActivity.EXTRA_COURSE_TITLE,       course.title);
                    intent.putExtra(CourseFormActivity.EXTRA_COURSE_DESCRIPTION, course.description);
                    intent.putExtra(CourseFormActivity.EXTRA_COURSE_PRICE,       course.priceText);
                    intent.putExtra(CourseFormActivity.EXTRA_COURSE_IMAGE_URL,   course.imageUrl != null ? course.imageUrl : "");
                    intent.putExtra(CourseFormActivity.EXTRA_COURSE_CATEGORY,    course.category != null ? course.category : "");
                    courseFormLauncher.launch(intent);
                },
                course -> showDeleteDialog(course),
                course -> showRestoreDialog(course)
        );

        // RecyclerView courses
        RecyclerView rvCourses = new RecyclerView(requireContext());
        rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCourses.setAdapter(courseAdapter);
        getBinding().layoutMyCourses.addView(rvCourses);

        // RecyclerView hoạt động gần đây
        activityAdapter = new RecentActivityAdapter(activityList);
        getBinding().rvRecentActivities.setLayoutManager(new LinearLayoutManager(getContext()));
        getBinding().rvRecentActivities.setAdapter(activityAdapter);
        getBinding().rvRecentActivities.setNestedScrollingEnabled(false);

        // RecyclerView nhiệm vụ
        taskAdapter = new TaskAdapter(taskList);
        getBinding().rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        getBinding().rvTasks.setAdapter(taskAdapter);
        getBinding().rvTasks.setNestedScrollingEnabled(false);

        // ── Observe & Load ────────────────────────────────────────
        observeMyCourses();
        observeDashboardData();
        observeActivitiesPaged();
        observeCoursesPaged();
        observeDeleteResult();
        observeRestoreResult();

        if (teacherId != null) {
            dashboardViewModel.loadAllDashboard();
        }
    }

    @Override
    protected void setupListeners() {
        getBinding().tabOverview.setOnClickListener(v -> showTab(0));
        getBinding().tabMyCourses.setOnClickListener(v -> {
            showTab(1);
            // Load trang đầu tiên khoá học khi vào tab này
            if (!coursesLoaded) {
                coursesLoaded = true;
                dashboardViewModel.loadCoursesPage(0);
            }
        });
        getBinding().tabStudents.setOnClickListener(v -> showTab(2));

        getBinding().btnCreateCourseHome.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CourseFormActivity.class);
            courseFormLauncher.launch(intent);
        });

        // ── Cuộn vô tận (Infinite Scrolling) ─────────────────────
        getBinding().scrollView.setOnScrollChangeListener((androidx.core.widget.NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > 0 && scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                // Kéo đến cuối
                if (currentTab == 0) { // Tab Tổng quan (Hoạt động gần đây)
                    if (!isActivitiesLoading && dashboardViewModel.getActivitiesPage() < dashboardViewModel.getActivitiesTotalPages() - 1) {
                        dashboardViewModel.nextActivitiesPage();
                    }
                } else if (currentTab == 1) { // Tab Khoá học
                    if (!isCoursesLoading && dashboardViewModel.getCoursesPage() < dashboardViewModel.getCoursesTotalPages() - 1) {
                        dashboardViewModel.nextCoursesPage();
                    }
                }
            }
        });
    }

    // ════════════════════════════════════════════════════════════
    //  Tab switching
    // ════════════════════════════════════════════════════════════
    private void showTab(int index) {
        int colorSelected   = 0xFF6C63FF;
        int colorUnselected = 0xFF888888;

        getBinding().tabOverview.setTextColor(colorUnselected);
        getBinding().tabMyCourses.setTextColor(colorUnselected);
        getBinding().tabStudents.setTextColor(colorUnselected);

        getBinding().tabOverview.setBackgroundResource(0);
        getBinding().tabMyCourses.setBackgroundResource(0);
        getBinding().tabStudents.setBackgroundResource(0);

        getBinding().layoutOverview.setVisibility(View.GONE);
        getBinding().layoutMyCourses.setVisibility(View.GONE);
        getBinding().layoutStudents.setVisibility(View.GONE);

        getBinding().tabOverview.setTypeface(null, Typeface.NORMAL);
        getBinding().tabMyCourses.setTypeface(null, Typeface.NORMAL);
        getBinding().tabStudents.setTypeface(null, Typeface.NORMAL);

        currentTab = index;

        switch (index) {
            case 0:
                getBinding().tabOverview.setTextColor(colorSelected);
                getBinding().tabOverview.setBackgroundResource(R.drawable.bg_tab_selected);
                getBinding().tabOverview.setTypeface(null, Typeface.BOLD);
                getBinding().layoutOverview.setVisibility(View.VISIBLE);
                break;
            case 1:
                getBinding().tabMyCourses.setTextColor(colorSelected);
                getBinding().tabMyCourses.setBackgroundResource(R.drawable.bg_tab_selected);
                getBinding().tabMyCourses.setTypeface(null, Typeface.BOLD);
                getBinding().layoutMyCourses.setVisibility(View.VISIBLE);
                break;
            case 2:
                getBinding().tabStudents.setTextColor(colorSelected);
                getBinding().tabStudents.setBackgroundResource(R.drawable.bg_tab_selected);
                getBinding().tabStudents.setTypeface(null, Typeface.BOLD);
                getBinding().layoutStudents.setVisibility(View.VISIBLE);
                break;
        }
    }

    // ════════════════════════════════════════════════════════════
    //  Observers
    // ════════════════════════════════════════════════════════════
    private void observeDashboardData() {

        // 1. Overview → 4 stats cards + tên GV
        dashboardViewModel.overview.observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            if (result.data != null) {
                getBinding().tvStatCourses.setText(String.valueOf(result.data.totalActiveCourses));
                getBinding().tvStatStudents.setText(String.valueOf(result.data.totalStudents));
                getBinding().tvStatPending.setText(String.valueOf(result.data.pendingGrading));
                getBinding().tvStatAvgScore.setText(
                        String.format(Locale.US, "%.1f", result.data.avgScore));
                if (result.data.teacherName != null) {
                    getBinding().tvTeacherName.setText("Giảng viên " + result.data.teacherName);
                    // Lấy 2 chữ cái đầu làm initials
                    String[] parts = result.data.teacherName.trim().split("\\s+");
                    String initials = parts.length >= 2
                            ? String.valueOf(parts[0].charAt(0)) + String.valueOf(parts[parts.length - 1].charAt(0))
                            : result.data.teacherName.substring(0, Math.min(2, result.data.teacherName.length()));
                    String imageUrl = new SessionManager(requireContext()).getImageUrl();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_person)
                                .circleCrop()
                                .into(getBinding().tvTeacherInitials);
                    }
                }
            }
            if (result.message != null && !result.message.isEmpty()) {
                // Không toast để tránh spam, chỉ log
            }
        });

        // 2. Recent activities
        dashboardViewModel.recentActivities.observe(getViewLifecycleOwner(), result -> {
            if (result == null || result.data == null) return;
            activityList.clear();
            activityList.addAll(result.data);
            activityAdapter.notifyDataSetChanged();
        });

        // 3. Weekly activity → vẽ bar chart
        dashboardViewModel.weeklyActivity.observe(getViewLifecycleOwner(), result -> {
            if (result == null || result.data == null) return;
            renderBarChart(result.data);
        });

        // 4. Tasks
        dashboardViewModel.tasks.observe(getViewLifecycleOwner(), result -> {
            if (result == null || result.data == null) return;
            taskList.clear();
            if (result.data.tasks != null) {
                taskList.addAll(result.data.tasks);
            }
            taskAdapter.notifyDataSetChanged();

            // Code cũ của bạn: Cập nhật text
            getBinding().tvTaskCount.setText(String.valueOf(result.data.pendingCount));

            // THÊM DÒNG NÀY: Ẩn badge nếu không có task nào, hiện nếu task > 0
            getBinding().tvTaskCount.setVisibility(result.data.pendingCount > 0 ? View.VISIBLE : View.GONE);
        });
    }

    // ════════════════════════════════════════════════════════════
    //  Bar chart (vẽ bằng View thuần — không cần thư viện)
    // ════════════════════════════════════════════════════════════
    private void renderBarChart(WeeklyActivityResponse data) {
        if (data.dailySubmissions == null || data.dailySubmissions.isEmpty()) return;

        LinearLayout chartContainer = getBinding().chartBars;
        chartContainer.removeAllViews();

        long maxVal = 1;
        for (WeeklyActivityResponse.DailyCount v : data.dailySubmissions) if (v.count > maxVal) maxVal = v.count;

        // Tìm ngày có giá trị cao nhất để tô màu highlight
        int maxIdx = 0;
        for (int i = 1; i < data.dailySubmissions.size(); i++) {
            if (data.dailySubmissions.get(i).count > data.dailySubmissions.get(maxIdx).count) maxIdx = i;
        }

        int chartHeightPx = (int) (100 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < data.dailySubmissions.size(); i++) {
            long val = data.dailySubmissions.get(i).count;

            // 1. Wrapper column (Cột bao ngoài)
            LinearLayout col = new LinearLayout(requireContext());
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams colParams = new LinearLayout.LayoutParams(
                    0, chartHeightPx, 1f);
            colParams.setMarginStart(4);
            colParams.setMarginEnd(4);
            col.setLayoutParams(colParams);

            // 2. Tạo TextView để hiện con số trên đỉnh cột
            TextView tvValue = new TextView(requireContext());
            tvValue.setText(String.valueOf(val));
            tvValue.setTextSize(10f); // Chữ nhỏ vừa phải
            tvValue.setGravity(Gravity.CENTER);


            // Nếu là ngày cao nhất (maxIdx) thì tô chữ màu tím đậm, còn lại màu xám
            if (i == maxIdx && val > 0) {
                tvValue.setTextColor(0xFF6C63FF);
                tvValue.setTypeface(null, Typeface.BOLD);
            } else {
                tvValue.setTextColor(0xFFAAAAAA);
            }

            // (Tùy chọn) Ẩn số 0 cho đồ thị đỡ rối mắt, nếu thích hiện thì bỏ dòng if này đi
            if (val == 0) {
                tvValue.setVisibility(View.INVISIBLE);
            }

            // 3. Tạo Cột bar (Thanh màu)
            View bar = new View(requireContext());
            int barHeightPx = maxVal == 0 ? 4 : (int) ((float) val / maxVal * (chartHeightPx - 32)); // Trừ hao thêm 25px để nhường chỗ cho text bên trên
            barHeightPx = Math.max(barHeightPx, 8); // min height

            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                    (int)(20 * getResources().getDisplayMetrics().density), barHeightPx);

            // Cách số một chút cho đẹp
            barParams.topMargin = 4;
            bar.setLayoutParams(barParams);

            // Màu: highlight ngày cao nhất bằng tím đậm, còn lại màu nhạt
            bar.setBackgroundResource(i == maxIdx
                    ? R.drawable.bg_chart_bar_2
                    : R.drawable.bg_step_inactive_2);

            // 4. Nhét Text và Bar vào Cột bao ngoài (Thứ tự rất quan trọng: Text trước, Bar sau)
            col.addView(tvValue);
            col.addView(bar);

            // 5. Nhét toàn bộ vào Khung đồ thị
            chartContainer.addView(col);
        }

        // Cập nhật badge %
        String sign = data.weeklyChangePercent >= 0 ? "+" : "";
        getBinding().tvWeeklyPercent.setText(sign + data.weeklyChangePercent + "%");

        // Nếu âm, đổi màu đỏ
        if (data.weeklyChangePercent < 0) {
            getBinding().tvWeeklyPercent.setTextColor(0xFFE74C3C);
            getBinding().cardWeeklyPercent.setCardBackgroundColor(0xFFFFF0F0);
        } else {
            getBinding().tvWeeklyPercent.setTextColor(0xFF2ECC71);
            getBinding().cardWeeklyPercent.setCardBackgroundColor(0xFFF0FFF8);
        }
    }

    // ════════════════════════════════════════════════════════════
    //  Pagination Observers
    // ════════════════════════════════════════════════════════════

    /** Lắng nghe phân trang hoạt động gần đây */
    private void observeActivitiesPaged() {
        dashboardViewModel.activitiesPaged.observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    isActivitiesLoading = true;
                    break;
                case SUCCESS:
                    isActivitiesLoading = false;
                    if (result.data != null) {
                        PagedResponse<RecentActivityResponse> paged = result.data;
                        if (paged.page == 0) {
                            activityList.clear();
                        }
                        if (paged.content != null) {
                            activityList.addAll(paged.content);
                        }
                        activityAdapter.notifyDataSetChanged();
                    }
                    break;
                case ERROR:
                    isActivitiesLoading = false;
                    break;
            }
        });
    }

    /** Lắng nghe phân trang khoá học */
    private void observeCoursesPaged() {
        dashboardViewModel.coursesPaged.observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    isCoursesLoading = true;
                    break;
                case SUCCESS:
                    isCoursesLoading = false;
                    if (result.data != null) {
                        PagedResponse<CourseResponse> paged = result.data;
                        if (paged.page == 0) {
                            myCourseList.clear();
                        }
                        if (paged.content != null) {
                            for (CourseResponse dto : paged.content) {
                                myCourseList.add(Course.fromResponse(dto));
                            }
                        }
                        courseAdapter.notifyDataSetChanged();
                    }
                    break;
                case ERROR:
                    isCoursesLoading = false;
                    break;
            }
        });
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

    // ── Observe kết quả xóa mềm ──────────────────────────────
    private void observeDeleteResult() {
        courseViewModel.deleteResult.observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    break;
                case SUCCESS:
                    showToast("Xóa thành công! Sinh viên đã nhận thông báo.");
                    dashboardViewModel.loadCoursesPage(0); // Load lại trang 1
                    loadMyCourses(); // Reload data immediately cho courseViewModel
                    // Đặt lại state để không bị trigger lại khi xoay màn hình
                    courseViewModel.clearDeleteResult();
                    break;
                case ERROR:
                    showToast("Lỗi: " + result.message);
                    break;
            }
        });
    }

    // ── Observe kết quả khôi phục ────────────────────────────
    private void observeRestoreResult() {
        courseViewModel.restoreResult.observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    break;
                case SUCCESS:
                    showToast("Khôi phục thành công! Sinh viên đã nhận thông báo.");
                    dashboardViewModel.loadCoursesPage(0); // Load lại trang 1
                    loadMyCourses(); // Reload data immediately cho courseViewModel
                    // Đặt lại state
                    courseViewModel.clearRestoreResult();
                    break;
                case ERROR:
                    showToast("Lỗi: " + result.message);
                    break;
            }
        });
    }

    // ── Dialog xác nhận xóa mềm ──────────────────────────────
    private void showDeleteDialog(com.example.myapplms.model.Course course) {
        if (getContext() == null) return;

        EditText etReason = new EditText(getContext());
        etReason.setHint("Nhập lý do xóa khóa học...");
        etReason.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        etReason.setMaxLines(3);

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
                .setMessage("Bạn muốn xóa \"" + course.title + "\"?\n\nSinh viên đã mua sẽ nhận thông báo.")
                .setView(container)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    String reason = etReason.getText().toString().trim();
                    if (reason.isEmpty()) reason = "Không dùng nữa";
                    SessionManager session = new SessionManager(requireContext());
                    String deletedBy = "Teacher_" + (session.getTeacherId() != null ? session.getTeacherId() : "Unknown");
                    courseViewModel.deleteCourse(course.id, deletedBy, reason);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ── Dialog xác nhận khôi phục ────────────────────────────
    private void showRestoreDialog(com.example.myapplms.model.Course course) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Khôi phục khóa học")
                .setMessage("Bạn muốn khôi phục \"" + course.title + "\"?\n\nSinh viên đã mua sẽ nhận thông báo.")
                .setPositiveButton("Khôi phục", (dialog, which) -> {
                    SessionManager session = new SessionManager(requireContext());
                    String restoredBy = "Teacher_" + (session.getTeacherId() != null ? session.getTeacherId() : "Unknown");
                    courseViewModel.restoreCourse(course.id, restoredBy);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadMyCourses() {
        SessionManager session = new SessionManager(requireContext());
        Integer tid = session.getTeacherId();
        if (tid != null) {
            coursesLoaded = true;
            courseViewModel.loadMyCourses(tid);
        } else {
            showToast("Không tìm thấy thông tin giảng viên");
        }
    }
}