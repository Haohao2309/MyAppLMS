package com.example.myapplms.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class VoteRequest {

    @SerializedName("studentId")
    public Integer studentId;

    @SerializedName("isUpvote")
    public Boolean isUpvote;

    // Constructor để code gọi API cho gọn
    public VoteRequest(Integer studentId, Boolean isUpvote) {
        this.studentId = studentId;
        this.isUpvote = isUpvote;
    }
}