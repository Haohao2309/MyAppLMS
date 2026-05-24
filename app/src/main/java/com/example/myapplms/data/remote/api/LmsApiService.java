package com.example.myapplms.data.remote.api;


import com.example.myapplms.data.remote.dto.request.LoginRequest;
import com.example.myapplms.data.remote.dto.request.PaymentCheckoutRequest;
import com.example.myapplms.data.remote.dto.request.PaymentWebhookRequest;
import com.example.myapplms.data.remote.dto.request.RefreshTokenRequest;
import com.example.myapplms.data.remote.dto.response.AuthResponse;
import com.example.myapplms.data.remote.dto.response.NotificationResponse;
import com.example.myapplms.data.remote.dto.response.PaymentCheckoutResponse;
import com.example.myapplms.data.remote.dto.response.PaymentWebhookResponse;
import com.example.myapplms.data.remote.dto.response.TeacherResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface LmsApiService {

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/refresh")
    Call<AuthResponse> refreshToken(@Body RefreshTokenRequest request);

    @POST("auth/logout")
    Call<Void> logout(@Body RefreshTokenRequest request);
    @GET("teachers")
    Call<List<TeacherResponse>> getTeachers();
    @GET("teachers/{id}")
    Call<TeacherResponse> getTeacherbyId(@Path("id") Integer id); // Bắt buộc phải có @Path("id")

    // Payment
    @POST("payments/checkout")
    Call<PaymentCheckoutResponse> checkout(@Body PaymentCheckoutRequest request);

    @POST("payments/webhook")
    Call<PaymentWebhookResponse> paymentWebhook(@Body PaymentWebhookRequest request);

    @GET("/api/notifications/my-notifications")
    Call<List<NotificationResponse>> getMyNotifications();

    @PATCH("/api/notifications/{id}/read")
    Call<Void> markAsRead(@Path("id") String id);
}

