package com.example.myapplms.data.remote.dto.response.course_content;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ModuleResponse {
    @SerializedName("moduleId")
    public String moduleId;

    @SerializedName("title")
    public String title;

    @SerializedName("orderIndex")
    public int orderIndex;

    @SerializedName("lessons")
    public List<LessonResponse> lessons;
}