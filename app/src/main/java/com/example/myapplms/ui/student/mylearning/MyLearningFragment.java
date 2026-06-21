package com.example.myapplms.ui.student.mylearning;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplms.LMSApplication;
import com.example.myapplms.R;
import com.example.myapplms.data.repository.CourseRepository;
import com.example.myapplms.databinding.FragmentMyLearningBinding;
import com.example.myapplms.model.Course;
import com.example.myapplms.ui.base.BaseFragment;
import com.example.myapplms.ui.student.course_detail.CourseDetailActivity;
import com.example.myapplms.ui.student.mylearning.adapter.MyLearningCourseAdapter;
import com.example.myapplms.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class MyLearningFragment extends BaseFragment<FragmentMyLearningBinding> {

    private MyLearningViewModel viewModel;
    private MyLearningCourseAdapter adapter;

    @NonNull
    @Override
    protected FragmentMyLearningBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentMyLearningBinding.inflate(inflater, container, false);
    }

    private enum TabState { IN_PROGRESS, COMPLETED }
    private TabState currentTab = TabState.IN_PROGRESS;
    private List<Course> allCourses = new ArrayList<>();

    @Override
    protected void setupViews() {
        super.setupViews();
        initViewModel();
        setupRecyclerView();
        setupExploreButton();
        setupTabClickListeners();
    }

    private void setupExploreButton() {
        getBinding().btnExploreMore.setOnClickListener(v -> {
            if (getActivity() instanceof com.example.myapplms.ui.StudentMainActivity) {
                com.google.android.material.bottomnavigation.BottomNavigationView nav = 
                    getActivity().findViewById(R.id.bottom_navigation);
                nav.setSelectedItemId(R.id.nav_explore);
            }
        });
    }

    private void setupTabClickListeners() {
        getBinding().tabInProgress.setOnClickListener(v -> updateTabUI(TabState.IN_PROGRESS));
        getBinding().tabCompleted.setOnClickListener(v -> updateTabUI(TabState.COMPLETED));
    }

    private void initViewModel() {
        LMSApplication app = (LMSApplication) requireActivity().getApplication();
        CourseRepository repository = new CourseRepository(app.getRetrofitClient().getApiService());
        viewModel = new ViewModelProvider(this, new MyLearningViewModelFactory(repository))
                .get(MyLearningViewModel.class);
        viewModel.loadMyCourses();
    }

    private void setupRecyclerView() {
        getBinding().rvMyCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyLearningCourseAdapter(new ArrayList<>(), new MyLearningCourseAdapter.OnCourseClickListener() {
            @Override
            public void onCourseClick(Course course) {
                openCourseDetail(course.id);
            }
        });
        getBinding().rvMyCourses.setAdapter(adapter);
    }

    private void openCourseDetail(int courseId) {
        android.content.Intent intent = new android.content.Intent(getActivity(), CourseDetailActivity.class);
        intent.putExtra("COURSE_ID", courseId);
        startActivity(intent);
    }

    private void updateTabUI(TabState tab) {
        currentTab = tab;
        
        // Reset unselected states (Make backgrounds transparent)
        int textSecondaryColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_secondary);
        getBinding().tabInProgress.setBackgroundResource(android.R.color.transparent);
        getBinding().tabInProgress.setTextColor(textSecondaryColor);
        getBinding().tabInProgress.setTypeface(null, android.graphics.Typeface.NORMAL);
        
        getBinding().tabCompleted.setBackgroundResource(android.R.color.transparent);
        getBinding().tabCompleted.setTextColor(textSecondaryColor);
        getBinding().tabCompleted.setTypeface(null, android.graphics.Typeface.NORMAL);
        
        // Set selected state
        int whiteColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white);
        switch (tab) {
            case IN_PROGRESS:
                getBinding().tabInProgress.setBackgroundResource(R.drawable.bg_my_learning_tab_selected);
                getBinding().tabInProgress.setTextColor(whiteColor);
                getBinding().tabInProgress.setTypeface(null, android.graphics.Typeface.BOLD);
                displayCourses(getFilteredCourses(TabState.IN_PROGRESS));
                break;
            case COMPLETED:
                getBinding().tabCompleted.setBackgroundResource(R.drawable.bg_my_learning_tab_selected);
                getBinding().tabCompleted.setTextColor(whiteColor);
                getBinding().tabCompleted.setTypeface(null, android.graphics.Typeface.BOLD);
                displayCourses(getFilteredCourses(TabState.COMPLETED));
                break;
        }
    }

    private List<Course> getFilteredCourses(TabState tab) {
        List<Course> filtered = new ArrayList<>();
        switch (tab) {
            case IN_PROGRESS:
                // Purchase Course tab shows all enrolled courses
                filtered.addAll(allCourses);
                break;
            case COMPLETED:
                for (Course c : allCourses) {
                    if (c.progressPercent >= 100) {
                        filtered.add(c);
                    }
                }
                break;
        }
        return filtered;
    }

    private void displayCourses(List<Course> list) {
        if (list.isEmpty()) {
            getBinding().rvMyCourses.setVisibility(android.view.View.GONE);
            getBinding().llEmptyState.setVisibility(android.view.View.VISIBLE);
        } else {
            getBinding().rvMyCourses.setVisibility(android.view.View.VISIBLE);
            getBinding().llEmptyState.setVisibility(android.view.View.GONE);
            adapter.updateData(list);
        }
    }



    @Override
    protected void observeViewModel() {
        super.observeViewModel();
        viewModel.myCourses.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    getBinding().pbLoading.setVisibility(android.view.View.VISIBLE);
                    break;
                case SUCCESS:
                    getBinding().pbLoading.setVisibility(android.view.View.GONE);
                    allCourses.clear();
                    if (resource.data != null) {
                        allCourses.addAll(resource.data);
                    }
                    
                    // Count counts
                    int inProgressCount = 0;
                    int completedCount = 0;
                    for (Course c : allCourses) {
                        if (c.progressPercent < 100) {
                            inProgressCount++;
                        } else {
                            completedCount++;
                        }
                    }
                    // Update Tab Texts
                    getBinding().tabInProgress.setText("Purchase Course");
                    getBinding().tabCompleted.setText("Completed");

                    // Update UI list to current tab
                    updateTabUI(currentTab);
                    
                    getBinding().tvEnrolledCount.setText(allCourses.size() + " courses enrolled");
                    break;
                case ERROR:
                    getBinding().pbLoading.setVisibility(android.view.View.GONE);
                    showToast(resource.message);
                    break;
            }
        });
    }
}
