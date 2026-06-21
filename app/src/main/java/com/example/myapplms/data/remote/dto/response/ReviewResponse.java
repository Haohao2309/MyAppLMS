package com.example.myapplms.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ReviewResponse {
    @SerializedName(value = "_id", alternate = {"id"})
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

    @SerializedName("downvotes")
    public Integer downvotes;

    @SerializedName("votedUpBy")
    public List<Integer> votedUpBy;

    @SerializedName("votedDownBy")
    public List<Integer> votedDownBy;

    @SerializedName("isVerified")
    public Boolean isVerified;

    @SerializedName("courseId")
    public Integer courseId;

    @SerializedName("createdAt")
    public String createdAt;
}