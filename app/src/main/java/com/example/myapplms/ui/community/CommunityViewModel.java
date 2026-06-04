package com.example.myapplms.ui.community;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplms.data.repository.CommunityRepository;
import com.example.myapplms.data.remote.dto.request.CreatePostRequest;
import com.example.myapplms.data.remote.dto.response.community_response.CommunityStatsResponse;
import com.example.myapplms.data.remote.dto.response.community_response.PostResponse;
import com.example.myapplms.data.remote.dto.response.community_response.CommunityActionResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityViewModel extends ViewModel {
    private final CommunityRepository repository;

    private final MutableLiveData<List<PostResponse>> _posts = new MutableLiveData<>();
    public LiveData<List<PostResponse>> posts = _posts;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<Boolean> _postCreated = new MutableLiveData<>(false);
    public LiveData<Boolean> postCreated = _postCreated;

    private final MutableLiveData<CommunityStatsResponse> _stats = new MutableLiveData<>();
    public LiveData<CommunityStatsResponse> stats = _stats;

    private int currentPage = 1;
    private final int pageSize = 10;
    private boolean isLastPage = false;
    private String currentCategory = null;
    private String currentQuery = null;
    private String currentSortBy = "newest";

    public CommunityViewModel(CommunityRepository repository) {
        this.repository = repository;
    }

    public void fetchPosts(String category, String query, String sortBy, boolean isLoadMore) {
        if (Boolean.TRUE.equals(_isLoading.getValue())) return;
        if (isLoadMore && isLastPage) return;

        if (!isLoadMore) {
            currentPage = 1;
            isLastPage = false;
            fetchStats();
        } else {
            currentPage++;
        }

        currentCategory = category;
        currentQuery = query;
        currentSortBy = sortBy;
        _isLoading.setValue(true);

        repository.getPosts(category, query, sortBy, currentPage, pageSize).enqueue(new Callback<List<PostResponse>>() {
            @Override
            public void onResponse(Call<List<PostResponse>> call, Response<List<PostResponse>> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<PostResponse> newPosts = response.body();
                    if (newPosts.size() < pageSize) {
                        isLastPage = true;
                    }

                    if (isLoadMore) {
                        List<PostResponse> currentList = new ArrayList<>(_posts.getValue() != null ? _posts.getValue() : new ArrayList<>());
                        currentList.addAll(newPosts);
                        _posts.setValue(currentList);
                    } else {
                        _posts.setValue(newPosts);
                    }
                } else {
                    _errorMessage.setValue("Lỗi lấy dữ liệu: " + response.code());
                    if (isLoadMore) currentPage--;
                }
            }

            @Override
            public void onFailure(Call<List<PostResponse>> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
                if (isLoadMore) currentPage--;
            }
        });
    }

    public void refreshPosts() {
        fetchPosts(currentCategory, currentQuery, currentSortBy, false);
    }

    public void loadMorePosts() {
        fetchPosts(currentCategory, currentQuery, currentSortBy, true);
    }

    public void searchPosts(String query) {
        fetchPosts(currentCategory, query, currentSortBy, false);
    }

    public void selectCategory(String category) {
        fetchPosts(category, currentQuery, currentSortBy, false);
    }

    public void sortPosts(String sortBy) {
        fetchPosts(currentCategory, currentQuery, sortBy, false);
    }

    public void createPost(String category, String title, String content, List<String> tags) {
        _isLoading.setValue(true);
        CreatePostRequest request = new CreatePostRequest(title, content, category, category, tags);
        repository.createPost(category, request).enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful()) {
                    _postCreated.setValue(true);
                } else {
                    _errorMessage.setValue("Lỗi đăng bài: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {
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
                    List<PostResponse> currentList = _posts.getValue();
                    if (currentList != null) {
                        for (PostResponse post : currentList) {
                            if (post.id.equals(postId)) {
                                post.likes = response.body().likesCount; // Đã đồng bộ trường dữ liệu số lượt thích
                                break;
                            }
                        }
                        _posts.setValue(currentList);
                    }
                }
            }

            @Override
            public void onFailure(Call<CommunityActionResponse> call, Throwable t) {
                _errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // 🌟 DÁN ĐÈ HÀM NÀY VÀO CommunityViewModel.java CỦA FRAGMENT
    public void togglePin(String postId) {
        repository.togglePin(postId).enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PostResponse> currentList = _posts.getValue();
                    if (currentList != null) {
                        List<PostResponse> updatedList = new ArrayList<>(currentList);
                        for (int i = 0; i < updatedList.size(); i++) {
                            if (updatedList.get(i).id.equals(postId)) {
                                updatedList.set(i, response.body());
                                break;
                            }
                        }

                        updatedList.sort((p1, p2) -> {
                            if (p1.pinned != p2.pinned) {
                                return Boolean.compare(p2.pinned, p1.pinned);
                            }
                            return p2.id.compareTo(p1.id);
                        });

                        _posts.setValue(updatedList);
                    }
                } else {
                    _errorMessage.setValue("Lỗi thao tác ghim: Mã lỗi " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {
                _errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void removePostLocally(String postId) {
        List<PostResponse> currentList = _posts.getValue();
        if (currentList != null) {
            List<PostResponse> newList = new ArrayList<>(currentList);
            boolean removed = false;
            for (int i = 0; i < newList.size(); i++) {
                if (newList.get(i).id.equals(postId)) {
                    newList.remove(i);
                    removed = true;
                    break;
                }
            }
            if (removed) {
                _posts.setValue(newList);
            }
        }
    }

    public void resetPostCreated() {
        _postCreated.setValue(false);
    }

    private void fetchStats() {
        repository.getCommunityStats().enqueue(new Callback<CommunityStatsResponse>() {
            @Override
            public void onResponse(Call<CommunityStatsResponse> call, Response<CommunityStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    _stats.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<CommunityStatsResponse> call, Throwable t) {
                // Ignore
            }
        });
    }
}
