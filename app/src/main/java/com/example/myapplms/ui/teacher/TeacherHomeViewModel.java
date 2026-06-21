package com.example.myapplms.ui.teacher;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplms.data.remote.dto.response.TeacherStatsResponse;
import com.example.myapplms.data.repository.TeacherRepository;
import com.example.myapplms.utils.Resource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TeacherHomeViewModel extends ViewModel {

    private final TeacherRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // LiveData chứa dữ liệu trả về từ API
    private final MutableLiveData<Resource<TeacherStatsResponse>> _dashboardData = new MutableLiveData<>();
    public final LiveData<Resource<TeacherStatsResponse>> dashboardData = _dashboardData;

    public TeacherHomeViewModel(TeacherRepository repository) {
        this.repository = repository;
    }

    // Hàm được Fragment gọi để lấy dữ liệu thống kê
    public void loadDashboard(Integer teacherId) {
        _dashboardData.postValue(Resource.loading()); // Báo cho UI biết đang tải dữ liệu

        executor.execute(() -> {
            // Gọi API thông qua Repository ở một luồng chạy ngầm (Background Thread)
            Resource<TeacherStatsResponse> result = repository.getDashboardStats(teacherId);

            // Đẩy kết quả về lại UI (Main Thread)
            _dashboardData.postValue(result);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Đóng luồng chạy ngầm để tránh rò rỉ bộ nhớ (memory leak) khi thoát Fragment
        executor.shutdown();
    }
}