package com.example.myapplms.ui.explore;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplms.CourseAdapter;
import com.example.myapplms.LMSApplication;
import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.response.CategoryResponse;
import com.example.myapplms.data.remote.dto.response.CourseResponse;
import com.example.myapplms.data.remote.dto.response.PagedResponse;
import com.example.myapplms.data.repository.CategoryRepository;
import com.example.myapplms.data.repository.CourseRepository;
import com.example.myapplms.databinding.FragmentExploreListCourseBinding;
import com.example.myapplms.model.Course;
import com.example.myapplms.ui.base.BaseFragment;
import com.example.myapplms.ui.course.CategoryViewModel;
import com.example.myapplms.ui.course.CategoryViewModelFactory;
import com.example.myapplms.ui.student.course_detail.CourseDetailActivity;
import com.example.myapplms.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExploreListCourseFragment extends BaseFragment<FragmentExploreListCourseBinding> {

    private ExploreViewModel viewModel;
    private CategoryViewModel categoryViewModel; // Khai báo thêm CategoryViewModel

    private CourseAdapter adapter;
    private final List<Course> courseList = new ArrayList<>();
    private SessionManager sessionManager;
    private Integer teacherId;

    // Biến lưu trữ TextView danh mục đang được chọn
    private TextView currentSelectedCategoryTv = null;

    private boolean isLoading = false;

    @NonNull
    @Override
    protected FragmentExploreListCourseBinding inflateBinding(@NonNull LayoutInflater inflater,
                                                              @Nullable ViewGroup container) {
        return FragmentExploreListCourseBinding.inflate(inflater, container, false);
    }

    @Override
    protected void setupViews() {
        adapter = new CourseAdapter(courseList, course -> {
            Intent intent = new Intent(requireContext(), CourseDetailActivity.class);
            intent.putExtra("COURSE_ID", course.id);
            startActivity(intent);
        });

        getBinding().rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        getBinding().rvCourses.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        teacherId = sessionManager.getTeacherId();
        LMSApplication app = (LMSApplication) requireActivity().getApplication();

        // 1. Khởi tạo Repository & ViewModel cho Course
        CourseRepository repository = new CourseRepository(app.getRetrofitClient().getApiService());
        viewModel = new ViewModelProvider(this, new ExploreViewModelFactory(repository))
                .get(ExploreViewModel.class);

        // 2. Khởi tạo Repository & ViewModel cho Category
        CategoryRepository catRepo = new CategoryRepository(app.getRetrofitClient().getApiService());
        categoryViewModel = new ViewModelProvider(this, new CategoryViewModelFactory(catRepo))
                .get(CategoryViewModel.class);

        // 3. Observe Data
        observeCourses();
        observeCategories();

        // 4. Load Data từ API
        categoryViewModel.loadCategories(); // Tải danh mục từ API

        // Load trang đầu tiên (phân trang server-side)
        boolean isTeacher = (teacherId != null);
        viewModel.loadFirstPage(isTeacher);

        // Observe phân trang
        observeCoursePaged();
    }

    private void observeCourses() {
        viewModel.getCourses().observe(getViewLifecycleOwner(), result -> {
            switch (result.status) {
                case LOADING:
                    break;
                case SUCCESS:
                    if (result.data != null) {
                        adapter.updateData(result.data);
                        updateCourseCountText();
                    }
                    break;
                case ERROR:
                    Toast.makeText(getContext(), result.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    /** Observer mới: phân trang server-side */
    private void observeCoursePaged() {
        viewModel.coursesPaged.observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    isLoading = true;
                    break;
                case SUCCESS:
                    isLoading = false;
                    if (result.data != null) {
                        PagedResponse<CourseResponse> paged = result.data;

                        // Nếu là trang đầu tiên (page == 0), xoá danh sách cũ
                        if (paged.page == 0) {
                            courseList.clear();
                        }

                        // Parse data và thêm vào cuối danh sách
                        if (paged.content != null) {
                            for (CourseResponse dto : paged.content) {
                                courseList.add(com.example.myapplms.model.Course.fromResponse(dto));
                            }
                        }

                        // Cập nhật Adapter
                        adapter.updateData(courseList);
                        updateCourseCountText();
                    }
                    break;
                case ERROR:
                    isLoading = false;
                    Toast.makeText(getContext(), result.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    // ── HÀM MỚI: Lắng nghe và vẽ danh mục lên ScrollView ──
    private void observeCategories() {
        categoryViewModel.categories.observe(getViewLifecycleOwner(), result -> {
            switch (result.status) {
                case LOADING:
                    break;
                case SUCCESS:
                    if (result.data != null) {
                        renderCategoriesToScrollView(result.data);
                    }
                    break;
                case ERROR:
                    Toast.makeText(getContext(), result.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void renderCategoriesToScrollView(List<CategoryResponse> apiCategories) {
        LinearLayout layoutCategories = getBinding().layoutCategories;
        layoutCategories.removeAllViews(); // Xóa các TextView (All, Dev) code cứng trong XML

        // 1. Tạo nút "All" đầu tiên
        TextView tvAll = createCategoryChip("All", true);
        layoutCategories.addView(tvAll);
        currentSelectedCategoryTv = tvAll;

        // 2. Lặp qua data API để tạo các nút tiếp theo
        for (CategoryResponse cat : apiCategories) {
            if (cat.name != null) {
                TextView tv = createCategoryChip(cat.name, false);
                layoutCategories.addView(tv);
            }
        }
    }

    private TextView createCategoryChip(String categoryName, boolean isSelected) {
        TextView tv = new TextView(requireContext());

        // Set Layout Params (MarginStart 8dp cho các nút sau nút All)
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        if (!categoryName.equals("All")) {
            params.setMarginStart(dpToPx(8));
        }
        tv.setLayoutParams(params);

        // Set Padding (16dp ngang, 8dp dọc)
        int padH = dpToPx(16);
        int padV = dpToPx(8);
        tv.setPadding(padH, padV, padH, padV);

        tv.setText(categoryName);

        // Đặt màu mặc định
        setCategoryChipState(tv, isSelected);

        // Bắt sự kiện Click
        tv.setOnClickListener(v -> {
            // Reset màu nút cũ
            if (currentSelectedCategoryTv != null) {
                setCategoryChipState(currentSelectedCategoryTv, false);
            }
            // Kích hoạt màu nút mới
            setCategoryChipState(tv, true);
            currentSelectedCategoryTv = tv;

            // Lọc theo Category qua Server
            viewModel.applyCategory(categoryName);
        });

        return tv;
    }

    private void setCategoryChipState(TextView tv, boolean isActive) {
        if (isActive) {
            tv.setBackgroundResource(R.drawable.bg_chip_selected);
            tv.setTextColor(getResources().getColor(R.color.white, null));
        } else {
            tv.setBackgroundResource(R.drawable.bg_chip_unselected);
            tv.setTextColor(getResources().getColor(R.color.text_secondary, null));
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void updateCourseCountText() {
        getBinding().tvCourseCount.setText(
                String.format(Locale.getDefault(), "%d courses found", adapter.getItemCount())
        );
    }

    @Override
    protected void setupListeners() {
        getBinding().searchViewCourse.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Remove debounce to keep it simple, just apply search when typed
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                viewModel.applySearch(s.toString());
            }
        });
        getBinding().btnFilter.setOnClickListener(v -> {
            FilterBottomSheetFragment filterSheet = new FilterBottomSheetFragment();

            filterSheet.setFilterListener((level, price, rating) -> {
                viewModel.applyFilters(price, rating);
            });

            filterSheet.show(getParentFragmentManager(), "FilterBottomSheet");
        });

        // ── Cuộn vô tận (Infinite Scrolling) ─────────────────────
        getBinding().rvCourses.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull androidx.recyclerview.widget.RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) { // Đang cuộn xuống
                    if (!recyclerView.canScrollVertically(1)) { // Kéo đến cuối cùng của danh sách
                        if (!isLoading && viewModel.getCurrentPage() < viewModel.getTotalPages() - 1) {
                            viewModel.nextPage();
                        }
                    }
                }
            }
        });
    }
}