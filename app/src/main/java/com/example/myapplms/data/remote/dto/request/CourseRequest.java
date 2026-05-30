package com.example.myapplms.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class CourseRequest {

    @SerializedName("teacherId")
    private Integer teacherId;

    @SerializedName("categoryId")
    private Integer categoryId;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("price")
    private Double price;

    public CourseRequest(Integer teacherId, Integer categoryId, String title,
                         String description, String imageUrl, Double price) {
        this.teacherId   = teacherId;
        this.categoryId  = categoryId;
        this.title       = title;
        this.description = description;
        this.imageUrl    = imageUrl;
        this.price       = price;
    }
}