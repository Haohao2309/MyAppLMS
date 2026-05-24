package com.example.myapplms.model;

public class Course {
    private Integer courseId;
    private String name;
    private String instructor;
    private String rating;
    private String students;
    private String lessons;
    private String duration;
    private String price;
    private String category;
    private String level;
    private int imageResId;

    public Course(Integer courseId, String name, String instructor, String rating, String students, String lessons, String duration, String price, String category, String level, int imageResId) {
        this.courseId = courseId;
        this.name = name;
        this.instructor = instructor;
        this.rating = rating;
        this.students = students;
        this.lessons = lessons;
        this.duration = duration;
        this.price = price;
        this.category = category;
        this.level = level;
        this.imageResId = imageResId;
    }

    public Integer getCourseId() { return courseId; }
    public String getName() { return name; }
    public String getInstructor() { return instructor; }
    public String getRating() { return rating; }
    public String getStudents() { return students; }
    public String getLessons() { return lessons; }
    public String getDuration() { return duration; }
    public String getPrice() { return price; }
    public String getCategory() { return category; }
    public String getLevel() { return level; }
    public int getImageResId() { return imageResId; }
}
