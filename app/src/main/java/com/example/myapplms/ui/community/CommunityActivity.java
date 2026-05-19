package com.example.myapplms.ui.community;

import android.os.Bundle;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.R;
import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.request.CreatePostRequest;
import com.example.myapplms.data.remote.dto.response.PostResponse;
import com.example.myapplms.ui.community.adapter.PostAdapter;
import com.example.myapplms.utils.SessionManager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityActivity extends AppCompatActivity {

    private PostAdapter adapter;
    private final List<PostResponse> postList = new ArrayList<>();
    private int currentPage = 1;
    private final int pageSize = 10;
    private boolean isLastPage = false;
    private boolean isLoading = false;
    private String currentQuery = null;
    private String currentCategory = null;

    private final ActivityResultLauncher<Intent> detailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.hasExtra("deletedPostId")) {
                        String deletedId = data.getStringExtra("deletedPostId");
                        // Xóa thủ công khỏi list hiện tại để biến mất ngay
                        for (int i = 0; i < postList.size(); i++) {
                            if (postList.get(i).id.equals(deletedId)) {
                                postList.remove(i);
                                adapter.notifyItemRemoved(i);
                                break;
                            }
                        }
                    } else {
                        // Refresh từ server nếu có tín hiệu chung hoặc lỗi 404
                        fetchPostsFromBackend(false);

        EditText etSearch = findViewById(R.id.etSearch);
        if (etSearch != null) {
            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                currentQuery = etSearch.getText().toString().trim();
                fetchPostsFromBackend(false);
                return true;
            });
        }

        View tabAll = findViewById(R.id.tabAll);
        if (tabAll != null) tabAll.setOnClickListener(v -> {
            currentCategory = null;
            fetchPostsFromBackend(false);
        });

        View tabCourse = findViewById(R.id.tabCourse);
        if (tabCourse != null) tabCourse.setOnClickListener(v -> {
            currentCategory = "Khóa học";
            fetchPostsFromBackend(false);
        });

        View tabTech = findViewById(R.id.tabTech);
        if (tabTech != null) tabTech.setOnClickListener(v -> {
            currentCategory = "Lập trình";
            fetchPostsFromBackend(false);
        });
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        if (findViewById(R.id.btnBack) != null) {
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_community);

            BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.nav_notifications);
            badge.setVisible(true);
            badge.setNumber(3);
        }

        RecyclerView rvPosts = findViewById(R.id.rvPosts);
        SessionManager sessionManager = new SessionManager(this);
        String currentUserId = sessionManager.getUserId();

        adapter = new PostAdapter(postList, new PostAdapter.OnPostClickListener() {
            @Override
            public void onPostClick(PostResponse post) {
                Intent intent = new Intent(CommunityActivity.this, PostDetailActivity.class);
                intent.putExtra("POST_ID", post.id);
                detailLauncher.launch(intent);
            }

            @Override
            public void onLikeClick(PostResponse post) {
                // Implement like logic if needed here or via ViewModel
            }

            @Override
            public void onDeleteClick(PostResponse post) {
                // Implement delete logic if needed here
            }
        }, currentUserId);
        rvPosts.setLayoutManager(new LinearLayoutManager(this));
        rvPosts.setAdapter(adapter);

        rvPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastVisibleItemPosition() == postList.size() - 1) {
                    fetchPostsFromBackend(true);
                }
            }
        });

        fetchPostsFromBackend(false);

        EditText etSearch = findViewById(R.id.etSearch);
        if (etSearch != null) {
            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                currentQuery = etSearch.getText().toString().trim();
                fetchPostsFromBackend(false);
                return true;
            });
        }

        View tabAll = findViewById(R.id.tabAll);
        if (tabAll != null) tabAll.setOnClickListener(v -> {
            currentCategory = null;
            fetchPostsFromBackend(false);
        });

        View tabCourse = findViewById(R.id.tabCourse);
        if (tabCourse != null) tabCourse.setOnClickListener(v -> {
            currentCategory = "Khóa học";
            fetchPostsFromBackend(false);
        });

        View tabTech = findViewById(R.id.tabTech);
        if (tabTech != null) tabTech.setOnClickListener(v -> {
            currentCategory = "Lập trình";
            fetchPostsFromBackend(false);
        });

        View btnAddPost = findViewById(R.id.btnAddPost);
        if (btnAddPost != null) {
            btnAddPost.setOnClickListener(v -> showCreatePostDialog());
        }
    }

    private void fetchPostsFromBackend(boolean isLoadMore) {
        if (isLoading || (isLoadMore && isLastPage)) return;

        if (!isLoadMore) {
            currentPage = 1;
            isLastPage = false;
        } else {
            currentPage++;
        }

        isLoading = true;
        LmsApiService apiService =
                ((com.example.myapplms.LMSApplication) getApplication())
                        .getRetrofitClient()
                        .getApiService();

        apiService.getPosts(currentCategory, currentQuery, currentPage, pageSize).enqueue(new Callback<List<PostResponse>>() {
            @Override
            public void onResponse(Call<List<PostResponse>> call, Response<List<PostResponse>> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    List<PostResponse> newPosts = response.body();
                    if (newPosts.size() < pageSize) {
                        isLastPage = true;
                    }

                    if (!isLoadMore) {
                        postList.clear();
                    }
                    postList.addAll(newPosts);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(CommunityActivity.this,
                            "Lỗi tải bài: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    if (isLoadMore) currentPage--;
                }
            }

            @Override
            public void onFailure(Call<List<PostResponse>> call, Throwable t) {
                isLoading = false;
                Toast.makeText(CommunityActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                if (isLoadMore) currentPage--;
            }
        });
    }

    private void showCreatePostDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_post, null);

        AlertDialog dialog = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
                .setView(dialogView)
                .create();

        Spinner spinner = dialogView.findViewById(R.id.spinnerCategory);
        String[] categories = {"Khóa học", "Lập trình", "Hỏi đáp", "Thảo luận"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinner.setAdapter(categoryAdapter);

        dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnCreate).setOnClickListener(v -> {
            String title = ((EditText) dialogView.findViewById(R.id.etTitle)).getText().toString().trim();
            String content = ((EditText) dialogView.findViewById(R.id.etContent)).getText().toString().trim();
            String category = spinner.getSelectedItem().toString().trim();
            String tagsString = ((EditText) dialogView.findViewById(R.id.etTags)).getText().toString().trim();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tiêu đề và nội dung", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> tags = null;
            if (!tagsString.isEmpty()) {
                tags = Arrays.asList(tagsString.split("\\s*,\\s*"));
            }

            CreatePostRequest req = new CreatePostRequest(
                    title,
                    content,
                    category,
                    category,
                    tags
            );

            LmsApiService apiService =
                    ((com.example.myapplms.LMSApplication) getApplication())
                            .getRetrofitClient()
                            .getApiService();

            apiService.createPost(category, req).enqueue(new Callback<PostResponse>() {
                @Override
                public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(CommunityActivity.this, "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        fetchPostsFromBackend(false);

        EditText etSearch = findViewById(R.id.etSearch);
        if (etSearch != null) {
            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                currentQuery = etSearch.getText().toString().trim();
                fetchPostsFromBackend(false);
                return true;
            });
        }

        View tabAll = findViewById(R.id.tabAll);
        if (tabAll != null) tabAll.setOnClickListener(v -> {
            currentCategory = null;
            fetchPostsFromBackend(false);
        });

        View tabCourse = findViewById(R.id.tabCourse);
        if (tabCourse != null) tabCourse.setOnClickListener(v -> {
            currentCategory = "Khóa học";
            fetchPostsFromBackend(false);
        });

        View tabTech = findViewById(R.id.tabTech);
        if (tabTech != null) tabTech.setOnClickListener(v -> {
            currentCategory = "Lập trình";
            fetchPostsFromBackend(false);
        });
                    } else {
                        Toast.makeText(CommunityActivity.this,
                                "Đăng bài lỗi: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<PostResponse> call, Throwable t) {
                    Toast.makeText(CommunityActivity.this,
                            "Lỗi kết nối: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
}
