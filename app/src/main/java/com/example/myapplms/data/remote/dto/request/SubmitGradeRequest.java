package com.example.myapplms.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class SubmitGradeRequest {

    @SerializedName("studentId")
    private Integer studentId;

    @SerializedName("courseId")
    private Integer courseId;

    @SerializedName("examScore")
    private Double examScore;

    @SerializedName("isMasked")
    private Boolean isMasked;

    public SubmitGradeRequest(Integer studentId, Integer courseId, Double examScore, Boolean isMasked) {
        this.studentId = studentId;
        this.courseId  = courseId;
        this.examScore = examScore;
        this.isMasked  = isMasked;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public Double getExamScore() {
        return examScore;
    }

    public void setExamScore(Double examScore) {
        this.examScore = examScore;
    }

    public Boolean getMasked() {
        return isMasked;
    }

    public void setMasked(Boolean masked) {
        isMasked = masked;
    }


    // Bạn có thể tự generate thêm Getter/Setter nếu cần dùng đến
}