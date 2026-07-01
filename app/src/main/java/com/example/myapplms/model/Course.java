package com.example.myapplms.model;

import com.example.myapplms.data.remote.dto.response.CourseResponse;
import java.text.NumberFormat;
import java.util.Locale;

public class Course {
    public int id;
    public String title;
    public String description;
    public String instructor;
    public String rating;
    public String students;
    public String lessons;
    public String duration;
    public String priceText;
    public String category;
    public String level;
    public String imageUrl;
    public boolean purchased;
    public boolean isDeleted;

    public Course(int id, String title, String description, String instructor, String rating,
                  String students, String lessons, String duration, String priceText,
                  String category, String level, String imageUrl, boolean purchased, boolean isDeleted) {
        this.id          = id;
        this.title       = title;
        this.description = description;
        this.instructor  = instructor;
        this.rating      = rating;
        this.students    = students;
        this.lessons     = lessons;
        this.duration    = duration;
        this.priceText   = priceText;
        this.category    = category;
        this.level       = level;
        this.imageUrl    = imageUrl;
        this.purchased   = purchased;
        this.isDeleted   = isDeleted;
    }

    public static Course fromResponse(CourseResponse res) {
        String displayPrice = (res.price == null || res.price <= 0)
                ? "FREE"
                : String.format(Locale.US, "$%.2f", res.price);

        String instructorName = res.teacherName != null
                ? "by " + res.teacherName
                : "by Unknown";

        // Xử lý hiển thị số học sinh thực tế (ví dụ: "1,200 students")
        int studentsCount = res.totalStudents != null ? res.totalStudents : 0;
        String studentsDisplay = NumberFormat.getNumberInstance(Locale.US).format(studentsCount) + " students";

        // Xử lý hiển thị tổng số bài học thực tế
        int lessonsCount = res.totalLessons != null ? res.totalLessons : 0;
        String lessonsDisplay = lessonsCount + " lessons";

        return new Course(
                res.courseId != null ? res.courseId : 0,
                res.title,
                res.description,
                instructorName,
                res.averageRating != null ? String.valueOf(res.averageRating) : "0.0",
                studentsDisplay,
                lessonsDisplay,
                "2h 00m",
                displayPrice,
                res.categoryName != null ? res.categoryName : "General",
                "Beginner",
                res.imageUrl != null ? res.imageUrl : "",
                res.purchased != null ? res.purchased : false,
                res.isDeleted != null ? res.isDeleted : false
        );
    }
}