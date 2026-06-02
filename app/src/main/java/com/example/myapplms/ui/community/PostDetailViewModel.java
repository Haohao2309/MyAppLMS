package com.example.myapplms.ui.community;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplms.data.remote.dto.request.CreateCommentRequest;
import com.example.myapplms.data.remote.dto.response.community_response.CommentResponse;
import com.example.myapplms.data.remote.dto.response.community_response.CommunityActionResponse;
import com.example.myapplms.data.remote.dto.response.community_response.PostDetailResponse;
import com.example.myapplms.data.repository.CommunityRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostDetailViewModel extends ViewModel {
    private final CommunityRepository repository;

    private final MutableLiveData<PostDetailResponse> _postDetail = new MutableLiveData<>();
    public LiveData<PostDetailResponse> postDetail = _postDetail;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<Boolean> _postDeleted = new MutableLiveData<>(false);
    public LiveData<Boolean> postDeleted = _postDeleted;

    public PostDetailViewModel(CommunityRepository repository) {
        this.repository = repository;
    }

    public void loadDetail(String postId) {
        _isLoading.setValue(true);
        repository.getPostDetail(postId).enqueue(new Callback<PostDetailResponse>() {
            @Override
            public void onResponse(Call<PostDetailResponse> call, Response<PostDetailResponse> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful()) {
                    _postDetail.setValue(response.body());
                } else {
                    _errorMessage.setValue("Lỗi tải chi tiết: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PostDetailResponse> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void toggleLike(String postId) {
        repository.toggleLike(postId).enqueue(new Callback<CommunityActionResponse>() {
            @Override
            public void onResponse(Call<CommunityActionResponse> call, Response<CommunityActionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PostDetailResponse current = _postDetail.getValue();
                    if (current != null && current.id.equals(postId)) {
                        current.likes = response.body().likesCount;
                        current.likedByMe = response.body().liked;
                        _postDetail.setValue(current);
                    }
                }
            }

            @Override
            public void onFailure(Call<CommunityActionResponse> call, Throwable t) {
                _errorMessage.setValue("Lỗi Like: " + t.getMessage());
            }
        });
    }

    public void addComment(String postId, String content, String parentCommentId) {
        _isLoading.setValue(true);
        CreateCommentRequest request = (parentCommentId == null) ? 
                new CreateCommentRequest(content) : 
                new CreateCommentRequest(content, parentCommentId);
                
        repository.addComment(postId, request).enqueue(new Callback<CommentResponse>() {
            @Override
            public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful()) {
                    loadDetail(postId);
                } else {
                    _errorMessage.setValue("Lỗi gửi bình luận: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CommentResponse> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void deleteComment(String postId, String commentId) {
        _isLoading.setValue(true);
        repository.deleteComment(postId, commentId).enqueue(new Callback<CommunityActionResponse>() {
            @Override
            public void onResponse(Call<CommunityActionResponse> call, Response<CommunityActionResponse> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful()) {
                    loadDetail(postId);
                } else {
                    _errorMessage.setValue("Lỗi xóa bình luận: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CommunityActionResponse> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void updateComment(String postId, String commentId, String content) {
        _isLoading.setValue(true);
        CreateCommentRequest request = new CreateCommentRequest(content);
        repository.updateComment(postId, commentId, request).enqueue(new Callback<CommentResponse>() {
            @Override
            public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful()) {
                    loadDetail(postId);
                } else {
                    _errorMessage.setValue("Lỗi cập nhật bình luận: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CommentResponse> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void deletePost(String postId) {
        _isLoading.setValue(true);
        repository.deletePost(postId).enqueue(new Callback<CommunityActionResponse>() {
            @Override
            public void onResponse(Call<CommunityActionResponse> call, Response<CommunityActionResponse> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful()) {
                    _postDeleted.setValue(true);
                } else {
                    _errorMessage.setValue("Lỗi xóa bài viết: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CommunityActionResponse> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
