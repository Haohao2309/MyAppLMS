package com.example.myapplms.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class DashboardOverviewResponse {
    @SerializedName("teacherName")
    public String teacherName;

    @SerializedName("totalActiveCourses")
    public long totalActiveCourses;

    @SerializedName("totalStudents")
    public int totalStudents;

    @SerializedName("pendingGrading")
    public int pendingGrading;

    @SerializedName("avgScore")
    public double avgScore;
}
