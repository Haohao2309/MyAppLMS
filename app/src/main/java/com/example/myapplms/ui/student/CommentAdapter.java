package com.example.myapplms.ui.student;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplms.R;
import com.example.myapplms.model.Comment;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.tvUserName.setText(comment.getUserName());
        holder.tvTime.setText(comment.getTimeAgo());
        holder.tvContent.setText(comment.getContent());
        holder.tvLikeCount.setText(String.valueOf(comment.getLikeCount()));

        holder.tvInstructorBadge.setVisibility(comment.isInstructor() ? View.VISIBLE : View.GONE);
        holder.tvAuthorBadge.setVisibility(comment.isAuthor() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvTime, tvContent, tvLikeCount, tvInstructorBadge, tvAuthorBadge;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvInstructorBadge = itemView.findViewById(R.id.tvInstructorBadge);
            tvAuthorBadge = itemView.findViewById(R.id.tvAuthorBadge);
        }
    }
}
