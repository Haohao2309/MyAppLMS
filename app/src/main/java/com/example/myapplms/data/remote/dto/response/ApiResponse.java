package com.example.myapplms.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {

    @SerializedName("success")
    private boolean success;

    // Banner API trả về "status": "ok" thay vì boolean success
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data; // Generics: Chứa data thật (AuthResponse, User, Course...)

    // Getter methods
    public boolean isSuccess() { return success || "ok".equalsIgnoreCase(status); }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}