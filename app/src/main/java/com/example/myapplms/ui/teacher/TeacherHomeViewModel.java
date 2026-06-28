package com.example.myapplms.ui.teacher;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplms.data.remote.dto.response.CourseResponse;
import com.example.myapplms.data.remote.dto.response.DashboardOverviewResponse;
import com.example.myapplms.data.remote.dto.response.PagedResponse;
import com.example.myapplms.data.remote.dto.response.RecentActivityResponse;
import com.example.myapplms.data.remote.dto.response.TaskItemResponse;
import com.example.myapplms.data.remote.dto.response.TeacherStatsResponse;
import com.example.myapplms.data.remote.dto.response.TeacherTaskResponse;
import com.example.myapplms.data.remote.dto.response.WeeklyActivityResponse;
import com.example.myapplms.data.repository.TeacherRepository;
import com.example.myapplms.utils.Resource;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TeacherHomeViewModel extends ViewModel {

    private final TeacherRepository repository;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private static final int PAGE_SIZE = 8;

    // ── Legacy dashboard LiveData (giữ lại để không break code cũ) ──
    private final MutableLiveData<Resource<TeacherStatsResponse>> _dashboardData = new MutableLiveData<>();
    public final LiveData<Resource<TeacherStatsResponse>> dashboardData = _dashboardData;

    // ── Dashboard v2 LiveData ──────────────────────────────────────
    private final MutableLiveData<Resource<DashboardOverviewResponse>> _overview = new MutableLiveData<>();
    public final LiveData<Resource<DashboardOverviewResponse>> overview = _overview;

    private final MutableLiveData<Resource<List<RecentActivityResponse>>> _recentActivities = new MutableLiveData<>();
    public final LiveData<Resource<List<RecentActivityResponse>>> recentActivities = _recentActivities;

    private final MutableLiveData<Resource<WeeklyActivityResponse>> _weeklyActivity = new MutableLiveData<>();
    public final LiveData<Resource<WeeklyActivityResponse>> weeklyActivity = _weeklyActivity;

    private final MutableLiveData<Resource<TeacherTaskResponse>> _tasks = new MutableLiveData<>();
    public final LiveData<Resource<TeacherTaskResponse>> tasks = _tasks;

    // ── Phân trang: Hoạt động gần đây ─────────────────────────────
    private final MutableLiveData<Resource<PagedResponse<RecentActivityResponse>>> _activitiesPaged = new MutableLiveData<>();
    public final LiveData<Resource<PagedResponse<RecentActivityResponse>>> activitiesPaged = _activitiesPaged;

    private int _activitiesPage = 0;
    private int _activitiesTotalPages = 1;

    // ── Phân trang: Khoá học ──────────────────────────────────────
    private final MutableLiveData<Resource<PagedResponse<CourseResponse>>> _coursesPaged = new MutableLiveData<>();
    public final LiveData<Resource<PagedResponse<CourseResponse>>> coursesPaged = _coursesPaged;

    private int _coursesPage = 0;
    private int _coursesTotalPages = 1;

    public TeacherHomeViewModel(TeacherRepository repository) {
        this.repository = repository;
    }

    /** Legacy — giữ lại */
    public void loadDashboard(Integer teacherId) {
        _dashboardData.postValue(Resource.loading());
        executor.execute(() -> _dashboardData.postValue(repository.getDashboardStats(teacherId)));
    }

    /** Gọi 4 API v2 song song + activities paged */
    public void loadAllDashboard() {
        _overview.postValue(Resource.loading());
        _recentActivities.postValue(Resource.loading());
        _weeklyActivity.postValue(Resource.loading());
        _tasks.postValue(Resource.loading());

        executor.execute(() -> _overview.postValue(repository.getOverview()));
        executor.execute(() -> _weeklyActivity.postValue(repository.getWeeklyActivity()));
        executor.execute(() -> _tasks.postValue(repository.getTasks()));

        // Load trang đầu tiên của recent activities (phân trang)
        loadActivitiesPage(0);
    }

    // ── Phân trang hoạt động gần đây ────────────────────────────────────────

    public void loadActivitiesPage(int page) {
        _activitiesPaged.postValue(Resource.loading());
        executor.execute(() -> {
            Resource<PagedResponse<RecentActivityResponse>> result =
                    repository.getRecentActivitiesPaged(page, PAGE_SIZE);
            if (result.data != null) {
                _activitiesPage = result.data.page;
                _activitiesTotalPages = result.data.totalPages;
                // Cũng cập nhật LiveData cũ để Fragment không bị break
                _recentActivities.postValue(Resource.success(result.data.content));
            }
            _activitiesPaged.postValue(result);
        });
    }

    public void nextActivitiesPage() {
        if (_activitiesPage < _activitiesTotalPages - 1) {
            loadActivitiesPage(_activitiesPage + 1);
        }
    }

    public void prevActivitiesPage() {
        if (_activitiesPage > 0) {
            loadActivitiesPage(_activitiesPage - 1);
        }
    }

    public int getActivitiesPage()       { return _activitiesPage; }
    public int getActivitiesTotalPages() { return _activitiesTotalPages; }

    // ── Phân trang khoá học ─────────────────────────────────────────────────

    public void loadCoursesPage(int page) {
        _coursesPaged.postValue(Resource.loading());
        executor.execute(() -> {
            Resource<PagedResponse<CourseResponse>> result =
                    repository.getMyCoursesPagedTeacher(page, PAGE_SIZE);
            if (result.data != null) {
                _coursesPage = result.data.page;
                _coursesTotalPages = result.data.totalPages;
            }
            _coursesPaged.postValue(result);
        });
    }

    public void nextCoursesPage() {
        if (_coursesPage < _coursesTotalPages - 1) {
            loadCoursesPage(_coursesPage + 1);
        }
    }

    public void prevCoursesPage() {
        if (_coursesPage > 0) {
            loadCoursesPage(_coursesPage - 1);
        }
    }

    public int getCoursesPage()       { return _coursesPage; }
    public int getCoursesTotalPages() { return _coursesTotalPages; }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}