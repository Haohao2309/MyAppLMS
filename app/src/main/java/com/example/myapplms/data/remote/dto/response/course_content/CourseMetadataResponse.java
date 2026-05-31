package com.example.myapplms.data.remote.dto.response.course_content;

import com.google.gson.annotations.SerializedName;

public class CourseMetadataResponse {
    @SerializedName("totalLessons")
    public int totalLessons;

    @SerializedName("totalDuration")
    public int totalDuration;

    @SerializedName("lastUpdated")
    public String lastUpdated;
}