package com.example.myapplms.data.remote.dto.request;

import java.util.List;

public class CreatePostRequest {
    private String title;
    private String content;
    private String category;
    private String type;
    private List<String> tags;

    public CreatePostRequest(String title, String content, String category, String type, List<String> tags) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.type = type;
        this.tags = tags;
    }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getCategory() { return category; }
    public String getType() { return type; }
    public List<String> getTags() { return tags; }
}
