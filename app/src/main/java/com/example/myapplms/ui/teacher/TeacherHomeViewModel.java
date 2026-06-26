package com.example.myapplms.ui.teacher;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplms.data.remote.dto.response.DashboardOverviewResponse;
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

    public TeacherHomeViewModel(TeacherRepository repository) {
        this.repository = repository;
    }

    /** Legacy — giữ lại */
    public void loadDashboard(Integer teacherId) {
        _dashboardData.postValue(Resource.loading());
        executor.execute(() -> _dashboardData.postValue(repository.getDashboardStats(teacherId)));
    }

    /** Gọi 4 API v2 song song */
    public void loadAllDashboard() {
        _overview.postValue(Resource.loading());
        _recentActivities.postValue(Resource.loading());
        _weeklyActivity.postValue(Resource.loading());
        _tasks.postValue(Resource.loading());

        executor.execute(() -> _overview.postValue(repository.getOverview()));
        executor.execute(() -> _recentActivities.postValue(repository.getRecentActivities()));
        executor.execute(() -> _weeklyActivity.postValue(repository.getWeeklyActivity()));
        executor.execute(() -> _tasks.postValue(repository.getTasks()));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}