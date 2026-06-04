package com.example.myapplms.data.remote.dto.request;
import com.google.gson.annotations.SerializedName;

public class PaymentCheckoutRequest {
    @SerializedName("courseId")
    public Integer courseId;

    public PaymentCheckoutRequest(Integer courseId) {
        this.courseId = courseId;
    }
}