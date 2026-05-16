package com.example.myapplms.data.repository;

import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.response.TeacherResponse;
import com.example.myapplms.utils.Resource;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;

public class TeacherRepository {
    private final LmsApiService lmsApi;

    public TeacherRepository(LmsApiService lmsApi) {
        this.lmsApi = lmsApi;
    }
    public Resource<TeacherResponse> getTeacherbyId(Integer id) {
        try {
            Response<TeacherResponse> response = lmsApi.getTeacherbyId(id).execute();
            if (response.isSuccessful() && response.body() != null) {
                return Resource.success(response.body());
            }
            return Resource.error("Lỗi: " + response.code(), null);
        } catch (IOException e) {
            return Resource.error("Không có kết nối mạng", null);
        }

        }
    public Resource<List<TeacherResponse>> getTeacher() {
        try {
            Response<List<TeacherResponse>> response = lmsApi.getTeachers().execute();

            if (response.isSuccessful() && response.body() != null) {
                return Resource.success(response.body());
            }
            return Resource.error("Lỗi: " + response.code(), null);

        } catch (IOException e) {
            return Resource.error("Không có kết nối mạng", null);
        }
    }
}