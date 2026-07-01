package com.example.myapplms.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.request.CreateDiscussionRequest;
import com.example.myapplms.data.remote.dto.request.SubmitAssignmentRequest;
import com.example.myapplms.data.remote.dto.request.SubmitQuizRequest;
import com.example.myapplms.data.remote.dto.request.SyncVideoRequest;
import com.example.myapplms.data.remote.dto.response.DiscussionResponse;
import com.example.myapplms.data.remote.dto.response.ProgressResponse;
import com.example.myapplms.utils.Resource;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LearningRepository {
    private final LmsApiService apiService;

    public LearningRepository(LmsApiService apiService) {
        this.apiService = apiService;
    }

    private final MutableLiveData<Resource<ProgressResponse>> progressLiveData = new MutableLiveData<>();

    public LiveData<Resource<ProgressResponse>> getProgressLiveData() {
        return progressLiveData;
    }

    // 1. Lấy tiến độ học tập (Gọi hàm này sẽ tự động cập nhật progressLiveData)
    public void fetchProgress(int courseId) {
        progressLiveData.setValue(Resource.loading());

        apiService.getProgress(courseId).enqueue(new Callback<ProgressResponse>() {
            @Override
            public void onResponse(Call<ProgressResponse> call, Response<ProgressResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    progressLiveData.postValue(Resource.success(response.body()));
                } else {
                    progressLiveData.postValue(Resource.error("Lỗi tải tiến độ: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ProgressResponse> call, Throwable t) {
                progressLiveData.postValue(Resource.error("Lỗi mạng: " + t.getMessage(), null));
            }
        });
    }

    // 2. Đồng bộ tiến độ Video
    public LiveData<Resource<Void>> syncVideoProgress(int courseId, String lessonId, SyncVideoRequest request) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        
        apiService.syncVideoProgress(courseId, lessonId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.postValue(Resource.success(null));
                } else {
                    result.postValue(Resource.error("Lỗi đồng bộ", null));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.postValue(Resource.error("Lỗi mạng: " + t.getMessage(), null));
            }
        });
        return result;
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
    public LiveData<Resource<List<DiscussionResponse>>> getDiscussions(int courseId, String lessonId) {
        MutableLiveData<Resource<List<DiscussionResponse>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.getDiscussions(courseId, lessonId).enqueue(new Callback<List<DiscussionResponse>>() {
            @Override
            public void onResponse(Call<List<DiscussionResponse>> call, Response<List<DiscussionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Không thể tải danh sách thảo luận", null));
                }
            }

            @Override
            public void onFailure(Call<List<DiscussionResponse>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi mạng: " + t.getMessage(), null));
            }
        });
        return result;
    }

    public LiveData<Resource<DiscussionResponse>> createDiscussion(int courseId, String lessonId, CreateDiscussionRequest request) {
        MutableLiveData<Resource<DiscussionResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.createDiscussion(courseId, lessonId, request).enqueue(new Callback<DiscussionResponse>() {
            @Override
            public void onResponse(Call<DiscussionResponse> call, Response<DiscussionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Không thể tạo câu hỏi", null));
                }
            }

            @Override
            public void onFailure(Call<DiscussionResponse> call, Throwable t) {
                result.setValue(Resource.error("Lỗi mạng: " + t.getMessage(), null));
            }
        });
        return result;
    }
}