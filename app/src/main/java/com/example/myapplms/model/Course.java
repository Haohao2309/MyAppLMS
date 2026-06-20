package com.example.myapplms.model;


import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.response.CourseResponse;
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
    public String imageUrl;  // ← đổi int → String

    public Course(int id, String title, String description, String instructor, String rating,
                  String students, String lessons, String duration, String priceText,
                  String category, String level, String imageUrl) {  // ← đổi int → String
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
    }

    public static Course fromResponse(CourseResponse res) {
        String displayPrice = (res.price == null || res.price <= 0)
                ? "FREE"
                : String.format(Locale.US, "$%.2f", res.price);

        String instructorName = res.teacherName != null
                ? "by " + res.teacherName
                : "by Unknown";

        return new Course(
                res.courseId != null ? res.courseId : 0,
                res.title,
                res.description,
                instructorName,
                "4.5 (1k+)",
                "10k students",
                "12 lessons",
                "2h 00m",
                displayPrice,
                res.categoryName != null ? res.categoryName : "General",
                "Beginner",
                res.imageUrl != null ? res.imageUrl : ""  // ← lấy URL thật từ server
        );
    }
}