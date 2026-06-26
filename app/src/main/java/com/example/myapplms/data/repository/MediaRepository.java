package com.example.myapplms.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.response.CourseResponse;
import com.example.myapplms.data.remote.dto.response.UserResponse;
import com.example.myapplms.utils.Resource;
import com.example.myapplms.utils.SessionManager;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MediaRepository {

    private final LmsApiService apiService;
    private final SessionManager sessionManager;

    public MediaRepository(LmsApiService apiService, SessionManager sessionManager) {
        this.apiService     = apiService;
        this.sessionManager = sessionManager;
    }

    // ── Upload avatar lên Cloudinary qua server ───────────────
    public LiveData<Resource<String>> uploadAvatar(File imageFile) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.postValue(Resource.loading());

        // Tạo multipart body từ file ảnh
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("image/*"), imageFile);
        MultipartBody.Part part = MultipartBody.Part.createFormData(
                "file", imageFile.getName(), requestBody);

        apiService.uploadAvatar(part).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String imageUrl = response.body().imageUrl;
                    // Lưu vào session để dùng lại không cần gọi API
                    sessionManager.saveImageUrl(imageUrl);
                    result.postValue(Resource.success(imageUrl));
                } else {
                    result.postValue(Resource.error("Upload thất bại: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                result.postValue(Resource.error("Không có kết nối mạng", null));
            }
        });

        return result;
    }

    // ── Upload ảnh khóa học lên Cloudinary qua server ───────────────
    public LiveData<Resource<String>> uploadCourseImage(File imageFile) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.postValue(Resource.loading());

        RequestBody requestBody = RequestBody.create(
                MediaType.parse("image/*"), imageFile);
        MultipartBody.Part part = MultipartBody.Part.createFormData(
                "file", imageFile.getName(), requestBody);

        apiService.uploadCourseImage(part).enqueue(new Callback<CourseResponse>() {
            // Thay đoạn onResponse của uploadCourseImage:
            @Override
            public void onResponse(Call<CourseResponse> call, Response<CourseResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().imageUrl != null      // ← thêm check này
                        && !response.body().imageUrl.isEmpty()) {
                    result.postValue(Resource.success(response.body().imageUrl));
                } else {
                    result.postValue(Resource.error("Upload thất bại: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<CourseResponse> call, Throwable t) {
                result.postValue(Resource.error("Không có kết nối mạng", null));
            }
        });

        return result;
    }

}