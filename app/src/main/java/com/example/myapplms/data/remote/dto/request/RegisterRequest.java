package com.example.myapplms.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    public RegisterRequest(String username, String email, String password, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }
}