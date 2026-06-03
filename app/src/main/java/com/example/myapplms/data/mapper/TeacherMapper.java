package com.example.myapplms.data.mapper;

import com.example.myapplms.data.remote.dto.request.TeacherRequest;
import com.example.myapplms.data.remote.dto.response.TeacherResponse;
import com.example.myapplms.model.Teacher;

public class TeacherMapper {

    // ── DTO → Domain Model ────────────────────────────────────
    public static Teacher toModel(TeacherResponse dto) {
        if (dto == null) return null;
        return new Teacher(
                dto.getTeacherId(),
                dto.getUserId(),
                dto.getFirstName(),
                dto.getLastName(),
                dto.getBirthDate(),
                dto.getLocation(),
                dto.getPhone(),
                dto.getBio(),
                dto.getDegree()
        );
    }

    // ── Domain Model → Request ────────────────────────────────
    public static TeacherRequest toRequest(Teacher model) {
        if (model == null) return null;
        return new TeacherRequest(
                model.getFirstName(),
                model.getLastName(),
                model.getBirthDate(),
                model.getLocation(),
                model.getPhone(),
                model.getBio(),
                model.getDegree()
        );
    }
}