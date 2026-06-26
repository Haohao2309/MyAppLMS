package com.example.myapplms.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CreateReviewRequest {
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

    public CreateReviewRequest(Double rating, String title, String content, List<String> pros, List<String> cons) {
        this.rating = rating;
        this.title = title;
        this.content = content;
        this.pros = pros;
        this.cons = cons;
    }
}
