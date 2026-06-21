package com.example.myapplms.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DashboardResponse {
    @SerializedName("achievements")
    public List<AchievementDTO> achievements;

    @SerializedName("continueLearning")
    public List<CourseResponse> continueLearning;

    @SerializedName("continueLearningCourses")
    public List<CourseResponse> continueLearningCourses;

    @SerializedName("featuredCourses")
    public List<CourseResponse> featuredCourses;

    @SerializedName("recommendedCourses")
    public List<CourseResponse> recommendedCourses;

    public static class AchievementDTO {
        @SerializedName("id")
        public String id;
        
        @SerializedName("title")
        public String title;
        
        @SerializedName("value")
        public String value;
        
        @SerializedName("type")
        public String type; // e.g., "STREAK", "POINTS", "RANK"
    }
}