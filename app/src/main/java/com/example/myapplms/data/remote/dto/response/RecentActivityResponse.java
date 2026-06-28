package com.example.myapplms.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class RecentActivityResponse {

    @SerializedName("studentName")
    public String studentName;

    // JSON là "studentInitials" nhưng biến trong code của bạn là "initials"
    @SerializedName("studentInitials")
    public String initials;

    // JSON là "description" nhưng biến trong code của bạn là "action"
    @SerializedName("description")
    public String action;

    @SerializedName("timeAgo")
    public String timeAgo;

    // JSON là "courseTitle" nhưng biến trong code của bạn là "courseName"
    @SerializedName("courseTitle")
    public String courseName;

    // Bạn có thể khai báo thêm các trường khác trong JSON nếu cần dùng sau này
    @SerializedName("activityType")
    public String activityType;

    @SerializedName("courseId")
    public Integer courseId;

    @SerializedName("occurredAt")
    public String occurredAt;
}