package com.example.myapplms.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplms.LMSApplication;
import com.example.myapplms.R;
import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.response.CourseResponse;
import com.example.myapplms.data.remote.dto.response.EnrollmentStatusResponse;
import com.example.myapplms.data.remote.dto.response.ProgressResponse;
import com.example.myapplms.model.Course;
import com.example.myapplms.ui.explore.ExploreListCourseFragment;
import com.example.myapplms.ui.notification.NotificationsFragment;
import com.example.myapplms.ui.student.course_detail.CourseDetailActivity;
import com.example.myapplms.utils.SessionManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private ProgressBar pbLoading;
    private ImageView ivAvatar;
    private TextView tvUserName;
    private ImageButton btnNotification;
    private View layoutSearchBar;
    private RecyclerView rvContinueLearning, rvFeatured, rvRecommended;
    private View layoutContinueEmpty;
    private Button btnBannerCta;
    private TextView tvBannerTitle, tvBannerCategory;
    private ImageView ivBanner;

    private ContinueLearningAdapter continueLearningAdapter;
    private FeaturedCourseAdapter featuredAdapter;
    private RecommendedCourseAdapter recommendedAdapter;

    private LmsApiService apiService;
    private SessionManager sessionManager;

    // Lưu banner course để xử lý click
    private Course bannerCourse;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LMSApplication app = (LMSApplication) requireActivity().getApplication();
        sessionManager = app.getSessionManager();
        apiService = app.getRetrofitClient().getApiService();

        initViews(view);
        setupRecyclerViews();
        loadUserInfo();
        loadAllCourseData();
    }

    private void initViews(View view) {
        pbLoading         = view.findViewById(R.id.pbLoading);
        ivAvatar          = view.findViewById(R.id.ivAvatar);
        tvUserName        = view.findViewById(R.id.tvUserName);
        btnNotification   = view.findViewById(R.id.btnNotification);
        layoutSearchBar   = view.findViewById(R.id.layoutSearchBar);
        rvContinueLearning = view.findViewById(R.id.rvContinueLearning);
        rvFeatured        = view.findViewById(R.id.rvFeatured);
        rvRecommended     = view.findViewById(R.id.rvRecommended);
        layoutContinueEmpty = view.findViewById(R.id.layoutContinueEmpty);
        btnBannerCta      = view.findViewById(R.id.btnBannerCta);
        tvBannerTitle     = view.findViewById(R.id.tvBannerTitle);
        tvBannerCategory  = view.findViewById(R.id.tvBannerCategory);
        ivBanner          = view.findViewById(R.id.ivBanner);

        TextView tvMyCourses    = view.findViewById(R.id.tvMyCourses);
        TextView tvSeeAllFeatured = view.findViewById(R.id.tvSeeAllFeatured);
        TextView tvExploreAll   = view.findViewById(R.id.tvExploreAll);

        btnNotification.setOnClickListener(v -> navigateTo(new NotificationsFragment()));
        layoutSearchBar.setOnClickListener(v -> navigateTo(new ExploreListCourseFragment()));
        tvMyCourses.setOnClickListener(v -> navigateTo(new ExploreListCourseFragment()));
        tvSeeAllFeatured.setOnClickListener(v -> navigateTo(new ExploreListCourseFragment()));
        tvExploreAll.setOnClickListener(v -> navigateTo(new ExploreListCourseFragment()));
        btnBannerCta.setOnClickListener(v -> {
            if (bannerCourse != null) openCourseDetail(bannerCourse.id);
        });
    }

    private void setupRecyclerViews() {
        // Continue Learning — vertical list
        continueLearningAdapter = new ContinueLearningAdapter(course -> openCourseDetail(course.id));
        rvContinueLearning.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvContinueLearning.setAdapter(continueLearningAdapter);
        rvContinueLearning.setNestedScrollingEnabled(false);

        // Featured Courses — horizontal scroll
        featuredAdapter = new FeaturedCourseAdapter(course -> openCourseDetail(course.id));
        LinearLayoutManager hLayoutManagerFeatured = new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false);
        rvFeatured.setLayoutManager(hLayoutManagerFeatured);
        rvFeatured.setAdapter(featuredAdapter);
        rvFeatured.setNestedScrollingEnabled(false);

        // Recommended — vertical list
        recommendedAdapter = new RecommendedCourseAdapter(course -> openCourseDetail(course.id));
        rvRecommended.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecommended.setAdapter(recommendedAdapter);
        rvRecommended.setNestedScrollingEnabled(false);
    }

    // ── Load user info ────────────────────────────────────────
    private void loadUserInfo() {
        String imageUrl = sessionManager.getImageUrl();
        String email = sessionManager.getKeyEmail();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(ivAvatar);
        }

        if (email != null && !email.isEmpty()) {
            String displayName = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
            tvUserName.setText(displayName);
        }

        Integer studentId = sessionManager.getStudentId();
        if (studentId != null) {
            apiService.getStudentById(studentId).enqueue(new Callback<com.example.myapplms.data.remote.dto.response.StudentResponse>() {
                @Override
                public void onResponse(Call<com.example.myapplms.data.remote.dto.response.StudentResponse> call,
                                       Response<com.example.myapplms.data.remote.dto.response.StudentResponse> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful() && response.body() != null) {
                        com.example.myapplms.data.remote.dto.response.StudentResponse student = response.body();
                        String name = "";
                        if (student.firstName != null) name = student.firstName;
                        if (student.lastName != null) name = (name + " " + student.lastName).trim();
                        if (!name.isEmpty()) {
                            String finalName = name;
                            requireActivity().runOnUiThread(() -> tvUserName.setText(finalName));
                        }
                    }
                }

                @Override
                public void onFailure(Call<com.example.myapplms.data.remote.dto.response.StudentResponse> call, Throwable t) {
                    // Giữ nguyên email hiển thị
                }
            });
        }
    }

    // ── Load tất cả data từ 1 lần gọi getCourses() ───────────
    private void loadAllCourseData() {
        pbLoading.setVisibility(View.VISIBLE);

        apiService.getCourses().enqueue(new Callback<List<CourseResponse>>() {
            @Override
            public void onResponse(Call<List<CourseResponse>> call, Response<List<CourseResponse>> response) {
                if (!isAdded()) return;
                pbLoading.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    showContinueEmpty();
                    return;
                }

                List<CourseResponse> allCourses = response.body();

                // 1. Featured: top 5 theo rating giảm dần
                List<CourseResponse> sorted = new ArrayList<>(allCourses);
                sorted.sort((a, b) -> {
                    double rA = a.averageRating != null ? a.averageRating : 0.0;
                    double rB = b.averageRating != null ? b.averageRating : 0.0;
                    return Double.compare(rB, rA);
                });
                List<Course> featuredList = new ArrayList<>();
                int featuredLimit = Math.min(sorted.size(), 8);
                for (int i = 0; i < featuredLimit; i++) {
                    featuredList.add(Course.fromResponse(sorted.get(i)));
                }
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    rvFeatured.setVisibility(View.VISIBLE);
                    featuredAdapter.setCourses(featuredList);
                });

                // 2. Banner: dùng khóa đầu tiên (top rating)
                if (!sorted.isEmpty()) {
                    bannerCourse = Course.fromResponse(sorted.get(0));
                    requireActivity().runOnUiThread(() -> {
                        if (!isAdded()) return;
                        tvBannerTitle.setText(bannerCourse.title != null
                                ? bannerCourse.title : "Khóa học hot nhất");
                        tvBannerCategory.setText(bannerCourse.category != null
                                ? bannerCourse.category : "Khóa học nổi bật");
                        if (bannerCourse.imageUrl != null && !bannerCourse.imageUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(bannerCourse.imageUrl)
                                    .centerCrop()
                                    .into(ivBanner);
                        }
                    });
                }

                // 3. Recommended: chỉ hiển thị top 5 khóa học
                List<Course> recommendedList = new ArrayList<>();
                int recommendedLimit = Math.min(allCourses.size(), 5);
                for (int i = 0; i < recommendedLimit; i++) {
                    recommendedList.add(Course.fromResponse(allCourses.get(i)));
                }
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    rvRecommended.setVisibility(View.VISIBLE);
                    recommendedAdapter.setCourses(recommendedList);
                });

                // 4. Continue Learning: kiểm tra enrollment + progress
                checkEnrollmentsAndProgress(allCourses);
            }

            @Override
            public void onFailure(Call<List<CourseResponse>> call, Throwable t) {
                if (!isAdded()) return;
                pbLoading.setVisibility(View.GONE);
                showContinueEmpty();
            }
        });
    }

    // ── Continue Learning ─────────────────────────────────────
    private void checkEnrollmentsAndProgress(List<CourseResponse> allCourses) {
        List<ContinueLearningAdapter.ContinueLearningItem> result = new ArrayList<>();
        AtomicInteger pending = new AtomicInteger(allCourses.size());

        if (allCourses.isEmpty()) {
            showContinueEmpty();
            return;
        }

        for (CourseResponse cr : allCourses) {
            int courseId = cr.courseId != null ? cr.courseId : 0;

            apiService.getEnrollmentStatus(courseId).enqueue(new Callback<EnrollmentStatusResponse>() {
                @Override
                public void onResponse(Call<EnrollmentStatusResponse> call,
                                       Response<EnrollmentStatusResponse> response) {
                    if (!isAdded()) return;

                    boolean enrolled = response.isSuccessful()
                            && response.body() != null
                            && response.body().enrolled;

                    if (enrolled) {
                        fetchProgressForCourse(cr, result, pending, allCourses.size());
                    } else {
                        decrementAndUpdate(pending, result, allCourses.size());
                    }
                }

                @Override
                public void onFailure(Call<EnrollmentStatusResponse> call, Throwable t) {
                    if (!isAdded()) return;
                    decrementAndUpdate(pending, result, allCourses.size());
                }
            });
        }
    }

    private void fetchProgressForCourse(CourseResponse cr,
                                        List<ContinueLearningAdapter.ContinueLearningItem> result,
                                        AtomicInteger pending,
                                        int total) {
        int courseId = cr.courseId != null ? cr.courseId : 0;

        apiService.getProgress(courseId).enqueue(new Callback<ProgressResponse>() {
            @Override
            public void onResponse(Call<ProgressResponse> call, Response<ProgressResponse> response) {
                if (!isAdded()) return;

                double progress = 0.0;
                if (response.isSuccessful() && response.body() != null
                        && response.body().overallProgress != null) {
                    progress = response.body().overallProgress;
                }

                // Chỉ thêm khóa chưa hoàn thành (< 100%)
                if (progress < 100.0) {
                    synchronized (result) {
                        result.add(new ContinueLearningAdapter.ContinueLearningItem(
                                Course.fromResponse(cr), progress));
                    }
                }
                decrementAndUpdate(pending, result, total);
            }

            @Override
            public void onFailure(Call<ProgressResponse> call, Throwable t) {
                if (!isAdded()) return;
                synchronized (result) {
                    result.add(new ContinueLearningAdapter.ContinueLearningItem(
                            Course.fromResponse(cr), 0.0));
                }
                decrementAndUpdate(pending, result, total);
            }
        });
    }

    private void decrementAndUpdate(AtomicInteger pending,
                                    List<ContinueLearningAdapter.ContinueLearningItem> result,
                                    int total) {
        if (pending.decrementAndGet() == 0) {
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                if (result.isEmpty()) {
                    showContinueEmpty();
                } else {
                    layoutContinueEmpty.setVisibility(View.GONE);
                    rvContinueLearning.setVisibility(View.VISIBLE);
                    continueLearningAdapter.setItems(result);
                }
            });
        }
    }

    private void showContinueEmpty() {
        rvContinueLearning.setVisibility(View.GONE);
        layoutContinueEmpty.setVisibility(View.VISIBLE);
    }

    // ── Navigation ─────────────────────────────────────────────
    private void openCourseDetail(int courseId) {
        Intent intent = new Intent(requireContext(), CourseDetailActivity.class);
        intent.putExtra("COURSE_ID", courseId);
        startActivity(intent);
    }

    private void navigateTo(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}