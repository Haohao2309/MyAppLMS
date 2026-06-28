package com.example.myapplms.data.repository;

import com.example.myapplms.data.mapper.TeacherMapper;
import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.request.TeacherRequest;
import com.example.myapplms.data.remote.dto.response.TeacherResponse;
import com.example.myapplms.data.remote.dto.response.TeacherStatsResponse;
import com.example.myapplms.data.remote.dto.response.DashboardOverviewResponse;
import com.example.myapplms.data.remote.dto.response.PagedResponse;
import com.example.myapplms.data.remote.dto.response.RecentActivityResponse;
import com.example.myapplms.data.remote.dto.response.WeeklyActivityResponse;
import com.example.myapplms.data.remote.dto.response.TaskItemResponse;
import com.example.myapplms.data.remote.dto.response.TeacherTaskResponse;
import com.example.myapplms.data.remote.dto.response.CourseResponse;
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

    // ── Dashboard v2 ──────────────────────────────────
    public Resource<DashboardOverviewResponse> getOverview() {
        try {
            Response<DashboardOverviewResponse> response = lmsApi.getTeacherOverview().execute();
            if (response.isSuccessful() && response.body() != null) {
                return Resource.success(response.body());
            }
            return Resource.error("Lỗi overview: " + response.code(), null);
        } catch (IOException e) {
            return Resource.error("Không có kết nối mạng", null);
        }
    }

    public Resource<java.util.List<RecentActivityResponse>> getRecentActivities() {
        try {
            Response<java.util.List<RecentActivityResponse>> response =
                    lmsApi.getRecentActivities().execute();
            if (response.isSuccessful() && response.body() != null) {
                return Resource.success(response.body());
            }
            return Resource.error("Lỗi activities: " + response.code(), null);
        } catch (IOException e) {
            return Resource.error("Không có kết nối mạng", null);
        }
    }

    public Resource<WeeklyActivityResponse> getWeeklyActivity() {
        try {
            Response<WeeklyActivityResponse> response = lmsApi.getWeeklyActivity().execute();
            if (response.isSuccessful() && response.body() != null) {
                return Resource.success(response.body());
            }
            return Resource.error("Lỗi weekly: " + response.code(), null);
        } catch (IOException e) {
            return Resource.error("Không có kết nối mạng", null);
        }
    }

    public Resource<TeacherTaskResponse> getTasks() {
        try {
            Response<TeacherTaskResponse> response = lmsApi.getTasks().execute();
            if (response.isSuccessful() && response.body() != null) {
                return Resource.success(response.body());
            }
            return Resource.error("Lỗi tasks: " + response.code(), null);
        } catch (IOException e) {
            return Resource.error("Không có kết nối mạng", null);
        }
    }

    // ── Phân trang (server-side) ──────────────────────────────────────────────

    /**
     * Lấy phân trang hoạt động gần đây (8 bản ghi/trang).
     */
    public Resource<PagedResponse<RecentActivityResponse>> getRecentActivitiesPaged(int page, int size) {
        try {
            Response<PagedResponse<RecentActivityResponse>> response =
                    lmsApi.getRecentActivitiesPaged(page, size).execute();
            if (response.isSuccessful() && response.body() != null) {
                return Resource.success(response.body());
            }
            return Resource.error("Lỗi activities paged: " + response.code(), null);
        } catch (IOException e) {
            return Resource.error("Không có kết nối mạng", null);
        }
    }

    /**
     * Lấy phân trang khoá học của teacher (8 bản ghi/trang).
     */
    public Resource<PagedResponse<CourseResponse>> getMyCoursesPagedTeacher(int page, int size) {
        try {
            Response<PagedResponse<CourseResponse>> response =
                    lmsApi.getMyCoursesPagedTeacher(page, size).execute();
            if (response.isSuccessful() && response.body() != null) {
                return Resource.success(response.body());
            }
            return Resource.error("Lỗi courses paged: " + response.code(), null);
        } catch (IOException e) {
            return Resource.error("Không có kết nối mạng", null);
        }
    }
}