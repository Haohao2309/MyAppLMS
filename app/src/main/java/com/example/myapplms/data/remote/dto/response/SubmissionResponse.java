package com.example.myapplms.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class SubmissionResponse {
    @SerializedName("submissionId")
    public String submissionId;

    @SerializedName("studentId")
    public Integer studentId;

    @SerializedName("studentName")
    public String studentName;

    @SerializedName("fileUrl")
    public String fileUrl;

    @SerializedName("submittedAt")
    public String submittedAt;

    @SerializedName("type")
    public String type;

    @SerializedName("attemptCount")
    public Integer attemptCount;

    // Điểm từ CourseGrade (JOIN phía BE)
    @SerializedName("examScore")
    public Double examScore;

    @SerializedName("finalScore")
    public Double finalScore;

    @SerializedName("gradeLevel")
    public String gradeLevel;

    @SerializedName("isMasked")
    public Boolean isMasked; // false = đã chấm, true/null = chưa chấm
}