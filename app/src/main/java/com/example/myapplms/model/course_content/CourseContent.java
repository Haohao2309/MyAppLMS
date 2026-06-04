package com.example.myapplms.model.course_content;

import com.example.myapplms.data.remote.dto.response.course_content.CourseContentResponse;
import com.example.myapplms.data.remote.dto.response.course_content.ModuleResponse;
import java.util.ArrayList;
import java.util.List;

public class CourseContent {
    public int courseId;
    public String courseTitle;
    public String description;
    public List<CourseModule> modules;
    public CourseContentMetadata metadata;

    public CourseContent(int courseId, String courseTitle, String description,
                         List<CourseModule> modules, CourseContentMetadata metadata) {
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.description = description;
        this.modules = modules;
        this.metadata = metadata;
    }

    public static CourseContent fromResponse(CourseContentResponse res) {
        List<CourseModule> domainModules = new ArrayList<>();
        if (res.modules != null) {
            for (ModuleResponse modRes : res.modules) {
                domainModules.add(CourseModule.fromResponse(modRes));
            }
        }
        return new CourseContent(
                res.courseId != null ? res.courseId : 0,
                res.courseTitle,
                res.description,
                domainModules,
                CourseContentMetadata.fromResponse(res.metadata)
        );
    }
}