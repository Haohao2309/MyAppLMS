package com.example.myapplms.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("accessToken")
    public String accessToken;

    @SerializedName("refreshToken")
    public String refreshToken;

    @SerializedName("tokenType")
    public String tokenType;

    @SerializedName("userId")
    public String userId;

    @SerializedName("email")
    public String email;

    @SerializedName("isActive")
    public boolean isActive;

    @SerializedName("role")
    public String role; // "STUDENT" | "TEACHER" | "ADMIN"

    @SerializedName("teacherId")
    public Integer teacherId;  // null nếu không phải TEACHER
    @SerializedName("studentId")
    public Integer studentId;

    @SerializedName("imageUrl")
    public String imageUrl;


}
