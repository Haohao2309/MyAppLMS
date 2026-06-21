package com.example.myapplms.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class EnrollmentStatusResponse {
    @SerializedName("enrolled")
    public boolean enrolled;

    @SerializedName("enrollmentId")
    public Integer enrollmentId;
}
