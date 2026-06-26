package com.example.myapplms.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class GradeSubmissionRequest {
    @SerializedName("studentId")
    public Integer studentId;

    @SerializedName("courseId")
    public Integer courseId;

    @SerializedName("examScore")
    public Double examScore;

    @SerializedName("finalScore")
    public Double finalScore;

    @SerializedName("gradeLevel")
    public String gradeLevel;

    @SerializedName("isMasked")
    public Boolean isMasked; // false = công bố điểm, true = ẩn điểm

    // Nhận xét (tags + comment) - lưu dưới dạng JSON string hoặc separate field
    @SerializedName("feedbackTags")
    public String feedbackTags; // VD: "Lập luận tốt,Ngôn ngữ rõ ràng"

    @SerializedName("feedbackComment")
    public String feedbackComment;
}