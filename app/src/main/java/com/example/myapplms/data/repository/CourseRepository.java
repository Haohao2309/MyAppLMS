package com.example.myapplms.data.repository;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.response.CourseResponse;
import com.example.myapplms.model.Course;
import com.example.myapplms.utils.Resource;
import com.example.myapplms.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseRepository {

    private final LmsApiService apiService;

    public CourseRepository(LmsApiService apiService) {
        this.apiService = apiService;
    }

    public LiveData<Resource<List<Course>>> getCourses() {
        MutableLiveData<Resource<List<Course>>> result = new MutableLiveData<>();
        // Báo LOADING ngay lập tức

        result.postValue(Resource.loading());
        apiService.getCourses().enqueue(new Callback<List<CourseResponse>>() {
            @Override
            public void onResponse(Call<List<CourseResponse>> call,
                                   Response<List<CourseResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Course> domainList = new ArrayList<>();
                    for (CourseResponse dto : response.body()) {
                        domainList.add(Course.fromResponse(dto));
                    }
                    result.postValue(Resource.success(domainList));
                } else {
                    result.postValue(Resource.error("Lỗi server: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<List<CourseResponse>> call, Throwable t) {
                result.postValue(Resource.error("Không có kết nối mạng: " + t.getMessage(), null));
            }
        });

        return result;
    }
    public LiveData<Resource<List<Course>>> getCoursesByTeacherId(Integer teacherId) {
        MutableLiveData<Resource<List<Course>>> result = new MutableLiveData<>();
        // Báo LOADING ngay lập tức

        result.postValue(Resource.loading());
        apiService.getCoursesByTeacherId(teacherId).enqueue(new Callback<List<CourseResponse>>() {
            @Override
            public void onResponse(Call<List<CourseResponse>> call,
                                   Response<List<CourseResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Course> domainList = new ArrayList<>();
                    for (CourseResponse dto : response.body()) {
                        domainList.add(Course.fromResponse(dto));
                    }
                    result.postValue(Resource.success(domainList));
                } else {
                    result.postValue(Resource.error("Lỗi server: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<List<CourseResponse>> call, Throwable t) {
                result.postValue(Resource.error("Không có kết nối mạng: " + t.getMessage(), null));
            }
        });

        return result;
    }

}