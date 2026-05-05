package com.example.myapplms.ui.student;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplms.R;
import com.example.myapplms.model.Post;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;

    public PostAdapter(List<Post> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.tvUserName.setText(post.getUserName());
        holder.tvTime.setText("• " + post.getTimeAgo());
        holder.tvTitle.setText(post.getTitle());
        holder.tvCommentCount.setText(String.valueOf(post.getCommentCount()));
        holder.tvLikeCount.setText(String.valueOf(post.getLikeCount()));
        holder.tvViewCount.setText(post.getViewCount() + " lượt xem");

        holder.tvHotBadge.setVisibility(post.isHot() ? View.VISIBLE : View.GONE);

        holder.cgTags.removeAllViews();
        for (String tag : post.getTags()) {
            Chip chip = new Chip(holder.itemView.getContext());
            chip.setText(tag);
            
            // Sử dụng ColorStateList để đổi màu nền (Trắng -> Xanh khi nhấn)
            int[][] states = new int[][] {
                new int[] { android.R.attr.state_pressed}, // pressed
                new int[] {}  // default
            };
            int[] colors = new int[] {
                holder.itemView.getContext().getResources().getColor(R.color.primary, null),
                holder.itemView.getContext().getResources().getColor(R.color.white, null)
            };
            chip.setChipBackgroundColor(new android.content.res.ColorStateList(states, colors));
            
            // Đổi màu chữ (Xám -> Trắng khi nhấn)
            int[] textColors = new int[] {
                holder.itemView.getContext().getResources().getColor(R.color.white, null),
                holder.itemView.getContext().getResources().getColor(R.color.text_secondary, null)
            };
            chip.setTextColor(new android.content.res.ColorStateList(states, textColors));
            
            // Thiết kế viền xám mặc định
            chip.setChipStrokeColorResource(R.color.divider);
            chip.setChipStrokeWidth(2f);

            chip.setTextSize(12);
            chip.setClickable(true);
            chip.setFocusable(true);
            holder.cgTags.addView(chip);
        }

        // Bắt sự kiện click vào item để mở chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PostDetailActivity.class);
            // Có thể truyền dữ liệu bài viết qua Intent nếu cần
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvTime, tvTitle, tvCommentCount, tvLikeCount, tvViewCount, tvHotBadge;
        ChipGroup cgTags;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvViewCount = itemView.findViewById(R.id.tvViewCount);
            tvHotBadge = itemView.findViewById(R.id.tvHotBadge);
            cgTags = itemView.findViewById(R.id.cgTags);
        }
    }
}
