package com.example.myapplms.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class TeacherResponse {
    @SerializedName("teacherId")
    private Integer teacherId; // Đổi sang Integer cho khớp với dữ liệu thực tế từ DB
    @SerializedName("userId")
    private Integer userId;    // Đổi sang Integer
    @SerializedName("firstName")
    private String firstName;
    @SerializedName("lastName")
    private String lastName;
    @SerializedName("birthDate")
    private String birthDate;  // Đổi từ LocalDate sang String để Gson không bị lỗi parse
    @SerializedName("location")
    private String location;
    @SerializedName("phone")
    private String phone;
    @SerializedName("bio")
    private String bio;        // Sửa lại tên biến thành bio cho đúng bản chất dữ liệu
    @SerializedName("degree")
    private String degree;

    // --- BẮT BUỘC PHẢI CÓ CÁC HÀM GETTER DƯỚI ĐÂY ĐỂ FRAGMENT GỌI ĐƯỢC ---
    public Integer getTeacherId() { return teacherId; }
    public Integer getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getBirthDate() { return birthDate; }
    public String getLocation() { return location; }
    public String getPhone() { return phone; }
    public String getBio() { return bio; }
    public String getDegree() { return degree; }
}