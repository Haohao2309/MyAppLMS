package com.example.myapplms.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class RecentActivityResponse {
    @SerializedName("studentName")
    public String studentName;

    @SerializedName("initials")
    public String initials;

    @SerializedName("action")
    public String action;

    @SerializedName("courseId")
    public int courseId;

    @SerializedName("courseName")
    public String courseName;

    @SerializedName("timeAgo")
    public String timeAgo;
}
