package com.example.myapplms.data.remote.dto.response;
import com.google.gson.annotations.SerializedName;

public class NotificationResponse {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("message") // Đón trường message từ Spring Boot
    private String message;
    @SerializedName("link")
    private String link;

    @SerializedName("type")
    private String type;

    @SerializedName("isRead")
    private boolean read;

    @SerializedName("createdAt")
    private String createdAt; // Dạng "2026-05-13T10:30:00"

    // TODO: Bạn tự generate Getters/Setters ở đây nhé
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
    public String getLink() {
        return link;
    }

    public String getType() {
        return type;
    }

    public boolean isRead() {
        return read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

}