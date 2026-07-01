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

    @SerializedName("totalStudents")
    public Integer totalStudents;

    @SerializedName("totalLessons")
    public Integer totalLessons;

    @SerializedName("averageRating")
    public Double averageRating;

    @SerializedName("purchased")
    public Boolean purchased;

    @SerializedName("isDeleted")
    public Boolean isDeleted;

    @SerializedName("archiveStatus")
    public String archiveStatus;
}