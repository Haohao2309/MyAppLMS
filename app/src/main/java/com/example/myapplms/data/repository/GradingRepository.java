package com.example.myapplms.data.repository;

import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.request.SubmitGradeRequest;
import com.example.myapplms.data.remote.dto.response.GradingListResponse;
import com.example.myapplms.utils.Resource;

import retrofit2.Response;

public class GradingRepository {

    private final LmsApiService api;

    public GradingRepository(LmsApiService api) {
        this.api = api;
    }

    /** Lấy danh sách sinh viên + trạng thái chấm theo courseId */
    public Resource<GradingListResponse> getGradingList(int courseId) {
        try {
            Response<GradingListResponse> res = api.getGradingList(courseId).execute();
            if (res.isSuccessful() && res.body() != null) {
                return Resource.success(res.body());
            }
            return Resource.error("Lỗi " + res.code(), null);
        } catch (Exception e) {
            return Resource.error(e.getMessage(), null);
        }
    }

    /** Giáo viên nhập điểm → ghi CourseGrade */
    public Resource<GradingListResponse.StudentGradingItem> submitGrade(SubmitGradeRequest request) {
        try {
            Response<GradingListResponse.StudentGradingItem> res =
                    api.submitGrade(request).execute();
            if (res.isSuccessful() && res.body() != null) {
                return Resource.success(res.body());
            }
            return Resource.error("Lỗi " + res.code(), null);
        } catch (Exception e) {
            return Resource.error(e.getMessage(), null);
        }
    }
}