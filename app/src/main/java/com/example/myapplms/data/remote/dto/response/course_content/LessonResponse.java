package com.example.myapplms.data.remote.dto.response.course_content;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class LessonResponse {
    @SerializedName("lessonId")
    public String lessonId;

    @SerializedName("title")
    public String title;

    @SerializedName("type")
    public String type;

    @SerializedName("orderIndex")
    public int orderIndex;

    @SerializedName("duration")
    public int duration;

    // Retrofit (Gson) sẽ tự động map JSON Object thành LinkedTreeMap
    @SerializedName("content")
    public Map<String, Object> content;

    @SerializedName("isPreview")
    public Boolean isPreview;
}