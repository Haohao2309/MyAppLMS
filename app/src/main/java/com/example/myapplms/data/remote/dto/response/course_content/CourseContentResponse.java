package com.example.myapplms.data.remote.dto.response.course_content;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CourseContentResponse {
    @SerializedName("id")
    public String id;

    @SerializedName("courseId")
    public Integer courseId;

    @SerializedName("nameCourse")
    public String nameCourse;

    @SerializedName("courseTitle")
    public String courseTitle;

    @SerializedName("description")
    public String description;

    @SerializedName("modules")
    public List<ModuleResponse> modules;

    @SerializedName("metadata")
    public CourseMetadataResponse metadata;
}