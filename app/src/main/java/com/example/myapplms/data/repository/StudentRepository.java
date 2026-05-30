package com.example.myapplms.data.repository;


import com.example.myapplms.data.mapper.StudentMapper;
import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.request.StudentRequest;
import com.example.myapplms.data.remote.dto.response.StudentResponse;
import com.example.myapplms.model.Student;
import com.example.myapplms.utils.Resource;

import java.io.IOException;

import retrofit2.Response;

/**
 * StudentRepository — tầng dữ liệu duy nhất biết về API Student.
 *
 * Luồng:
 *   ViewModel gọi repository
 *       ↓
 *   Repository gọi API (LmsApiService) → nhận StudentResponse (DTO)
 *       ↓
 *   StudentMapper.toModel()  →  Student (domain model)
 *       ↓
 *   Trả Resource<Student> về ViewModel
 *
 * Nếu cần UPDATE, ViewModel build StudentRequest qua StudentMapper.toRequest()
 * rồi gọi updateStudent().
 */
public class StudentRepository {

    private final LmsApiService apiService;

    public StudentRepository(LmsApiService apiService) {
        this.apiService = apiService;
    }

    // ── GET ──────────────────────────────────────────────────────────────────

    /**
     * Lấy thông tin sinh viên theo userId (= user.id trong Spring Boot).
     * Gọi: GET /api/students/{userId}
     */
    public Resource<Student> getStudentByUserId(Integer userId) {
        try {
            Response<StudentResponse> response =
                    apiService.getStudentById(userId).execute();

            if (response.isSuccessful() && response.body() != null) {
                // DTO → domain model qua Mapper
                Student student = StudentMapper.toModel(response.body());
                return Resource.success(student);
            }

            String errorMsg = "Lỗi " + response.code() + ": Không tìm thấy sinh viên";
            return Resource.error(errorMsg, null);

        } catch (IOException e) {
            return Resource.error("Không có kết nối mạng", null);
        }
    }

    // ── PUT ──────────────────────────────────────────────────────────────────

    /**
     * Cập nhật thông tin sinh viên.
     * Gọi: PUT /api/students/{studentId}
     *
     * Cách dùng từ ViewModel:
     *   StudentRequest req = StudentMapper.toRequest(currentStudent);
     *   req.setPhone("0912...");   // sửa field cần update
     *   repository.updateStudent(studentId, req);
     */
    public Resource<Student> updateStudent(Integer studentId, StudentRequest request) {
        try {
            Response<StudentResponse> response =
                    apiService.updateStudent(studentId, request).execute();

            if (response.isSuccessful() && response.body() != null) {
                Student updated = StudentMapper.toModel(response.body());
                return Resource.success(updated);
            }

            String errorMsg = "Lỗi " + response.code() + ": Cập nhật thất bại";
            return Resource.error(errorMsg, null);

        } catch (IOException e) {
            return Resource.error("Không có kết nối mạng", null);
        }
    }
}