package com.example.myapplms.data.remote.dto.response;


import com.google.gson.annotations.SerializedName;

public class CourseResponse {
    @SerializedName("courseId")
    public Integer courseId;

    @SerializedName("title")
    public String title;

    @SerializedName("description")
    public String description;

    @SerializedName("imageUrl")
    public String imageUrl;

    @SerializedName("price")
    public Double price;

    @SerializedName("teacherName")
    public String teacherName;

    @SerializedName("categoryName")
    public String categoryName;

    @SerializedName("progressPercent")
    public Integer progressPercent;

    @SerializedName("lessonsCount")
    public Integer lessonsCount;

    @SerializedName("rating")
    public Double rating;

    @SerializedName("studentsCount")
    public Integer studentsCount;

    @SerializedName("duration")
    public String duration;

    @SerializedName("level")
    public String level;
}
