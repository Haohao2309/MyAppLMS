package com.example.myapplms.data.model;

public class Comment {
    private final Long id;
    private final String content;
    private final String userName;
    private final String createdAt;

    public Comment(Long id, String content, String userName, String createdAt) {
        this.id = id;
        this.content = content;
        this.userName = userName;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getContent() { return content; }
    public String getUserName() { return userName; }
    public String getCreatedAt() { return createdAt; }
}
