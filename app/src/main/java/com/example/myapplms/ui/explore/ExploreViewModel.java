package com.example.myapplms.ui.explore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplms.data.remote.dto.response.CourseResponse;
import com.example.myapplms.data.remote.dto.response.PagedResponse;
import com.example.myapplms.data.repository.CourseRepository;
import com.example.myapplms.model.Course;
import com.example.myapplms.utils.Resource;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExploreViewModel extends ViewModel {

    private final CourseRepository courseRepository;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    private static final int PAGE_SIZE = 8;

    // ── QUAN TRỌNG: dùng MutableLiveData cố định, không gán lại ──
    private final MutableLiveData<Resource<List<Course>>> _courses = new MutableLiveData<>();
    public final LiveData<Resource<List<Course>>> courses = _courses;

    // LiveData cho Category
    private final MutableLiveData<Resource<List<String>>> _categories = new MutableLiveData<>();
    public final LiveData<Resource<List<String>>> categories = _categories;

    // ── Phân trang (server-side) ──────────────────────────────────
    private final MutableLiveData<Resource<PagedResponse<CourseResponse>>> _coursesPaged = new MutableLiveData<>();
    public final LiveData<Resource<PagedResponse<CourseResponse>>> coursesPaged = _coursesPaged;

    private int _currentPage = 0;
    private int _totalPages  = 1;
    private boolean _isTeacher = false;

    // State cho Filters
    private String _currentSearch = null;
    private String _currentCategory = "All";
    private String _currentPrice = "All";
    private String _currentRating = "Any";

    public ExploreViewModel(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    // ── Load tất cả (legacy, không phân trang) ───────────────────
    public void loadCourses() {
        courseRepository.getCourses().observeForever(result -> _courses.setValue(result));
    }

    public void loadCourses(Integer teacherId) {
        courseRepository.getCoursesByTeacherId(teacherId).observeForever(result -> _courses.setValue(result));
    }

    // Giữ lại getter cũ để không phải sửa Fragment
    public LiveData<Resource<List<Course>>> getCourses() {
        return _courses;
    }

    // ── Phân trang & Lọc (server-side) ──────────────────────────────────

    public void applySearch(String search) {
        this._currentSearch = search;
        loadFirstPage(_isTeacher);
    }

    public void applyCategory(String category) {
        this._currentCategory = category;
        loadFirstPage(_isTeacher);
    }

    public void applyFilters(String price, String rating) {
        this._currentPrice = price;
        this._currentRating = rating;
        loadFirstPage(_isTeacher);
    }

    /**
     * Load trang đầu tiên khi vào màn Explore.
     * @param isTeacher true nếu đang đăng nhập với tài khoản teacher
     */
    public void loadFirstPage(boolean isTeacher) {
        _isTeacher = isTeacher;
        _currentPage = 0;
        loadPageInternal(0);
    }

    public void nextPage() {
        if (_currentPage < _totalPages - 1) {
            loadPageInternal(_currentPage + 1);
        }
    }

    public void prevPage() {
        if (_currentPage > 0) {
            loadPageInternal(_currentPage - 1);
        }
    }

    private void loadPageInternal(int page) {
        _coursesPaged.postValue(Resource.loading());
        executor.execute(() -> {
            Resource<PagedResponse<CourseResponse>> result;
            if (_isTeacher) {
                result = courseRepository.getExploreCoursesPagedTeacher(page, PAGE_SIZE, _currentSearch, _currentCategory, _currentPrice, _currentRating);
            } else {
                result = courseRepository.getExploreCoursesPagedStudent(page, PAGE_SIZE, _currentSearch, _currentCategory, _currentPrice, _currentRating);
            }
            if (result.data != null) {
                _currentPage = result.data.page;
                _totalPages  = result.data.totalPages;
            }
            _coursesPaged.postValue(result);
        });
    }

    public int getCurrentPage()  { return _currentPage; }
    public int getTotalPages()   { return _totalPages; }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }
}