package com.example.myapplms.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class UserResponse {
    @SerializedName("id")
    public Integer id;

    @SerializedName("email")
    public String email;

    @SerializedName("username")
    public String username;

    @SerializedName("imageUrl")
    public String imageUrl;

    @SerializedName("role")
    public String role;
}