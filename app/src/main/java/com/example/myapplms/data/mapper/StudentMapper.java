package com.example.myapplms.data.mapper;

import com.example.myapplms.data.remote.dto.request.StudentRequest;
import com.example.myapplms.data.remote.dto.response.StudentResponse;
import com.example.myapplms.model.Student;

/**
 * StudentMapper — tách biệt 3 tầng dữ liệu.
 *
 * Tầng dữ liệu:
 *
 *   [Network DTO]          [UI Model]           [Network DTO]
 *   StudentResponse ──→   Student        ──→   StudentRequest
 *      (GET response)    (domain model)         (PUT request)
 *
 * Tất cả hàm là static, không cần khởi tạo:
 *   StudentMapper.toModel(response)
 *   StudentMapper.toRequest(model)
 *   StudentMapper.toRequest(response)    ← tiện khi pre-fill form Edit
 */
public class StudentMapper {

    // ── Response → UI Model ──────────────────────────────────────────────────

    /**
     * Chuyển DTO nhận từ API thành domain model dùng trong UI / ViewModel.
     */
    public static Student toModel(StudentResponse dto) {
        if (dto == null) return null;
        return new Student(
                dto.studentId,
                dto.userId,
                safe(dto.firstName),
                safe(dto.lastName),
                safe(dto.birthDate),
                safe(dto.location),
                safe(dto.phone),
                safe(dto.bio),
                safe(dto.school)
        );
    }

    // ── UI Model → Request ───────────────────────────────────────────────────

    /**
     * Chuyển domain model thành request body để gọi PUT /api/students/{id}.
     * Dùng khi người dùng lưu chỉnh sửa từ màn Edit Profile.
     */
    public static StudentRequest toRequest(Student model) {
        if (model == null) return null;
        return StudentRequest.builder()
                .firstName(model.getFirstName())
                .lastName(model.getLastName())
                .birthDate(model.getBirthDate())
                .location(model.getLocation())
                .phone(model.getPhone())
                .bio(model.getBio())
                .school(model.getSchool())
                .build();
    }

    // ── Response → Request (tiện ích pre-fill) ───────────────────────────────

    /**
     * Chuyển thẳng DTO response thành request body.
     * Tiện dụng khi mở màn Edit Profile: lấy data hiện tại → đổ vào form.
     *
     * Ví dụ sử dụng:
     *   StudentRequest prefilled = StudentMapper.toRequest(response);
     *   binding.etFirstName.setText(prefilled.getFirstName());
     */
    public static StudentRequest toRequest(StudentResponse dto) {
        if (dto == null) return null;
        return StudentRequest.builder()
                .firstName(safe(dto.firstName))
                .lastName(safe(dto.lastName))
                .birthDate(safe(dto.birthDate))
                .location(safe(dto.location))
                .phone(safe(dto.phone))
                .bio(safe(dto.bio))
                .school(safe(dto.school))
                .build();
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    /** Trả về chuỗi rỗng thay vì null để tránh NullPointerException ở UI. */
    private static String safe(String s) {
        return s != null ? s : "";
    }
}