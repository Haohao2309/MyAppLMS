package com.example.myapplms.data.repository;

import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.request.CreateCommentRequest;
import com.example.myapplms.data.remote.dto.request.CreatePostRequest;
import com.example.myapplms.data.remote.dto.response.community_response.CommentResponse;
import com.example.myapplms.data.remote.dto.response.community_response.CommunityActionResponse;
import com.example.myapplms.data.remote.dto.response.community_response.CommunityStatsResponse;
import com.example.myapplms.data.remote.dto.response.community_response.PostDetailResponse;
import com.example.myapplms.data.remote.dto.response.community_response.PostResponse;

import java.util.List;

import retrofit2.Call;

public class CommunityRepository {
    private final LmsApiService apiService;

    public CommunityRepository(LmsApiService apiService) {
        this.apiService = apiService;
    }

    public Call<List<PostResponse>> getPosts(String category, String query, String sortBy, Integer page, Integer size) {
        return apiService.getPosts(category, query, sortBy, page, size);
    }

    public Call<PostDetailResponse> getPostDetail(String id) {
        return apiService.getPostDetail(id);
    }

    public Call<PostResponse> createPost(String category, CreatePostRequest request) {
        return apiService.createPost(category, request);
    }

    public Call<CommunityActionResponse> deletePost(String postId) {
        return apiService.deletePost(postId);
    }

    public Call<CommunityActionResponse> toggleLike(String postId) {
        return apiService.toggleLike(postId);
    }

    public Call<CommentResponse> addComment(String postId, CreateCommentRequest request) {
        return apiService.addComment(postId, request);
    }

    public Call<CommunityActionResponse> deleteComment(String postId, String commentId) {
        return apiService.deleteComment(postId, commentId);
    }

    public Call<PostResponse> updatePost(String postId, CreatePostRequest request) {
        return apiService.updatePost(postId, request);
    }

    public Call<CommunityStatsResponse> getCommunityStats() {
        return apiService.getCommunityStats();
    }

    public Call<PostResponse> togglePin(String postId) {
        return apiService.togglePin(postId);
    }
}
