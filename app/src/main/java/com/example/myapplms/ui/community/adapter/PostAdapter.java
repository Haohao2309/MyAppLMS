package com.example.myapplms.ui.community.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.response.PostResponse;
import com.example.myapplms.utils.SessionManager;
import com.example.myapplms.util.TimeUtils;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private final List<PostResponse> postList;
    private final OnPostClickListener listener;
    private final String currentUserId;

    public interface OnPostClickListener {
        void onPostClick(PostResponse post);
        void onLikeClick(PostResponse post);
        void onDeleteClick(PostResponse post);
    }

    public PostAdapter(List<PostResponse> postList, OnPostClickListener listener, String currentUserId) {
        this.postList = postList;
        this.listener = listener;
        this.currentUserId = currentUserId;
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
        holder.tvUserName.setText(post.authorName != null ? post.authorName : "Ẩn danh");
        holder.tvTime.setText(TimeUtils.formatToRelativeTime(post.createdAt));
        holder.tvPostTitle.setText(post.title);
        holder.tvViews.setText(String.valueOf(post.views));
        holder.tvLikes.setText(String.valueOf(post.likes));
        holder.tvComments.setText(String.valueOf(post.commentsCount));

        // Author Role Chip
        if (post.authorRole != null && !post.authorRole.isEmpty()) {
            holder.tvRoleChip.setVisibility(View.VISIBLE);
            holder.tvRoleChip.setText(post.authorRole);
            // Optional: Change color based on role
            if ("TEACHER".equals(post.authorRole)) {
                holder.tvRoleChip.setBackgroundTintList(holder.itemView.getContext().getResources().getColorStateList(R.color.primary_light));
                holder.tvRoleChip.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.primary));
            } else if ("ADMIN".equals(post.authorRole)) {
                holder.tvRoleChip.setBackgroundTintList(holder.itemView.getContext().getResources().getColorStateList(R.color.accent));
                holder.tvRoleChip.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.white));
            } else {
                holder.tvRoleChip.setBackgroundTintList(holder.itemView.getContext().getResources().getColorStateList(R.color.divider));
                holder.tvRoleChip.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_secondary));
            }
        } else {
            holder.tvRoleChip.setVisibility(View.GONE);
        }

        // "Tác giả" Chip
        if (currentUserId != null && currentUserId.equals(post.userId)) {
            holder.tvAuthorIdentity.setVisibility(View.VISIBLE);
        } else {
            holder.tvAuthorIdentity.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onPostClick(postList.get(pos));
            }
        });
        
        View btnLike = holder.itemView.findViewById(R.id.btnLike);
        if (btnLike != null) {
            btnLike.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onLikeClick(postList.get(pos));
                }
            });
        }

        View btnMore = holder.itemView.findViewById(R.id.btnMore);
        if (btnMore != null) {
            btnMore.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
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
        }
    }
}
