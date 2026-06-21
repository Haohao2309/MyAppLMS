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
    public int progressPercent; // Added for progress tracking
    public int imageRes;
    public String imageUrl;

    public Course(int id, String title, String description, String instructor, String rating, String students,
                  String lessons, String duration, String priceText, String category,
                  String level, int imageRes) {
        this(id, title, description, instructor, rating, students, lessons, duration, priceText, category, level, 0, imageRes);
    }

    public Course(int id, String title, String description, String instructor, String rating, String students,
                  String lessons, String duration, String priceText, String category,
                  String level, int progressPercent, int imageRes) {
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
        this.progressPercent = progressPercent;
        this.imageRes = imageRes;
    }

    // Mapper: Chuyển DTO thành Domain Model
    public static Course fromResponse(CourseResponse res) {
        String displayPrice = (res.price == null || res.price <= 0)
                ? "FREE"
                : String.format(Locale.US, "$%.2f", res.price);

        String instructorName = res.teacherName != null ? "by " + res.teacherName : "by Unknown";
        String lessonsText = res.lessonsCount != null ? res.lessonsCount + " lessons" : "0 lessons";
        String ratingText = res.rating != null ? String.format(Locale.US, "%.1f", res.rating) : "0.0";
        String studentsText = res.studentsCount != null ? res.studentsCount + " students" : "0 students";
        String durationText = res.duration != null ? res.duration : "0h 0m";
        String levelText = res.level != null ? res.level : "Beginner";

        Course course = new Course(
                res.courseId != null ? res.courseId : 0,
                res.title,
                res.description,
                instructorName,
                ratingText,
                studentsText,
                lessonsText,
                durationText,
                displayPrice,
                res.categoryName != null ? res.categoryName : "General",
                levelText,
                res.progressPercent != null ? res.progressPercent : 0,
                R.drawable.ic_launcher_background
        );
        course.imageUrl = res.imageUrl;
        return course;
    }
}
