package com.example.myapplms.model;

import com.google.gson.annotations.SerializedName;

public class Notification {
    @SerializedName("_id")
    private String id;
    private String title;
    private String body;
    private String link;
    private String type; // "achievement", "grade", "message", "announcement", "payment"
    private boolean isRead;
    @SerializedName("createdAt")
    private String timestamp;
    private String dateGroup; // "Today", "Yesterday", "Earlier"

    public Notification(String id, String title, String body, String link, String type, String timestamp, String dateGroup, boolean isRead) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.link = link;
        this.type = type;
        this.timestamp = timestamp;
        this.dateGroup = dateGroup;
        this.isRead = isRead;
    }

    // Getters & Setters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getType() { return type; }
    public String getLink() { return link; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public String getTimestamp() { return timestamp; }
    public String getDateGroup() { return dateGroup; }
}
