package com.example.myapplms.ui.community.adapter;

import androidx.core.content.ContextCompat;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.response.community_response.PostResponse;
import com.example.myapplms.utils.TimeUtils;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private final List<PostResponse> postList;
    private final OnPostClickListener listener;
    private final String currentUserId;
    private final String userRole;

    public interface OnPostClickListener {
        void onPostClick(PostResponse post);
        void onLikeClick(PostResponse post);
        void onDeleteClick(PostResponse post);
        void onTagClick(String tag);
    }

    public PostAdapter(List<PostResponse> postList, OnPostClickListener listener, String currentUserId, String userRole) {
        this.postList = postList;
        this.listener = listener;
        this.currentUserId = currentUserId;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_community_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostResponse post = postList.get(position);

        // 1. Đổ dữ liệu text cơ bản
        holder.tvUserName.setText(post.authorName != null ? post.authorName : "Ẩn danh");
        holder.tvTime.setText(TimeUtils.formatToRelativeTime(post.createdAt));
        holder.tvPostTitle.setText(post.title);
        holder.tvViews.setText(String.valueOf(post.views));
        holder.tvLikes.setText(String.valueOf(post.likes));
        holder.tvComments.setText(String.valueOf(post.commentsCount));

        // 2. Xử lý Tags động
        holder.layoutTagsContainer.removeAllViews();
        if (post.tags != null && !post.tags.isEmpty()) {
            android.content.Context context = holder.itemView.getContext();
            for (String tag : post.tags) {
                if (tag == null || tag.trim().isEmpty()) continue;

                TextView tvTag = new TextView(context);
                String formattedTag = tag.trim();
                if (!formattedTag.startsWith("#")) {
                    formattedTag = "#" + formattedTag;
                }
                tvTag.setText(formattedTag);
                tvTag.setTextSize(12);
                tvTag.setTextColor(Color.parseColor("#6B7280"));
                tvTag.setBackgroundResource(R.drawable.bg_chip_unselected);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 8, 0);
                tvTag.setLayoutParams(params);
                tvTag.setPadding(24, 8, 24, 8);

                tvTag.setClickable(true);
                tvTag.setFocusable(true);
                tvTag.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onTagClick(tag.trim()); //
                    }
                });

                holder.layoutTagsContainer.addView(tvTag);
            }
        }

        // 3. Xử lý Author Role Chip (Đã tối ưu triệt để tránh lỗi lem màu khi cuộn)
        if (post.authorRole != null && !post.authorRole.trim().isEmpty()) {
            holder.tvRoleChip.setVisibility(View.VISIBLE);
            holder.tvRoleChip.setText(post.authorRole.trim());

            android.content.Context context = holder.itemView.getContext();
            if ("TEACHER".equalsIgnoreCase(post.authorRole)) {
                holder.tvRoleChip.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.primary_light));
                holder.tvRoleChip.setTextColor(ContextCompat.getColor(context, R.color.primary));
            } else if ("ADMIN".equalsIgnoreCase(post.authorRole)) {
                holder.tvRoleChip.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.accent));
                holder.tvRoleChip.setTextColor(ContextCompat.getColor(context, R.color.white));
            } else {
                // Ép lại màu mặc định rõ ràng cho STUDENT để tránh bị dính màu của item khác khi cuộn
                holder.tvRoleChip.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.divider));
                holder.tvRoleChip.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            }
        } else {
            holder.tvRoleChip.setVisibility(View.GONE);
        }

        // 4. Định danh "Tác giả" bài viết & Ẩn hiện nút sửa xóa
        boolean isAuthor = currentUserId != null && currentUserId.equals(post.userId);
        if (isAuthor) {
            holder.tvAuthorIdentity.setVisibility(View.VISIBLE);
        } else {
            holder.tvAuthorIdentity.setVisibility(View.GONE);
        }
        
        if (holder.btnMore != null) {
            boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole);
            holder.btnMore.setVisibility((isAuthor || isAdmin) ? View.VISIBLE : View.GONE);
        }

        // 5. Hot Badge Logic
        boolean isHot = post.views > 30 || post.likes > 5 || post.commentsCount > 3;
        if (holder.layoutHotBadge != null) {
            holder.layoutHotBadge.setVisibility(isHot ? View.VISIBLE : View.GONE);
        }

        holder.cardPost.setCardBackgroundColor(Color.WHITE);
        holder.cardPost.setStrokeColor(Color.parseColor("#E5E7EB")); // Mặc định gray-200
        holder.layoutPinnedBadge.setVisibility(View.GONE);

        // BỔ SUNG: Xử lý màu sắc nút Like
        if (holder.ivLike != null) {
            holder.ivLike.setImageResource(R.drawable.ic_heart);
            if (post.likedByMe) {
                holder.ivLike.setColorFilter(Color.parseColor("#EF4444")); // Red color for Like
            } else {
                holder.ivLike.setColorFilter(Color.parseColor("#6B7280")); // text_secondary
            }
        }

        // 6. Gán sự kiện Click xử lý tập trung
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onPostClick(postList.get(pos));
            }
        });

        // Giả định bạn đã chuyển btnLike và btnMore vào khai báo trong lớp ViewHolder
        if (holder.btnLike != null) {
            holder.btnLike.setOnClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onLikeClick(postList.get(pos));
                }
            });
        }

        if (holder.btnMore != null) {
            holder.btnMore.setOnClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(postList.get(pos));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvTime, tvPostTitle, tvViews, tvLikes, tvComments;
        TextView tvRoleChip, tvAuthorIdentity;
        ImageView ivLike;
        LinearLayout layoutTagsContainer, layoutHotBadge, layoutPinnedBadge;
        com.google.android.material.card.MaterialCardView cardPost;

        View btnLike;
        View btnMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvPostTitle = itemView.findViewById(R.id.tvPostTitle);
            tvViews = itemView.findViewById(R.id.tvViews);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            tvComments = itemView.findViewById(R.id.tvComments);
            tvRoleChip = itemView.findViewById(R.id.tvRoleChip);
            tvAuthorIdentity = itemView.findViewById(R.id.tvAuthorIdentity);
            ivLike = itemView.findViewById(R.id.ivLike);
            layoutTagsContainer = itemView.findViewById(R.id.layoutTagsContainer);
            layoutHotBadge = itemView.findViewById(R.id.layoutHotBadge);
            layoutPinnedBadge = itemView.findViewById(R.id.layoutPinnedBadge);
            cardPost = itemView.findViewById(R.id.cardPost);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
