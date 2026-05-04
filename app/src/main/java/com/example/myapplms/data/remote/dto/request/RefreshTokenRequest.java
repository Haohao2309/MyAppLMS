package com.example.myapplms.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class RefreshTokenRequest {
    @SerializedName("refreshToken")
    private String refreshToken;

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}