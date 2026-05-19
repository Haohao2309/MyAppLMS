package com.example.myapplms.ui.community;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.example.myapplms.data.remote.dto.response.CommentResponse;
import com.example.myapplms.data.remote.dto.response.PostDetailResponse;
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
    private View btnSendComment;

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
    }

    private void setupViewModel() {
        LMSApplication app = (LMSApplication) getApplication();
        CommunityRepository repository = new CommunityRepository(app.getRetrofitClient().getApiService());
        CommunityViewModelFactory factory = new CommunityViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(PostDetailViewModel.class);
    }

    private void setupRecyclerView() {
        RecyclerView rvComments = findViewById(R.id.rvComments);
        adapter = new CommentAdapter(commentList, comment -> showDeleteCommentDialog(comment.commentId));
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        ivLike.setOnClickListener(v -> viewModel.toggleLike(postId));
        btnSendComment.setOnClickListener(v -> {
            String content = etComment.getText().toString().trim();
            if (!content.isEmpty()) {
                viewModel.addComment(postId, content);
                etComment.setText("");
            }
        });
        btnMore.setOnClickListener(v -> showPostMenu());
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

        commentList.clear();
        if (detail.comments != null) {
            commentList.addAll(detail.comments);
        }
        adapter.notifyDataSetChanged();
    }

    private void showPostMenu() {
        String[] options = {"Xóa bài viết"};
        new AlertDialog.Builder(this)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showDeletePostDialog();
                    }
                })
                .show();
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
