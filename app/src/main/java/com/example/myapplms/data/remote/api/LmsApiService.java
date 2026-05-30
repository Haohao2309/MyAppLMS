package com.example.myapplms.data.remote.api;

import com.example.myapplms.data.remote.dto.request.CreateCommentRequest;
import com.example.myapplms.data.remote.dto.request.CreatePostRequest;
import com.example.myapplms.data.remote.dto.request.LoginRequest;
import com.example.myapplms.data.remote.dto.request.RefreshTokenRequest;
import com.example.myapplms.data.remote.dto.response.AuthResponse;
import com.example.myapplms.data.remote.dto.response.community_response.CommentResponse;
import com.example.myapplms.data.remote.dto.response.community_response.CommunityActionResponse;
import com.example.myapplms.data.remote.dto.response.community_response.CommunityStatsResponse;
import com.example.myapplms.data.remote.dto.response.community_response.PostDetailResponse;
import com.example.myapplms.data.remote.dto.response.community_response.PostResponse;

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
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/refresh")
    Call<AuthResponse> refreshToken(@Body RefreshTokenRequest request);

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
}
