package com.example.myapplms.data.model;

import java.util.List;

public class Post {
    private String id; // MongoDB ID là String
    private String userName;
    private String authorAvatar;
    private String time;
    private String title;
    private String content;
    private String type; // Khớp với trường "type" trong ảnh MongoDB của bạn
    private List<String> tags;
    private int views;
    private int likesCount; // Backend sẽ trả về số lượng từ array likes
    private int commentsCount; // Backend sẽ trả về số lượng từ array comments

    public Post(String id, String userName, String time, String title, String content, String type, int views, int likesCount, int commentsCount) {
        this.id = id;
        this.userName = userName;
        this.time = time;
        this.title = title;
        this.content = content;
        this.type = type;
        this.views = views;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
    }

    // Getters
    public String getId() { return id; }
    public String getUserName() { return userName; }
    public String getTime() { return time; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public int getViews() { return views; }
    public int getLikesCount() { return likesCount; }
    public int getCommentsCount() { return commentsCount; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
