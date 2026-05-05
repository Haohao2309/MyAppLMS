package com.example.myapplms.model;

public class Notification {
    public enum Type {
        ACHIEVEMENT, QUIZ, MESSAGE, UPDATE, ENROLLMENT
    }

    private String title;
    private String description;
    private String timeAgo;
    private boolean isUnread;
    private Type type;

    public Notification(String title, String description, String timeAgo, boolean isUnread, Type type) {
        this.title = title;
        this.description = description;
        this.timeAgo = timeAgo;
        this.isUnread = isUnread;
        this.type = type;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getTimeAgo() { return timeAgo; }
    public boolean isUnread() { return isUnread; }
    public Type getType() { return type; }
}
