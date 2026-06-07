package com.example.myapplms.data.remote.dto.response;

// ProgressResponse.java
import java.util.List;

public class ProgressResponse {
    public String currentLesson;
    public Double overallProgress;
    public Integer totalWatchTime;
    public List<String> completedLessons;
    public List<LessonDetailProgress> lessonDetails;

    public static class LessonDetailProgress {
        public String lessonId;
        public String status;
        public Double progressPercent;
        public Integer score;
    }
}