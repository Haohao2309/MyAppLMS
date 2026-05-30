package com.example.myapplms.data.remote.dto.response.community_response;

import com.google.gson.annotations.SerializedName;

public class CommentResponse {
    @SerializedName("commentId")
    public String commentId;

    @SerializedName("userId")
    public String userId;

    @SerializedName("authorName")
    public String authorName;

    @SerializedName("content")
    public String content;

    @SerializedName("parentCommentId")
    public String parentCommentId;

    @SerializedName("createdAt")
    public String createdAt; // Nhận chuỗi ISO-8601 từ Instant của Backend
}