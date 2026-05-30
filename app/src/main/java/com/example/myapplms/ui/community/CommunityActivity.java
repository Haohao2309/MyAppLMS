package com.example.myapplms.ui.community;

import android.os.Bundle;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.R;
import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.request.CreatePostRequest;
import com.example.myapplms.data.remote.dto.response.community_response.CommunityActionResponse;
import com.example.myapplms.data.remote.dto.response.community_response.CommunityStatsResponse;
import com.example.myapplms.data.remote.dto.response.community_response.PostResponse;
import com.example.myapplms.ui.community.adapter.PostAdapter;
import com.example.myapplms.ui.community.adapter.SortSpinnerAdapter;
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
    private String currentSortBy = "newest";
    private LmsApiService apiService;

    private TextView tvStatMembers, tvStatTopics, tvStatReplies, tvStatOnline;

    private final ActivityResultLauncher<Intent> detailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.hasExtra("deletedPostId")) {
                        String deletedId = data.getStringExtra("deletedPostId");
                        for (int i = 0; i < postList.size(); i++) {
                            if (postList.get(i).id.equals(deletedId)) {
                                postList.remove(i);
                                adapter.notifyItemRemoved(i);
                                break;
                            }
                        }
                    } else if (data != null && data.hasExtra("searchTag")) {
                        String tag = data.getStringExtra("searchTag");
                        onTagClickFromDetail(tag);
                    } else {
                        fetchPostsFromBackend(false);
                    }
                }
            }
    );

    private void onTagClickFromDetail(String tag) {
        EditText etSearch = findViewById(R.id.etSearch);
        if (etSearch != null) {
            etSearch.setText(tag);
        }
        currentQuery = tag;
        fetchPostsFromBackend(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        apiService = ((com.example.myapplms.LMSApplication) getApplication())
                .getRetrofitClient()
                .getApiService();

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

        // HOÀN THIỆN CHỨC NĂNG: Xử lý sự kiện Like và Xóa ngay trên danh sách của Activity
        adapter = new PostAdapter(postList, new PostAdapter.OnPostClickListener() {
            @Override
            public void onPostClick(PostResponse post) {
                Intent intent = new Intent(CommunityActivity.this, PostDetailActivity.class);
                intent.putExtra("POST_ID", post.id);
                detailLauncher.launch(intent);
            }

            @Override
            public void onLikeClick(PostResponse post) {
                apiService.toggleLike(post.id).enqueue(new Callback<CommunityActionResponse>() {
                    @Override
                    public void onResponse(Call<CommunityActionResponse> call, Response<CommunityActionResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            post.likes = response.body().likesCount;

                            // Tìm vị trí của post vừa like trong danh sách để ép cập nhật riêng item đó
                            int position = postList.indexOf(post);
                            if (position != -1) {
                                adapter.notifyItemChanged(position);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<CommunityActionResponse> call, Throwable t) {
                        Toast.makeText(CommunityActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onDeleteClick(PostResponse post) {
                showPostActionMenu(post);
            }

            @Override
            public void onTagClick(String tag) {
                EditText etSearch = findViewById(R.id.etSearch);
                if (etSearch != null) {
                    etSearch.setText(tag);
                }
                currentQuery = tag;
                fetchPostsFromBackend(false);
            }

            @Override
            public void onPinClick(PostResponse post) {
                togglePin(post);
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

        // Đã quy hoạch tập trung Listener tại đây, tránh trùng lặp mã nguồn
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
            updateTabUI(null);
        });

        View tabCourse = findViewById(R.id.tabCourse);
        if (tabCourse != null) tabCourse.setOnClickListener(v -> {
            currentCategory = "Khóa học";
            fetchPostsFromBackend(false);
            updateTabUI("Khóa học");
        });

        View tabTech = findViewById(R.id.tabTech);
        if (tabTech != null) tabTech.setOnClickListener(v -> {
            currentCategory = "Lập trình";
            fetchPostsFromBackend(false);
            updateTabUI("Lập trình");
        });

        View tabQna = findViewById(R.id.tabQna);
        if (tabQna != null) tabQna.setOnClickListener(v -> {
            currentCategory = "Hỏi đáp";
            fetchPostsFromBackend(false);
            updateTabUI("Hỏi đáp");
        });

        View tabDiscussion = findViewById(R.id.tabDiscussion);
        if (tabDiscussion != null) tabDiscussion.setOnClickListener(v -> {
            currentCategory = "Thảo luận";
            fetchPostsFromBackend(false);
            updateTabUI("Thảo luận");
        });

        // Initial state
        updateTabUI(null);

        View btnAddPost = findViewById(R.id.btnAddPost);
        if (btnAddPost != null) {
            btnAddPost.setOnClickListener(v -> showCreatePostDialog());
        }

        tvStatMembers = findViewById(R.id.tvStatMembers);
        tvStatTopics = findViewById(R.id.tvStatTopics);
        tvStatReplies = findViewById(R.id.tvStatReplies);
        tvStatOnline = findViewById(R.id.tvStatOnline);

        setupSortSpinner();

        fetchPostsFromBackend(false);
        fetchCommunityStats();
    }

    private void setupSortSpinner() {
        Spinner spinnerSort = findViewById(R.id.spinnerSort);
        String[] sortOptions = {"Mới nhất", "Trending", "Nhiều phản hồi"};
        SortSpinnerAdapter sortAdapter = new SortSpinnerAdapter(this, sortOptions);
        spinnerSort.setAdapter(sortAdapter);

        spinnerSort.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                sortAdapter.setSelectedPosition(position);
                String newSortBy;
                switch (position) {
                    case 1: newSortBy = "trending"; break;
                    case 2: newSortBy = "replies"; break;
                    default: newSortBy = "newest"; break;
                }
                if (!newSortBy.equals(currentSortBy)) {
                    currentSortBy = newSortBy;
                    fetchPostsFromBackend(false);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void fetchCommunityStats() {
        apiService.getCommunityStats().enqueue(new Callback<CommunityStatsResponse>() {
            @Override
            public void onResponse(Call<CommunityStatsResponse> call, Response<CommunityStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CommunityStatsResponse stats = response.body();
                    tvStatMembers.setText(String.valueOf(stats.totalMembers));
                    tvStatTopics.setText(String.valueOf(stats.totalTopics));
                    tvStatReplies.setText(String.valueOf(stats.totalReplies));
                    tvStatOnline.setText(String.valueOf(stats.liveOnlineCount));

                    TextView tvTopicCount = findViewById(R.id.tvTopicCount);
                    if (tvTopicCount != null) {
                        tvTopicCount.setText(stats.totalTopics + " chủ đề");
                    }
                }
            }

            @Override
            public void onFailure(Call<CommunityStatsResponse> call, Throwable t) {
                // Silently ignore
            }
        });
    }

    private void updateTabUI(String selectedCategory) {
        View btnAll = findViewById(R.id.tabAll);
        View btnCourse = findViewById(R.id.tabCourse);
        View btnTech = findViewById(R.id.tabTech);
        View btnQna = findViewById(R.id.tabQna);
        View btnDiscussion = findViewById(R.id.tabDiscussion);

        updateTabState(btnAll, selectedCategory == null);
        updateTabState(btnCourse, "Khóa học".equals(selectedCategory));
        updateTabState(btnTech, "Lập trình".equals(selectedCategory));
        updateTabState(btnQna, "Hỏi đáp".equals(selectedCategory));
        updateTabState(btnDiscussion, "Thảo luận".equals(selectedCategory));
    }

    private void updateTabState(View tab, boolean isSelected) {
        if (tab == null) return;

        tab.setBackgroundResource(isSelected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);

        if (tab instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) tab;
            int color = isSelected ?
                    ContextCompat.getColor(this, R.color.white) :
                    ContextCompat.getColor(this, R.color.text_secondary);

            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof ImageView) {
                    ((ImageView) child).setColorFilter(color);
                } else if (child instanceof TextView) {
                    ((TextView) child).setTextColor(color);
                }
            }
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
        apiService.getPosts(currentCategory, currentQuery, currentSortBy, currentPage, pageSize).enqueue(new Callback<List<PostResponse>>() {
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
                    Toast.makeText(CommunityActivity.this, "Lỗi tải bài: " + response.code(), Toast.LENGTH_SHORT).show();
                    if (isLoadMore) currentPage--;
                }
            }

            @Override
            public void onFailure(Call<List<PostResponse>> call, Throwable t) {
                isLoading = false;
                Toast.makeText(CommunityActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                if (isLoadMore) currentPage--;
            }
        });
    }

    // 🌟 DÁN ĐÈ LOGIC NÀY VÀO HÀM togglePin TRONG FILE CommunityActivity.java TIÊU CHUẨN
    private void togglePin(PostResponse post) {
        apiService.togglePin(post.id).enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 1. Cập nhật dữ liệu mới từ Server trả về vào List Local
                    for (int i = 0; i < postList.size(); i++) {
                        if (postList.get(i).id.equals(post.id)) {
                            postList.set(i, response.body());
                            break;
                        }
                    }

                    // 2. Thuật toán sắp xếp kép: Ưu tiên cờ pinned lên đầu, nếu cùng ghim thì thằng nào ID lớn hơn (mới hơn) đứng trước
                    postList.sort((p1, p2) -> {
                        if (p1.pinned != p2.pinned) {
                            return Boolean.compare(p2.pinned, p1.pinned);
                        }
                        return p2.id.compareTo(p1.id);
                    });

                    // 3. Ép cập nhật lại toàn bộ giao diện màn hình
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(CommunityActivity.this, "Lỗi ghim bài: Quyền không hợp lệ hoặc lỗi " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {
                Toast.makeText(CommunityActivity.this, "Lỗi kết nối hệ thống", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPostActionMenu(PostResponse post) {
        SessionManager sessionManager = new SessionManager(this);
        String userRole = sessionManager.getRole();

        List<String> options = new ArrayList<>();
        options.add("Sửa bài viết");
        options.add("Xóa bài viết");

        boolean canPin = "ADMIN".equalsIgnoreCase(userRole) || "TEACHER".equalsIgnoreCase(userRole);
        if (canPin) {
            options.add(post.pinned ? "Bỏ ghim bài viết" : "Ghim bài viết");
        }

        new AlertDialog.Builder(this)
                .setTitle("Lựa chọn")
                .setItems(options.toArray(new String[0]), (dialog, which) -> {
                    String selected = options.get(which);
                    if (selected.equals("Sửa bài viết")) {
                        showEditPostDialog(post);
                    } else if (selected.equals("Xóa bài viết")) {
                        confirmDeletePost(post);
                    } else if (selected.contains("ghim")) {
                        togglePin(post);
                    }
                })
                .show();
    }

    private void confirmDeletePost(PostResponse post) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa bài viết")
                .setMessage("Bạn có chắc chắn muốn xóa bài viết này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    apiService.deletePost(post.id).enqueue(new Callback<CommunityActionResponse>() {
                        @Override
                        public void onResponse(Call<CommunityActionResponse> call, Response<CommunityActionResponse> response) {
                            if (response.isSuccessful()) {
                                postList.remove(post);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(CommunityActivity.this, "Đã xóa bài viết", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<CommunityActionResponse> call, Throwable t) {
                            Toast.makeText(CommunityActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditPostDialog(PostResponse currentPost) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_post, null);
        AlertDialog dialog = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
                .setView(dialogView)
                .create();

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etContent = dialogView.findViewById(R.id.etContent);
        EditText etTags = dialogView.findViewById(R.id.etTags);
        Spinner spinner = dialogView.findViewById(R.id.spinnerCategory);

        etTitle.setText(currentPost.title);
        etContent.setText(currentPost.content);

        if (currentPost.tags != null && !currentPost.tags.isEmpty()) {
            etTags.setText(String.join(", ", currentPost.tags));
        }

        String[] categories = {"Khóa học", "Lập trình", "Hỏi đáp", "Thảo luận"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinner.setAdapter(categoryAdapter);

        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equalsIgnoreCase(currentPost.category)) {
                spinner.setSelection(i);
                break;
            }
        }

        android.widget.Button btnCreate = dialogView.findViewById(R.id.btnCreate);
        btnCreate.setText("Lưu thay đổi");

        dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String newTitle = etTitle.getText().toString().trim();
            String newContent = etContent.getText().toString().trim();
            String newCategory = spinner.getSelectedItem().toString();
            String tagsString = etTags.getText().toString().trim();

            if (newTitle.isEmpty() || newContent.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ tiêu đề và nội dung", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> newTags = new java.util.ArrayList<>();
            if (!tagsString.isEmpty()) {
                newTags = java.util.Arrays.asList(tagsString.split("\\s*,\\s*"));
            }

            CreatePostRequest updateReq = new CreatePostRequest(newTitle, newContent, newCategory, newCategory, newTags);

            apiService.updatePost(currentPost.id, updateReq).enqueue(new Callback<PostResponse>() {
                @Override
                public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(CommunityActivity.this, "Cập nhật bài viết thành công!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        fetchPostsFromBackend(false);
                    } else {
                        Toast.makeText(CommunityActivity.this, "Lỗi cập nhật: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<PostResponse> call, Throwable t) {
                    Toast.makeText(CommunityActivity.this, "Lỗi kết nối hệ thống", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
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

            List<String> tags = new ArrayList<>();
            if (!tagsString.isEmpty()) {
                tags = Arrays.asList(tagsString.split("\\s*,\\s*"));
            }

            CreatePostRequest req = new CreatePostRequest(title, content, category, category, tags);

            apiService.createPost(category, req).enqueue(new Callback<PostResponse>() {
                @Override
                public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(CommunityActivity.this, "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        fetchPostsFromBackend(false);
                    } else {
                        Toast.makeText(CommunityActivity.this, "Đăng bài lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<PostResponse> call, Throwable t) {
                    Toast.makeText(CommunityActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
}