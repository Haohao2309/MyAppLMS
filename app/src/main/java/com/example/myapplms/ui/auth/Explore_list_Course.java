package com.example.myapplms.ui.auth;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.Course;
import com.example.myapplms.CourseAdapter;
import com.example.myapplms.R;

import java.util.ArrayList;
import java.util.List;

public class Explore_list_Course extends AppCompatActivity {

    private RecyclerView rvCourses;
    private CourseAdapter adapter;
    private List<Course> courseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_list_course);

        initViews();
        setupRecyclerView();
        loadSampleData();
    }

    private void initViews() {
        rvCourses = findViewById(R.id.rvCourses);
    }

    private void setupRecyclerView() {
        courseList = new ArrayList<>();
        adapter = new CourseAdapter(courseList);
        rvCourses.setLayoutManager(new LinearLayoutManager(this));
        rvCourses.setAdapter(adapter);
    }

    private void loadSampleData() {
        // Thêm dữ liệu mẫu giống trong ảnh
        courseList.add(new Course(
                "E-Learning Platform Overview",
                "by Dr. Sarah Chen",
                "4.9 (1.2k)",
                "125k students",
                "18 lessons",
                "2h 30m",
                "FREE",
                "Development",
                "Beginner",
                R.drawable.ic_launcher_background // Thay bằng ảnh thật nếu có
        ));

        courseList.add(new Course(
                "Advanced Android Development",
                "by John Doe",
                "4.8 (850)",
                "50k students",
                "24 lessons",
                "5h 45m",
                "$49.99",
                "Development",
                "Advanced",
                R.drawable.ic_launcher_background
        ));

        courseList.add(new Course(
                "UI/UX Design Fundamentals",
                "by Jane Smith",
                "4.7 (2.1k)",
                "200k students",
                "12 lessons",
                "3h 15m",
                "FREE",
                "Design",
                "Beginner",
                R.drawable.ic_launcher_background
        ));

        adapter.notifyDataSetChanged();
    }
}
