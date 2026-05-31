package com.example.myapplms.data.remote.api;

import com.example.myapplms.data.remote.dto.request.CreateCommentRequest;
import com.example.myapplms.data.remote.dto.request.CreatePostRequest;

import com.example.myapplms.data.remote.dto.request.CourseRequest;
import com.example.myapplms.data.remote.dto.request.LoginRequest;
import com.example.myapplms.data.remote.dto.request.RefreshTokenRequest;
import com.example.myapplms.data.remote.dto.request.RegisterRequest;
import com.example.myapplms.data.remote.dto.request.StudentRequest;
import com.example.myapplms.data.remote.dto.response.ApiResponse;
import com.example.myapplms.data.remote.dto.response.AuthResponse;
import com.example.myapplms.data.remote.dto.response.community_response.CommentResponse;
import com.example.myapplms.data.remote.dto.response.community_response.CommunityActionResponse;
import com.example.myapplms.data.remote.dto.response.community_response.CommunityStatsResponse;
import com.example.myapplms.data.remote.dto.response.community_response.PostDetailResponse;
import com.example.myapplms.data.remote.dto.response.community_response.PostResponse;

import java.util.List;
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

    // ── Community API ───────────────────────────────────────

    @GET("community/posts")
    Call<List<PostResponse>> getPosts(
            @Query("category") String category,
            @Query("q") String query,
            @Query("sortBy") String sortBy,
            @Query("page") Integer page,
            @Query("size") Integer size
    );

    @GET("community/posts/{id}")
    Call<PostDetailResponse> getPostDetail(@Path("id") String id);

    @POST("community/posts")
    Call<PostResponse> createPost(
            @Query("category") String category,
            @Body CreatePostRequest request
    );

    @DELETE("community/posts/{id}")
    Call<CommunityActionResponse> deletePost(@Path("id") String postId);

    @POST("community/posts/{id}/like")
    Call<CommunityActionResponse> toggleLike(@Path("id") String postId);

    @POST("community/posts/{id}/comments")
    Call<CommentResponse> addComment(
            @Path("id") String postId,
            @Body CreateCommentRequest request
    );

    @DELETE("community/posts/{postId}/comments/{commentId}")
    Call<CommunityActionResponse> deleteComment(
            @Path("postId") String postId,
            @Path("commentId") String commentId
    );

    @PUT("community/posts/{postId}/comments/{commentId}")
    Call<CommentResponse> updateComment(
            @Path("postId") String postId,
            @Path("commentId") String commentId,
            @Body CreateCommentRequest request
    );

    @PUT("community/posts/{id}")
    Call<PostResponse> updatePost(
            @Path("id") String postId,
            @Body CreatePostRequest request
    );

    @GET("community/posts/stats")
    Call<CommunityStatsResponse> getCommunityStats();

    // 🌟 Tìm đến hàm togglePin trong file LmsApiService.java và sửa lại thành:
    @POST("api/community/posts/{postId}/pin")
    Call<PostResponse> togglePin(
            @Path("postId") String postId
    );
    @GET("teachers")
    Call<List<TeacherResponse>> getTeachers();
    @GET("teachers/{id}")
    Call<TeacherResponse> getTeacherbyId(@Path("id") Integer id); // Bắt buộc phải có @Path("id")

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


