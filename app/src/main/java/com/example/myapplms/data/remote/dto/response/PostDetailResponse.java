package com.example.myapplms.data.remote.dto.response;

import java.util.List;

public class PostDetailResponse extends PostResponse {
    public boolean likedByMe;
    public List<CommentResponse> comments;
}
