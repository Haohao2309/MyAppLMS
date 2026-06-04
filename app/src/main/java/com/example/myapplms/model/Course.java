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
    public int imageRes; // Tạm dùng int vì dùng ảnh local, sau này đổi thành String cho URL

    public Course(int id, String title, String description, String instructor, String rating, String students,
                  String lessons, String duration, String priceText, String category,
                  String level, int imageRes) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.instructor = instructor;
        this.rating = rating;
        this.students = students;
        this.lessons = lessons;
        this.duration = duration;
        this.priceText = priceText;
        this.category = category;
        this.level = level;
        this.imageRes = imageRes;
    }

    // Mapper: Chuyển DTO thành Domain Model
    public static Course fromResponse(CourseResponse res) {
        String displayPrice = (res.price == null || res.price <= 0)
                ? "FREE"
                : String.format(Locale.US, "$%.2f", res.price);

        String instructorName = res.teacherName != null ? "by " + res.teacherName : "by Unknown";

        return new Course(
                res.courseId != null ? res.courseId : 0,
                res.title,
                res.description,
                instructorName,
                "4.5 (1k+)",     // Fake data vì DB chưa có
                "10k students",  // Fake data
                "12 lessons",    // Fake data
                "2h 00m",        // Fake data
                displayPrice,
                res.categoryName != null ? res.categoryName : "General",
                "Beginner",      // Fake data
                R.drawable.ic_launcher_background // Ảnh mặc định
        );
    }
}