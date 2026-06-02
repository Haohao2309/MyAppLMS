package com.example.myapplms.data.remote.api;


import com.example.myapplms.data.remote.dto.request.CourseRequest;
import com.example.myapplms.data.remote.dto.request.LoginRequest;
import com.example.myapplms.data.remote.dto.request.RefreshTokenRequest;
import com.example.myapplms.data.remote.dto.request.RegisterRequest;
import com.example.myapplms.data.remote.dto.request.StudentRequest;
import com.example.myapplms.data.remote.dto.request.TeacherRequest;
import com.example.myapplms.data.remote.dto.response.ApiResponse;
import com.example.myapplms.data.remote.dto.response.AuthResponse;
import com.example.myapplms.data.remote.dto.response.CourseResponse;
import com.example.myapplms.data.remote.dto.response.StudentResponse;
import com.example.myapplms.data.remote.dto.response.TeacherResponse;
import com.example.myapplms.model.Course;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LmsApiService {

    @POST("auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);
    @POST("auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequest request);

    @POST("auth/refresh")
    Call<ApiResponse<AuthResponse>> refreshToken(@Body RefreshTokenRequest request);

    @POST("auth/logout")
    Call<Void> logout(@Body RefreshTokenRequest request);
    @GET("teachers")
    Call<List<TeacherResponse>> getTeachers();
    @GET("teachers/{id}")
    Call<TeacherResponse> getTeacherbyId(@Path("id") Integer id); // Bắt buộc phải có @Path("id")

    @PUT("teachers/{id}")
    Call<TeacherResponse> updateTeacher(@Path("id") Integer id, @Body TeacherRequest request);

    @GET("students/{id}")
    Call<StudentResponse> getStudentById(@Path("id") Integer userId);

    /** PUT cập nhật thông tin sinh viên theo studentId */
    @PUT("students/{id}")
    Call<StudentResponse> updateStudent(@Path("id") Integer studentId,
                                        @Body StudentRequest request);

    @GET("v1/courses")
    Call<List<CourseResponse>> getCourses();
    @GET("v1/courses/teacher/{id}")
    Call<List<CourseResponse>> getCoursesByTeacherId(@Path("id") Integer id);

    @POST("v1/courses")
    Call<CourseResponse> createCourse(@Body CourseRequest request);

    @PUT("v1/courses/{id}")
    Call<CourseResponse> updateCourse(@Path("id") Integer id, @Body CourseRequest request);

    @DELETE("v1/courses/{id}")
    Call<String> deleteCourse(@Path("id") Integer id,
                              @Query("deletedBy") String deletedBy,
                              @Query("reason") String reason);

}

