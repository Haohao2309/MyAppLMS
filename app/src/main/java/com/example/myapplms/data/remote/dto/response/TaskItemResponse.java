package com.example.myapplms.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class TaskItemResponse {
    @SerializedName("taskType")
    public String taskType;

    @SerializedName("title")
    public String title;

    @SerializedName("courseId")
    public Integer courseId;

    @SerializedName("courseTitle")
    public String courseTitle;

    @SerializedName("dueDate")
    public String dueDate;

    /** Nếu true → hiện badge "Gấp" màu cam */
    @SerializedName("isUrgent")
    public boolean isUrgent;

    @SerializedName("itemCount")
    public long itemCount;
}
