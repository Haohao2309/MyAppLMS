package com.example.myapplms.data.repository;

import com.example.myapplms.data.mapper.TeacherMapper;
import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.request.TeacherRequest;
import com.example.myapplms.data.remote.dto.response.TeacherResponse;
import com.example.myapplms.data.remote.dto.response.TeacherStatsResponse;
import com.example.myapplms.model.Teacher;
import com.example.myapplms.utils.Resource;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;

public class TeacherRepository {

    private final LmsApiService lmsApi;

    public TeacherRepository(LmsApiService lmsApi) {
        this.lmsApi = lmsApi;
    }

    // ── GET by ID ─────────────────────────────────────────────
    public Resource<Teacher> getTeacherById(Integer id) {
        try {
            Response<TeacherResponse> response = lmsApi.getTeacherbyId(id).execute();
            if (response.isSuccessful() && response.body() != null) {
                return Resource.success(TeacherMapper.toModel(response.body()));
            }
            return Resource.error("Lỗi: " + response.code(), null);
        } catch (IOException e) {
            return Resource.error("Không có kết nối mạng", null);
        }
    }

    // ── GET list ──────────────────────────────────────────────
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

    // ── PUT ───────────────────────────────────────────────────
    public Resource<Teacher> updateTeacher(Integer teacherId, TeacherRequest request) {
        try {
            Response<TeacherResponse> response =
                    lmsApi.updateTeacher(teacherId, request).execute();
            if (response.isSuccessful() && response.body() != null) {
                return Resource.success(TeacherMapper.toModel(response.body()));
            }
            return Resource.error("Lỗi " + response.code() + ": Cập nhật thất bại", null);
        } catch (IOException e) {
            return Resource.error("Không có kết nối mạng", null);
        }
    }

    public Resource<TeacherStatsResponse> getDashboardStats(Integer teacherId) {
        try {
            Response<TeacherStatsResponse> response = lmsApi.getTeacherDashboardStats(teacherId).execute();
            if (response.isSuccessful() && response.body() != null) {
                return Resource.success(response.body());
            }
            return Resource.error("Lỗi: " + response.code(), null);
        } catch (IOException e) {
            return Resource.error("Không có kết nối mạng", null);
        }
    }
}