package com.example.myapplms.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ReviewResponse {
    @SerializedName("id")
    public String id;

    @SerializedName("studentId")
    public Integer studentId;

    @SerializedName("studentName")
    public String studentName;

    @SerializedName("rating")
    public Double rating;

    @SerializedName("title")
    public String title;

    @SerializedName("content")
    public String content;

    @SerializedName("pros")
    public List<String> pros;

    @SerializedName("cons")
    public List<String> cons;

    @SerializedName("upvotes")
    public Integer upvotes;

    @SerializedName("createdAt")
    public String createdAt;
}