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
    public int userId;

    @SerializedName("email")
    public String email;

    // ✅ Đổi tên biến thành "active" cho đồng bộ tuyệt đối với JSON
    @SerializedName("active")
    public boolean active;

    @SerializedName("role")
    public String role;

    // ✅ Bổ sung SerializedName để bảo vệ an toàn cho data khi build release
    @SerializedName("teacherId")
    public Integer teacherId;

    @SerializedName("studentId")
    public Integer studentId;
}