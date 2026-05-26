package com.example.myapplms.model.course_content;

import com.example.myapplms.data.remote.dto.response.course_content.ModuleResponse;
import com.example.myapplms.data.remote.dto.response.course_content.LessonResponse;
import java.util.ArrayList;
import java.util.List;

public class CourseModule {
    public String moduleId;
    public String title;
    public int orderIndex;
    public List<CourseLesson> lessons;

    public CourseModule(String moduleId, String title, int orderIndex, List<CourseLesson> lessons) {
        this.moduleId = moduleId;
        this.title = title;
        this.orderIndex = orderIndex;
        this.lessons = lessons;
    }

    public static CourseModule fromResponse(ModuleResponse res) {
        List<CourseLesson> domainLessons = new ArrayList<>();
        if (res.lessons != null) {
            for (LessonResponse lessonRes : res.lessons) {
                domainLessons.add(CourseLesson.fromResponse(lessonRes));
            }
        }
        return new CourseModule(res.moduleId, res.title, res.orderIndex, domainLessons);
    }
}