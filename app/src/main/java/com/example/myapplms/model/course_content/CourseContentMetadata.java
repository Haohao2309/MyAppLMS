package com.example.myapplms.model.course_content;

import com.example.myapplms.data.remote.dto.response.course_content.CourseMetadataResponse;

public class CourseContentMetadata {
    public int totalLessons;
    public int totalDuration;
    public String lastUpdated;

    public CourseContentMetadata(int totalLessons, int totalDuration, String lastUpdated) {
        this.totalLessons = totalLessons;
        this.totalDuration = totalDuration;
        this.lastUpdated = lastUpdated;
    }

    public static CourseContentMetadata fromResponse(CourseMetadataResponse res) {
        if (res == null) return new CourseContentMetadata(0, 0, "");
        return new CourseContentMetadata(res.totalLessons, res.totalDuration, res.lastUpdated);
    }
}