package com.example.myapplms.data.remote.dto.response.community_response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PostResponse {
    public String id;
    public String title;
    public String content;
    public String category;
    public String type;
    public int views;
    public int likes;
    public int commentsCount;
    public String createdAt;
    public String updatedAt;
    public String userId;
    public String authorName;
    public String authorRole;

    // 🌟 SỬA DÒNG NÀY: Đồng bộ thành "pinned" để khớp 100% với Spring Boot của sếp
    @SerializedName("pinned")
    public boolean pinned;
    
    public boolean likedByMe;

    public List<String> tags;
}