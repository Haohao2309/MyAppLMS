package com.example.myapplms.model;

public class Comment {
    private String userName;
    private String avatarUrl; // For now, we'll use a placeholder
    private String timeAgo;
    private String content;
    private int likeCount;
    private boolean isInstructor;
    private boolean isAuthor;

    public Comment(String userName, String timeAgo, String content, int likeCount, boolean isInstructor, boolean isAuthor) {
        this.userName = userName;
        this.timeAgo = timeAgo;
        this.content = content;
        this.likeCount = likeCount;
        this.isInstructor = isInstructor;
        this.isAuthor = isAuthor;
    }

    public String getUserName() { return userName; }
    public String getTimeAgo() { return timeAgo; }
    public String getContent() { return content; }
    public int getLikeCount() { return likeCount; }
    public boolean isInstructor() { return isInstructor; }
    public boolean isAuthor() { return isAuthor; }
}
