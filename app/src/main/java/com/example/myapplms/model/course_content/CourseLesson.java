package com.example.myapplms.model.course_content;

import com.example.myapplms.data.remote.dto.response.course_content.LessonResponse;
import java.util.Map;

public class CourseLesson {
    public String lessonId;
    public String title;
    public String type; // "video", "quiz", "assignment"
    public int orderIndex;
    public int duration;
    public Map<String, Object> content;
    public boolean isPreview;

    public CourseLesson(String lessonId, String title, String type, int orderIndex,
                        int duration, Map<String, Object> content, boolean isPreview) {
        this.lessonId = lessonId;
        this.title = title;
        this.type = type;
        this.orderIndex = orderIndex;
        this.duration = duration;
        this.content = content;
        this.isPreview = isPreview;
    }

    public static CourseLesson fromResponse(LessonResponse res) {
        return new CourseLesson(
                res.lessonId,
                res.title,
                res.type,
                res.orderIndex,
                res.duration,
                res.content,
                res.isPreview != null ? res.isPreview : false
        );
    }
}