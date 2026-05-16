package com.example.myapplms.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class TeacherRequest {
    @SerializedName("firstName")
    private String firstName;
    @SerializedName("lastName")
    private String lastName;
    @SerializedName("birthDate")
    private String birthDate; // Đổi sang String
    @SerializedName("location")
    private String location;
    @SerializedName("phone")
    private String phone;
    @SerializedName("bio")
    private String bio;       // Đổi từ email thành bio
    @SerializedName("degree")
    private String degree;

    public TeacherRequest(String firstName, String lastName, String birthDate, String location, String phone, String bio, String degree) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.location = location;
        this.phone = phone;
        this.bio = bio;
        this.degree = degree;
    }
}