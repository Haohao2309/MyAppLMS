package com.example.myapplms.ui.student.course_detail.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.response.ReviewResponse;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final List<ReviewResponse> reviews;

    public ReviewAdapter(List<ReviewResponse> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewResponse review = reviews.get(position);

        holder.tvName.setText(review.studentName != null ? review.studentName : "Học viên");
        holder.tvRating.setText(String.valueOf(review.rating) + " ⭐");
        holder.tvTitle.setText(review.title);
        holder.tvContent.setText(review.content);
        holder.tvUpvotes.setText("Hữu ích: " + (review.upvotes != null ? review.upvotes : 0));
    }

    @Override
    public int getItemCount() {
        return reviews != null ? reviews.size() : 0;
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRating, tvTitle, tvContent, tvUpvotes;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_reviewer_name);
            tvRating = itemView.findViewById(R.id.tv_review_rating);
            tvTitle = itemView.findViewById(R.id.tv_review_title);
            tvContent = itemView.findViewById(R.id.tv_review_content);
            tvUpvotes = itemView.findViewById(R.id.tv_review_upvotes);
        }
    }
}