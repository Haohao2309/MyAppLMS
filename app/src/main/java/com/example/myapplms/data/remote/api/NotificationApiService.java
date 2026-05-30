package com.example.myapplms.data.remote.api;

import com.example.myapplms.data.remote.dto.response.NotificationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface NotificationApiService {

    @GET("/api/notifications/my-notifications")
    Call<List<NotificationResponse>> getMyNotifications();

    @PATCH("/api/notifications/{id}/read")
    Call<Void> markAsRead(@Path("id") String id);
}