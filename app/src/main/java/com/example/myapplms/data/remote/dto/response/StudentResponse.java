package com.example.myapplms.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

/**
 * Bước 1 — DTO ánh xạ JSON trả về từ GET /api/students/{id}
 */
public class StudentResponse {

    @SerializedName("studentId")
    public Integer studentId;

    @SerializedName("userId")
    public Integer userId;

    @SerializedName("firstName")
    public String firstName;

    @SerializedName("lastName")
    public String lastName;

    @SerializedName("birthDate")
    public String birthDate;   // "yyyy-MM-dd"

    @SerializedName("location")
    public String location;

    @SerializedName("phone")
    public String phone;

    @SerializedName("bio")
    public String bio;

    @SerializedName("school")
    public String school;
}