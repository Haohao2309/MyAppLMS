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
}