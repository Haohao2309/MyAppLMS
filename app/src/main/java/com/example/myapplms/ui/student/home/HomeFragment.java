package com.example.myapplms.ui.student.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplms.LMSApplication;
import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.response.BannerResponse;
import com.example.myapplms.data.repository.BannerRepository;
import com.example.myapplms.data.repository.CourseRepository;
import com.example.myapplms.data.repository.StudentRepository;
import com.example.myapplms.databinding.FragmentHomeBinding;
import com.example.myapplms.model.Course;
import com.example.myapplms.ui.StudentMainActivity;
import com.example.myapplms.ui.base.BaseFragment;
import com.example.myapplms.ui.student.course_detail.CourseDetailActivity;
import com.example.myapplms.ui.student.home.adapter.BannerAdapter;
import com.example.myapplms.ui.student.home.adapter.ContinueLearningAdapter;
import com.example.myapplms.ui.student.home.adapter.CourseCardAdapter;
import com.example.myapplms.ui.student.home.adapter.RecommendedCourseAdapter;
import com.example.myapplms.ui.student.home.adapter.StatsAdapter;
import com.example.myapplms.utils.Resource;
import com.example.myapplms.utils.SessionManager;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends BaseFragment<FragmentHomeBinding> {

    private HomeViewModel viewModel;
    private SessionManager sessionManager;
    private StatsAdapter statsAdapter;
    private ContinueLearningAdapter continueLearningAdapter;
    private CourseCardAdapter featuredAdapter;
    private RecommendedCourseAdapter recommendedAdapter;

    // Banner
    private BannerAdapter bannerAdapter;
    private int currentBannerCount = 0;

    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (getBinding() == null) return;
            ViewPager2 vpBanners = getBinding().vpBanners;
            if (vpBanners.getAdapter() != null && vpBanners.getAdapter().getItemCount() > 0) {
                int nextItem = (vpBanners.getCurrentItem() + 1) % vpBanners.getAdapter().getItemCount();
                vpBanners.setCurrentItem(nextItem, true);
            }
            sliderHandler.postDelayed(this, 4000);
        }
    };

    @NonNull
    @Override
    protected FragmentHomeBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentHomeBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LMSApplication app = (LMSApplication) requireActivity().getApplication();
        sessionManager = new SessionManager(requireContext());

        CourseRepository repository = new CourseRepository(app.getRetrofitClient().getApiService());
        StudentRepository studentRepository = new StudentRepository(app.getRetrofitClient().getApiService());
        BannerRepository bannerRepository = new BannerRepository(app.getRetrofitClient().getApiService());

        HomeViewModelFactory factory = new HomeViewModelFactory(repository, studentRepository, bannerRepository);
        viewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);

        super.onViewCreated(view, savedInstanceState);

        // Trigger profile, dashboard & banner loading
        viewModel.loadStudentProfile(sessionManager.getStudentId());
        viewModel.loadHomeData();
        viewModel.loadBanners();
    }

    @Override
    protected void setupViews() {
        setupGreeting();
        setupBanners();
        setupStats();
        setupContinueLearning();
        setupFeaturedCourses();
        setupRecommendedCourses();
        setupSwipeRefresh();
        setupActionButtons();
    }

    private void setupStats() {
        statsAdapter = new StatsAdapter();
        getBinding().rvStats.setAdapter(statsAdapter);
    }

    private void setupActionButtons() {
        getBinding().tvSeeAllMyCourses.setOnClickListener(v -> navigateToMyLearning());
        getBinding().tvSeeAllFeatured.setOnClickListener(v -> navigateToTab(R.id.nav_explore));
        getBinding().tvSeeAllRecommended.setOnClickListener(v -> navigateToTab(R.id.nav_explore));

        // Handle Notification click
        getBinding().flNotification.setOnClickListener(v -> navigateToTab(R.id.nav_notifications));

        // Handle Profile click
        getBinding().ivAvatar.setOnClickListener(v -> navigateToTab(R.id.nav_profile));

        getBinding().ivSearchIcon.setOnClickListener(v -> navigateToTab(R.id.nav_explore));
    }

    private void navigateToMyLearning() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, new com.example.myapplms.ui.student.mylearning.MyLearningFragment())
                .addToBackStack("home")
                .commit();
    }

    private void navigateToTab(int navId) {
        navigateToTab(navId, null);
    }

    private void navigateToTab(int navId, Bundle args) {
        if (getActivity() instanceof StudentMainActivity) {
            ((StudentMainActivity) getActivity()).navigateToTab(navId, args);
        }
    }

    private void setupSwipeRefresh() {
        getBinding().swipeRefresh.setOnRefreshListener(() -> {
            viewModel.loadStudentProfile(sessionManager.getStudentId());
            viewModel.refreshData();
        });
        getBinding().btnRetry.setOnClickListener(v -> {
            viewModel.loadStudentProfile(sessionManager.getStudentId());
            viewModel.refreshData();
        });
    }

    private void setupGreeting() {
        if (sessionManager != null) {
            getBinding().tvUserEmail.setText(sessionManager.getKeyEmail());
        }
        getBinding().tvUserName.setText("Learner!");
    }

    // ── Banner từ API ──────────────────────────────────────────
    private void setupBanners() {
        // Ẩn banner section cho đến khi có data thực từ API
        getBinding().vpBanners.setVisibility(View.GONE);
        getBinding().llBannerDots.setVisibility(View.GONE);

        bannerAdapter = new BannerAdapter(this::handleBannerClick);
        getBinding().vpBanners.setAdapter(bannerAdapter);

        getBinding().vpBanners.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateBannerDots(position, currentBannerCount);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 4000);
            }
        });
    }

    /**
     * Xử lý điều hướng theo target_type của banner:
     *  - COURSE  → CourseDetailActivity với courseId
     *  - CATEGORY → tab Explore (có thể mở rộng sau)
     *  - URL     → mở browser/WebView
     */
    private void handleBannerClick(BannerResponse banner) {
        if (banner.targetType == null || banner.targetId == null) return;
        switch (banner.targetType) {
            case "COURSE":
                try {
                    int courseId = Integer.parseInt(banner.targetId);
                    Intent intent = new Intent(requireContext(), CourseDetailActivity.class);
                    intent.putExtra("COURSE_ID", courseId);
                    startActivity(intent);
                } catch (NumberFormatException ignored) { }
                break;

            case "CATEGORY":
                // Navigate đến tab Explore
                // (có thể mở rộng: truyền categoryId để filter)
                navigateToTab(R.id.nav_explore);
                break;

            case "URL":
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(banner.targetId));
                    startActivity(browserIntent);
                } catch (Exception ignored) { }
                break;

            default:
                break;
        }
    }

    private void refreshBannerUI(List<BannerResponse> bannerList) {
        currentBannerCount = bannerList.size();
        bannerAdapter.updateData(bannerList);

        // Hiện banner section khi đã có data
        getBinding().vpBanners.setVisibility(View.VISIBLE);
        getBinding().llBannerDots.setVisibility(currentBannerCount > 1 ? View.VISIBLE : View.GONE);

        setupBannerDots(currentBannerCount);
        // Restart auto-scroll
        sliderHandler.removeCallbacks(sliderRunnable);
        if (currentBannerCount > 1) {
            sliderHandler.postDelayed(sliderRunnable, 4000);
        }
    }

    private void setupBannerDots(int count) {
        LinearLayout dotsLayout = getBinding().llBannerDots;
        dotsLayout.removeAllViews();
        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dot.setImageResource(R.drawable.bg_notification_dot);
            dotsLayout.addView(dot);
        }
        updateBannerDots(0, count);
    }

    private void updateBannerDots(int position, int count) {
        LinearLayout dotsLayout = getBinding().llBannerDots;
        for (int i = 0; i < count; i++) {
            ImageView dot = (ImageView) dotsLayout.getChildAt(i);
            if (dot != null) {
                if (i == position) {
                    dot.setImageTintList(android.content.res.ColorStateList.valueOf(
                            ContextCompat.getColor(requireContext(), R.color.primary)));
                    dot.setScaleX(1.2f);
                    dot.setScaleY(1.2f);
                } else {
                    dot.setImageTintList(android.content.res.ColorStateList.valueOf(
                            ContextCompat.getColor(requireContext(), R.color.text_secondary)));
                    dot.setScaleX(0.8f);
                    dot.setScaleY(0.8f);
                }
            }
        }
    }

    private void setupContinueLearning() {
        continueLearningAdapter = new ContinueLearningAdapter(this::openCourseDetail);
        getBinding().rvContinueLearning.setAdapter(continueLearningAdapter);
    }

    private void setupFeaturedCourses() {
        featuredAdapter = new CourseCardAdapter(this::openCourseDetail);
        getBinding().rvFeaturedCourses.setAdapter(featuredAdapter);
    }

    private void setupRecommendedCourses() {
        recommendedAdapter = new RecommendedCourseAdapter(this::openCourseDetail);
        getBinding().rvRecommended.setAdapter(recommendedAdapter);
    }

    private void openCourseDetail(Course course) {
        Intent intent = new Intent(requireContext(), CourseDetailActivity.class);
        intent.putExtra("COURSE_ID", course.id);
        startActivity(intent);
    }

    @Override
    protected void observeViewModel() {
        viewModel.getDashboardData().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            boolean isLoading = resource.status == Resource.Status.LOADING;
            getBinding().swipeRefresh.setRefreshing(isLoading);

            if (resource.status == Resource.Status.SUCCESS) {
                getBinding().llMainContent.setVisibility(View.VISIBLE);
                getBinding().llErrorState.setVisibility(View.GONE);
            } else if (resource.status == Resource.Status.ERROR) {
                getBinding().llMainContent.setVisibility(View.GONE);
                getBinding().llErrorState.setVisibility(View.VISIBLE);
                getBinding().tvErrorMessage.setText(resource.message != null ? resource.message : "An error occurred");
            }
        });

        viewModel.studentProfile.observe(getViewLifecycleOwner(), result -> {
            if (result != null && result.status == Resource.Status.SUCCESS && result.data != null) {
                getBinding().tvUserName.setText(result.data.getFullName());
            }
        });

        viewModel.achievements.observe(getViewLifecycleOwner(), result -> {
            if (result != null && result.status == Resource.Status.SUCCESS && result.data != null) {
                statsAdapter.updateData(result.data);
            }
        });

        viewModel.featuredCourses.observe(getViewLifecycleOwner(), result -> {
            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                featuredAdapter.updateData(result.data);
            }
        });

        viewModel.recommendedCourses.observe(getViewLifecycleOwner(), result -> {
            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                recommendedAdapter.updateData(result.data);
            }
        });

        viewModel.continueLearning.observe(getViewLifecycleOwner(), result -> {
            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                if (result.data.isEmpty()) {
                    getBinding().rvContinueLearning.setVisibility(View.GONE);
                    getBinding().llContinueLearningEmpty.setVisibility(View.VISIBLE);
                } else {
                    getBinding().rvContinueLearning.setVisibility(View.VISIBLE);
                    getBinding().llContinueLearningEmpty.setVisibility(View.GONE);
                    continueLearningAdapter.updateData(result.data);
                }
            }
        });

        // ── Banners từ API ─────────────────────────────────────
        viewModel.banners.observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            if (result.status == Resource.Status.SUCCESS && result.data != null && !result.data.isEmpty()) {
                refreshBannerUI(result.data);
            } else if (result.status == Resource.Status.ERROR || result.status == Resource.Status.SUCCESS) {
                // Lỗi hoặc empty list → ẩn hoàn toàn để không chiếm chỗ trống
                getBinding().vpBanners.setVisibility(View.GONE);
                getBinding().llBannerDots.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentBannerCount > 1) {
            sliderHandler.postDelayed(sliderRunnable, 4000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }
}
