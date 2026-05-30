package com.example.myapplms.data.remote.api;


import com.example.myapplms.data.remote.dto.request.LoginRequest;
import com.example.myapplms.data.remote.dto.request.PaymentCheckoutRequest;
import com.example.myapplms.data.remote.dto.request.PaymentWebhookRequest;
import com.example.myapplms.data.remote.dto.request.RefreshTokenRequest;
import com.example.myapplms.data.remote.dto.request.VoteRequest;
import com.example.myapplms.data.remote.dto.response.AuthResponse;
import com.example.myapplms.data.remote.dto.response.NotificationResponse;
import com.example.myapplms.data.remote.dto.response.PaymentCheckoutResponse;
import com.example.myapplms.data.remote.dto.response.PaymentWebhookResponse;
import com.example.myapplms.data.remote.dto.response.CourseResponse;
import com.example.myapplms.data.remote.dto.response.ReviewResponse;
import com.example.myapplms.data.remote.dto.response.TeacherResponse;
import com.example.myapplms.data.remote.dto.response.course_content.CourseContentResponse;
import com.example.myapplms.model.Course;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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

    @GET("notifications/my-notifications")
    Call<List<NotificationResponse>> getMyNotifications();

    @PATCH("notifications/{id}/read")
    Call<Void> markAsRead(@Path("id") String id);
    @GET("v1/courses")
    Call<List<CourseResponse>> getCourses();
    @GET("v1/courses/teacher/{id}")
    Call<List<CourseResponse>> getCoursesByTeacherId(@Path("id") Integer id);

    @GET("v1/courses/{id}")
    Call<CourseResponse> getCourseById(@Path("id") int id);

    @GET("v1/courses/{id}/content")
    Call<CourseContentResponse> getCourseContent(@Path("id") int id);

    // Lấy danh sách review
    @GET("courses/{courseId}/reviews")
    Call<List<ReviewResponse>> getCourseReviews(@Path("courseId") int courseId);

    // Vote review (Để sẵn cho tính năng vote sau này)
    @PUT("v1/courses/{courseId}/reviews/{reviewId}/vote")
    Call<ReviewResponse> voteReview(@Path("courseId") int courseId, @Path("reviewId") String reviewId, @Body VoteRequest request);
}

