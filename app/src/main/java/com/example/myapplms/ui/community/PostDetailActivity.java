package com.example.myapplms.ui.community;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.LMSApplication;
import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.response.community_response.CommentResponse;
import com.example.myapplms.data.remote.dto.response.community_response.PostDetailResponse;
import com.example.myapplms.data.remote.dto.response.community_response.PostResponse;
import com.example.myapplms.data.repository.CommunityRepository;
import com.example.myapplms.ui.community.adapter.CommentAdapter;
import com.example.myapplms.utils.SessionManager;
import com.example.myapplms.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {

    private PostDetailViewModel viewModel;
    private String postId;
    private CommentAdapter adapter;
    private final List<CommentResponse> commentList = new ArrayList<>();

    private TextView tvAuthorName, tvPostTime, tvPostContent, tvPostTitleHeader, tvCategory, tvCommentCount, tvViewCount, tvLikesCount;
    private TextView tvRoleChip, tvAuthorIdentity;
    private ImageView ivLike, btnBack, btnMore;
    private EditText etComment;
    private View btnSendComment, layoutPinnedBadgeDetail;
    private com.google.android.material.card.MaterialCardView cardPostDetail;
    private android.widget.LinearLayout layoutDetailTagsContainer, layoutHotBadge;
    private String selectedParentCommentId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        postId = getIntent().getStringExtra("POST_ID");
        if (postId == null) {
            finish();
            return;
        }

        initViews();
        setupViewModel();
        setupRecyclerView();
        setupListeners();
        observeViewModel();

        viewModel.loadDetail(postId);
    }

    private void initViews() {
        tvAuthorName = findViewById(R.id.tvAuthorName);
        tvPostTime = findViewById(R.id.tvPostTime);
        tvPostContent = findViewById(R.id.tvPostContent);
        tvPostTitleHeader = findViewById(R.id.tvPostTitleHeader);
        tvCategory = findViewById(R.id.tvCategory);
        tvCommentCount = findViewById(R.id.tvCommentCount);
        tvViewCount = findViewById(R.id.tvViewCount);
        tvLikesCount = findViewById(R.id.tvLikesCount);
        ivLike = findViewById(R.id.ivLike);
        btnBack = findViewById(R.id.btnBack);
        btnMore = findViewById(R.id.btnMore);
        etComment = findViewById(R.id.etComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        tvRoleChip = findViewById(R.id.tvRoleChip);
        tvAuthorIdentity = findViewById(R.id.tvAuthorIdentity);
        layoutDetailTagsContainer = findViewById(R.id.layoutDetailTagsContainer);
        layoutHotBadge = findViewById(R.id.layoutHotBadge);
        layoutPinnedBadgeDetail = findViewById(R.id.layoutPinnedBadgeDetail);
        cardPostDetail = findViewById(R.id.cardPostDetail);
    }

    private void setupViewModel() {
        LMSApplication app = (LMSApplication) getApplication();
        CommunityRepository repository = new CommunityRepository(app.getRetrofitClient().getApiService());
        CommunityViewModelFactory factory = new CommunityViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(PostDetailViewModel.class);
    }

    private void setupRecyclerView() {
        RecyclerView rvComments = findViewById(R.id.rvComments);
        adapter = new CommentAdapter(commentList, new CommentAdapter.OnCommentClickListener() {
            @Override
            public void onLongClick(CommentResponse comment) {
                showDeleteCommentDialog(comment.commentId);
            }

            @Override
            public void onReplyClick(CommentResponse comment) {
                selectedParentCommentId = comment.commentId;
                findViewById(R.id.layoutReplyIndicator).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.tvReplyingTo)).setText("Đang trả lời " + comment.authorName + "...");
                etComment.requestFocus();
            }
        });
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        ivLike.setOnClickListener(v -> viewModel.toggleLike(postId));
        btnSendComment.setOnClickListener(v -> {
            String content = etComment.getText().toString().trim();
            if (!content.isEmpty()) {
                viewModel.addComment(postId, content, selectedParentCommentId);
                etComment.setText("");
                selectedParentCommentId = null;
                findViewById(R.id.layoutReplyIndicator).setVisibility(View.GONE);
                etComment.setHint("Viết phản hồi của bạn...");
            }
        });
        btnMore.setOnClickListener(v -> showPostMenu());

        findViewById(R.id.btnCancelReply).setOnClickListener(v -> {
            selectedParentCommentId = null;
            findViewById(R.id.layoutReplyIndicator).setVisibility(View.GONE);
            etComment.setHint("Viết phản hồi của bạn...");
        });
    }

    private void observeViewModel() {
        viewModel.postDetail.observe(this, detail -> {
            if (detail != null && !isFinishing()) {
                renderPost(detail);
            }
        });

        viewModel.errorMessage.observe(this, message -> {
            if (message != null && !isFinishing()) {
                if (message.contains("404")) {
                    Toast.makeText(this, "Bài viết không tồn tại hoặc đã bị xóa", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.postDeleted.observe(this, deleted -> {
            if (Boolean.TRUE.equals(deleted) && !isFinishing()) {
                Toast.makeText(this, "Bài viết đã được xóa", Toast.LENGTH_SHORT).show();
                Intent data = new Intent();
                data.putExtra("deletedPostId", postId);
                setResult(RESULT_OK, data);
                finish();
            }
        });

    }

    private void renderPost(PostDetailResponse detail) {
        if (detail == null) return;

        tvAuthorName.setText(detail.authorName != null ? detail.authorName : "Ẩn danh");
        tvPostTime.setText(TimeUtils.formatToRelativeTime(detail.createdAt));
        tvPostContent.setText(detail.content);
        tvPostTitleHeader.setText(detail.title);
        tvCategory.setText(detail.category);
        
        try {
            tvCommentCount.setText(String.format(getString(R.string.comments_count_format), detail.commentsCount));
            tvViewCount.setText(String.format(getString(R.string.views_count_format), detail.views));
            tvLikesCount.setText(String.format(getString(R.string.likes_count_format), detail.likes));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Role Chip logic
        if (detail.authorRole != null && !detail.authorRole.isEmpty()) {
            tvRoleChip.setVisibility(View.VISIBLE);
            tvRoleChip.setText(detail.authorRole);
        } else {
            tvRoleChip.setVisibility(View.GONE);
        }

        // Author identity logic
        SessionManager sessionManager = new SessionManager(this);
        String currentUserId = sessionManager.getUserId();
        if (currentUserId != null && currentUserId.equals(detail.userId)) {
            tvAuthorIdentity.setVisibility(View.VISIBLE);
        } else {
            tvAuthorIdentity.setVisibility(View.GONE);
        }

        ivLike.setImageResource(R.drawable.ic_heart);
        ivLike.setColorFilter(detail.likedByMe ? getResources().getColor(R.color.accent) : getResources().getColor(R.color.text_secondary));

        // BỔ SUNG: Xử lý Ghim trong Detail
        if (detail.pinned) {
            layoutPinnedBadgeDetail.setVisibility(View.VISIBLE);
            cardPostDetail.setCardBackgroundColor(android.graphics.Color.parseColor("#FFFBEB"));
            cardPostDetail.setStrokeColor(android.graphics.Color.parseColor("#FDE68A"));
        } else {
            layoutPinnedBadgeDetail.setVisibility(View.GONE);
            cardPostDetail.setCardBackgroundColor(android.graphics.Color.WHITE);
            cardPostDetail.setStrokeColor(getResources().getColor(R.color.indigo_100));
        }

        // Hot Badge Logic
        boolean isHot = detail.views > 30 || detail.likes > 5 || detail.commentsCount > 3;
        if (layoutHotBadge != null) {
            layoutHotBadge.setVisibility(isHot ? View.VISIBLE : View.GONE);
        }

        // 🌟 ĐOẠN VIẾT MỚI: Xóa sạch tag cứng cũ và vẽ tag động từ MongoDB trả về
        if (layoutDetailTagsContainer != null) {
            layoutDetailTagsContainer.removeAllViews(); // Xóa sạch để tránh trùng lặp khi re-render

            if (detail.tags != null && !detail.tags.isEmpty()) {
                for (String tag : detail.tags) {
                    if (tag == null || tag.trim().isEmpty()) continue;

                    // Khởi tạo TextView cho từng tag bằng code Java
                    TextView tvTag = new TextView(this);
                    
                    // Chuẩn hóa định dạng dấu # cho tag
                    if (!tag.trim().startsWith("#")) {
                        tvTag.setText("#" + tag.trim());
                    } else {
                        tvTag.setText(tag.trim());
                    }

                    tvTag.setTextSize(12);
                    tvTag.setTextColor(android.graphics.Color.parseColor("#4B5563")); // Màu chữ xám đậm
                    
                    // Gán background bo góc xám nhạt của dự án
                    tvTag.setBackgroundResource(R.drawable.bg_chip_unselected); 

                    // Thiết lập khoảng cách giữa các thẻ tag
                    android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 0, 16, 0); // Cách thẻ bên phải 16dp
                    tvTag.setLayoutParams(params);
                    tvTag.setPadding(20, 10, 20, 10); // Padding trong cho chữ thoáng

                    tvTag.setClickable(true);
                    tvTag.setFocusable(true);
                    tvTag.setOnClickListener(v -> {
                        // Trả về kết quả search cho Activity/Fragment cha
                        Intent data = new Intent();
                        data.putExtra("searchTag", tag.trim());
                        setResult(RESULT_OK, data);
                        finish();
                    });

                    layoutDetailTagsContainer.addView(tvTag); // Đẩy vào layout hiển thị
                }
            }
        }

        commentList.clear();
        if (detail.comments != null) {
            commentList.addAll(detail.comments);
        }
        adapter.notifyDataSetChanged();
    }

    private void showPostMenu() {
        PostDetailResponse currentPost = viewModel.postDetail.getValue();
        if (currentPost == null) return;

        SessionManager sessionManager = new SessionManager(this);
        String userRole = sessionManager.getRole();

        List<String> options = new ArrayList<>();
        options.add("Sửa bài viết");
        options.add("Xóa bài viết");

        boolean canPin = "ADMIN".equalsIgnoreCase(userRole) || "TEACHER".equalsIgnoreCase(userRole);
        if (canPin) {
            options.add(currentPost.pinned ? "Bỏ ghim bài viết" : "Ghim bài viết");
        }

        new AlertDialog.Builder(this)
                .setItems(options.toArray(new String[0]), (dialog, which) -> {
                    String selected = options.get(which);
                    if (selected.equals("Sửa bài viết")) {
                        showEditPostDialog(currentPost);
                    } else if (selected.equals("Xóa bài viết")) {
                        showDeletePostDialog();
                    } else if (selected.contains("ghim")) {
                        togglePinDetail();
                    }
                })
                .show();
    }

    private void togglePinDetail() {
        LMSApplication app = (LMSApplication) getApplication();
        CommunityRepository repository = new CommunityRepository(app.getRetrofitClient().getApiService());
        repository.togglePin(postId).enqueue(new retrofit2.Callback<PostResponse>() {
            @Override
            public void onResponse(retrofit2.Call<PostResponse> call, retrofit2.Response<PostResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(PostDetailActivity.this, "Đã cập nhật trạng thái ghim", Toast.LENGTH_SHORT).show();
                    viewModel.loadDetail(postId); // Refresh detail
                    setResult(RESULT_OK); // Notify list to refresh
                }
            }

            @Override
            public void onFailure(retrofit2.Call<PostResponse> call, Throwable t) {
                Toast.makeText(PostDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 2. VIẾT MỚI HÀM TÁI SỬ DỤNG DIALOG ĐĂNG BÀI ĐỂ LÀM GIAO DIỆN SỬA BÀI VIẾT
    private void showEditPostDialog(PostDetailResponse currentPost) {
        View dialogView = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_create_post, null);
        AlertDialog dialog = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
                .setView(dialogView)
                .create();

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etContent = dialogView.findViewById(R.id.etContent);
        EditText etTags = dialogView.findViewById(R.id.etTags);
        android.widget.Spinner spinner = dialogView.findViewById(R.id.spinnerCategory);
        android.widget.Button btnCreate = dialogView.findViewById(R.id.btnCreate);

        etTitle.setText(currentPost.title);
        etContent.setText(currentPost.content);
        btnCreate.setText("Lưu thay đổi");

        // 🌟 FIX LỖI ĐỎ: Gọi trực tiếp .tags thay vì hàm .getTags() không tồn tại
        if (currentPost.tags != null && !currentPost.tags.isEmpty()) {
            etTags.setText(String.join(", ", currentPost.tags));
        }

        String[] categories = {"Khóa học", "Lập trình", "Hỏi đáp", "Thảo luận"};
        android.widget.ArrayAdapter<String> categoryAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinner.setAdapter(categoryAdapter);

        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equalsIgnoreCase(currentPost.category)) {
                spinner.setSelection(i);
                break;
            }
        }

        dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String newTitle = etTitle.getText().toString().trim();
            String newContent = etContent.getText().toString().trim();
            String newCategory = spinner.getSelectedItem().toString();
            String tagsString = etTags.getText().toString().trim();

            if (newTitle.isEmpty() || newContent.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> newTags = new java.util.ArrayList<>();
            if (!tagsString.isEmpty()) {
                newTags = java.util.Arrays.asList(tagsString.split("\\s*,\\s*"));
            }

            LMSApplication app = (LMSApplication) getApplication();
            CommunityRepository repository = new CommunityRepository(app.getRetrofitClient().getApiService());

            // 🌟 ĐỒNG BỘ REQUEST: Sử dụng CreatePostRequest khớp 100% với định nghĩa trong LmsApiService.java của bạn
            com.example.myapplms.data.remote.dto.request.CreatePostRequest updateReq =
                    new com.example.myapplms.data.remote.dto.request.CreatePostRequest(newTitle, newContent, newCategory, newCategory, newTags);

            repository.updatePost(currentPost.id, updateReq).enqueue(new retrofit2.Callback<PostResponse>() {
                @Override
                public void onResponse(retrofit2.Call<PostResponse> call, retrofit2.Response<PostResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(PostDetailActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        viewModel.loadDetail(currentPost.id);
                        setResult(RESULT_OK);
                    } else {
                        Toast.makeText(PostDetailActivity.this, "Lỗi phản hồi: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<PostResponse> call, Throwable t) {
                    Toast.makeText(PostDetailActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showDeletePostDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa bài viết")
                .setMessage("Bạn có chắc chắn muốn xóa bài viết này?")
                .setPositiveButton("Xóa", (dialog, which) -> viewModel.deletePost(postId))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteCommentDialog(String commentId) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa bình luận")
                .setMessage("Bạn có chắc chắn muốn xóa bình luận này?")
                .setPositiveButton("Xóa", (dialog, which) -> viewModel.deleteComment(postId, commentId))
                .setNegativeButton("Hủy", null)
                .show();
    }
}
