package com.example.myapplms.data.remote.dto.request;

public class CreateCommentRequest {
    public String content;
    public String parentCommentId;

    public CreateCommentRequest(String content) {
        this.content = content;
    }

    public CreateCommentRequest(String content, String parentCommentId) {
        this.content = content;
        this.parentCommentId = parentCommentId;
    }
}
