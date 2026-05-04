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

    @SerializedName("fullName")
    public String fullName;

    @SerializedName("role")
    public String role; // "STUDENT" | "TEACHER" | "ADMIN"
}

