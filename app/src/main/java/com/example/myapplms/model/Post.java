package com.example.myapplms.model;

import java.util.List;

public class Post {
    private String id;
    private String userName;
    private String timeAgo;
    private boolean isHot;
    private String title;
    private List<String> tags;
    private int commentCount;
    private int likeCount;
    private int viewCount;

    public Post(String userName, String timeAgo, boolean isHot, String title, List<String> tags, int commentCount, int likeCount, int viewCount) {
        this.userName = userName;
        this.timeAgo = timeAgo;
        this.isHot = isHot;
        this.title = title;
        this.tags = tags;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.viewCount = viewCount;
    }

    public String getUserName() { return userName; }
    public String getTimeAgo() { return timeAgo; }
    public boolean isHot() { return isHot; }
    public String getTitle() { return title; }
    public List<String> getTags() { return tags; }
    public int getCommentCount() { return commentCount; }
    public int getLikeCount() { return likeCount; }
    public int getViewCount() { return viewCount; }
}
