package com.example.myapplms.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.response.CourseResponse;
import com.example.myapplms.data.remote.dto.response.course_content.CourseContentResponse;
import com.example.myapplms.model.Course;
import com.example.myapplms.model.course_content.CourseContent;
import com.example.myapplms.utils.Resource;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseDetailRepository {

    private final LmsApiService apiService;

    public CourseDetailRepository(LmsApiService apiService) {
        this.apiService = apiService;
    }

    /**
     * Lấy thông tin cơ bản của khóa học từ PostgreSQL
     */
    public LiveData<Resource<Course>> getCourseDetail(int courseId) {
        MutableLiveData<Resource<Course>> result = new MutableLiveData<>();
        result.postValue(Resource.loading());

        apiService.getCourseById(courseId).enqueue(new Callback<CourseResponse>() {
            @Override
            public void onResponse(Call<CourseResponse> call, Response<CourseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Course domainCourse = Course.fromResponse(response.body());
                    result.postValue(Resource.success(domainCourse));
                } else {
                    result.postValue(Resource.error("Lỗi server: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<CourseResponse> call, Throwable t) {
                result.postValue(Resource.error("Không có kết nối mạng: " + t.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Lấy chi tiết nội dung phân cấp (Modules, Lessons) từ MongoDB
     */
    public LiveData<Resource<CourseContent>> getCourseContent(int courseId) {
        MutableLiveData<Resource<CourseContent>> result = new MutableLiveData<>();
        result.postValue(Resource.loading());

        apiService.getCourseContent(courseId).enqueue(new Callback<CourseContentResponse>() {
            @Override
            public void onResponse(Call<CourseContentResponse> call, Response<CourseContentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CourseContent domainContent = CourseContent.fromResponse(response.body());
                    result.postValue(Resource.success(domainContent));
                } else {
                    result.postValue(Resource.error("Lỗi server: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<CourseContentResponse> call, Throwable t) {
                result.postValue(Resource.error("Không có kết nối mạng: " + t.getMessage(), null));
            }
        });

        return result;
    }
}