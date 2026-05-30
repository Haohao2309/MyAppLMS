package com.example.myapplms.ui.community.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.response.community_response.CommentResponse;
import com.example.myapplms.util.TimeUtils;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private final List<CommentResponse> commentList;
    private final OnCommentClickListener listener;

    public interface OnCommentClickListener {
        void onLongClick(CommentResponse comment);
        void onReplyClick(CommentResponse comment);
    }

    public CommentAdapter(List<CommentResponse> commentList, OnCommentClickListener listener) {
        this.commentList = commentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommentResponse comment = commentList.get(position);
        
        // Visual cue for replies
        if (comment.parentCommentId != null && !comment.parentCommentId.isEmpty()) {
            holder.tvAuthor.setText("↳ " + comment.authorName);
        } else {
            holder.tvAuthor.setText(comment.authorName);
        }
        
        holder.tvContent.setText(comment.content);
        holder.tvTime.setText(TimeUtils.formatToRelativeTime(comment.createdAt));

        // Indentation for nested comments chuẩn DP (48dp for clear visibility)
        float scale = holder.itemView.getContext().getResources().getDisplayMetrics().density;
        int indentInPx = (int) (48 * scale + 0.5f);
        int defaultMarginInPx = (int) (12 * scale + 0.5f);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.rootLayout.getLayoutParams();
        
        if (comment.parentCommentId != null && !comment.parentCommentId.isEmpty()) {
            // Nếu là phản hồi: Thụt lề trái 48dp + đổi background nhẹ
            params.setMargins(indentInPx, 4, defaultMarginInPx, 4);
            holder.itemView.findViewById(R.id.layoutCommentBubble).setBackgroundTintList(
                    androidx.core.content.ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.gray_50)
            );
        } else {
            // Nếu là bình luận gốc: Để margin 12dp + background trắng/mặc định
            params.setMargins(defaultMarginInPx, 12, defaultMarginInPx, 12);
            holder.itemView.findViewById(R.id.layoutCommentBubble).setBackgroundTintList(null);
        }
        holder.rootLayout.setLayoutParams(params);

        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(comment);
            return true;
        });

        holder.tvReply.setOnClickListener(v -> listener.onReplyClick(comment));
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthor, tvContent, tvTime, tvReply;
        View rootLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tvAuthorName);
            tvContent = itemView.findViewById(R.id.tvCommentContent);
            tvTime = itemView.findViewById(R.id.tvCommentTime);
            tvReply = itemView.findViewById(R.id.tvReply);
            rootLayout = itemView.findViewById(R.id.rootCommentLayout);
        }
    }
}
