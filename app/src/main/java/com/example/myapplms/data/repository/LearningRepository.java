package com.example.myapplms.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.request.SubmitAssignmentRequest;
import com.example.myapplms.data.remote.dto.request.SubmitQuizRequest;
import com.example.myapplms.data.remote.dto.request.SyncVideoRequest;
import com.example.myapplms.data.remote.dto.response.ProgressResponse;
import com.example.myapplms.utils.Resource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LearningRepository {
    private final LmsApiService apiService;

    public LearningRepository(LmsApiService apiService) {
        this.apiService = apiService;
    }

    // 1. Lấy tiến độ học tập
    public LiveData<Resource<ProgressResponse>> getProgress(int courseId) {
        MutableLiveData<Resource<ProgressResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.getProgress(courseId).enqueue(new Callback<ProgressResponse>() {
            @Override
            public void onResponse(Call<ProgressResponse> call, Response<ProgressResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(Resource.success(response.body()));
                } else {
                    result.postValue(Resource.error("Lỗi tải tiến độ: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ProgressResponse> call, Throwable t) {
                result.postValue(Resource.error("Lỗi mạng: " + t.getMessage(), null));
            }
        });
        return result;
    }

    // 2. Đồng bộ tiến độ Video (Chạy ngầm không cần LiveData trả về UI)
    public void syncVideoProgress(int courseId, String lessonId, SyncVideoRequest request) {
        apiService.syncVideoProgress(courseId, lessonId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Thành công thì im lặng chạy tiếp, nếu cần có thể in log
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Log lỗi mạng nếu cần
            }
        });
    }

    // 3. Nộp bài Quiz
    public LiveData<Resource<ProgressResponse>> submitQuiz(int courseId, String lessonId, SubmitQuizRequest request) {
        MutableLiveData<Resource<ProgressResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.submitQuiz(courseId, lessonId, request).enqueue(new Callback<ProgressResponse>() {
            @Override
            public void onResponse(Call<ProgressResponse> call, Response<ProgressResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(Resource.success(response.body()));
                } else {
                    result.postValue(Resource.error("Lỗi nộp bài trắc nghiệm: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ProgressResponse> call, Throwable t) {
                result.postValue(Resource.error("Lỗi mạng: " + t.getMessage(), null));
            }
        });
        return result;
    }

    // 4. Nộp Assignment
    public LiveData<Resource<ProgressResponse>> submitAssignment(int courseId, String lessonId, SubmitAssignmentRequest request) {
        MutableLiveData<Resource<ProgressResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.submitAssignment(courseId, lessonId, request).enqueue(new Callback<ProgressResponse>() {
            @Override
            public void onResponse(Call<ProgressResponse> call, Response<ProgressResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(Resource.success(response.body()));
                } else {
                    result.postValue(Resource.error("Lỗi nộp bài tập: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ProgressResponse> call, Throwable t) {
                result.postValue(Resource.error("Lỗi mạng: " + t.getMessage(), null));
            }
        });
        return result;
    }
}