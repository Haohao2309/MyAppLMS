// data/remote/dto/response/TeacherStatsResponse.java
package com.example.myapplms.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

public class TeacherStatsResponse {
    // Các chỉ số thống kê
    @SerializedName("totalStudents") public long totalStudents;
    @SerializedName("enrollmentsThisMonth") public long enrollmentsThisMonth;
    @SerializedName("totalRevenue") public BigDecimal totalRevenue; // Đổi sang BigDecimal để khớp Backend và tránh sai số tiền tệ
    @SerializedName("avgRating") public double avgRating;
    @SerializedName("totalCourses") public long totalCourses;

    // Các chuỗi text hiển thị (từ Service Backend truyền xuống)
    @SerializedName("earningsChange") public String earningsChange;
    @SerializedName("studentsChange") public String studentsChange;
    @SerializedName("revenueChange") public String revenueChange;
    @SerializedName("ratingChange") public String ratingChange;

    // Danh sách khóa học
    @SerializedName("courses") public List<CourseResponse> courses;
}