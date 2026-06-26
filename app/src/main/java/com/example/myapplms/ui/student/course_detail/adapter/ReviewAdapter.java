package com.example.myapplms.ui.student.course_detail.adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.response.ReviewResponse;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    public interface OnReviewVoteListener {
        void onUpvoteClick(ReviewResponse review);
        void onDownvoteClick(ReviewResponse review);
    }

    private final List<ReviewResponse> reviews;
    private final List<String> upvotedIds;
    private final List<String> downvotedIds;
    private final OnReviewVoteListener listener;

    public ReviewAdapter(List<ReviewResponse> reviews, List<String> upvotedIds,
                         List<String> downvotedIds, OnReviewVoteListener listener) {
        this.reviews = reviews;
        this.upvotedIds = upvotedIds;
        this.downvotedIds = downvotedIds;
        this.listener = listener;
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

        String name = review.studentName != null ? review.studentName.trim() : "Học viên";
        holder.tvName.setText(name);

        // 1. Tạo avatar tròn và màu pastel động từ tên học viên
        String initial = "H";
        if (!name.isEmpty()) {
            String[] parts = name.split("\\s+");
            if (parts.length > 0) {
                String lastPart = parts[parts.length - 1];
                if (!lastPart.isEmpty()) {
                    initial = lastPart.substring(0, 1).toUpperCase();
                }
            }
        }
        holder.tvAvatar.setText(initial);

        int hash = name.hashCode();
        int[] pastelColors = {
            0xFF818CF8, // Indigo 400
            0xFFF472B6, // Pink 400
            0xFFFB7185, // Rose 400
            0xFF38BDF8, // Light Blue 400
            0xFF34D399, // Emerald 400
            0xFFFBBF24, // Amber 400
            0xFFA78BFA, // Violet 400
            0xFFF87171  // Red 400
        };
        int color = pastelColors[Math.abs(hash) % pastelColors.length];
        GradientDrawable avatarDrawable = new GradientDrawable();
        avatarDrawable.setShape(GradientDrawable.OVAL);
        avatarDrawable.setColor(color);
        holder.tvAvatar.setBackground(avatarDrawable);

        // 2. Định dạng ngày đánh giá (ISO String -> DD-MM-YYYY)
        String dateStr = review.createdAt;
        if (dateStr != null && dateStr.length() >= 10) {
            try {
                String datePart = dateStr.substring(0, 10); // YYYY-MM-DD
                String[] dateParts = datePart.split("-");
                if (dateParts.length == 3) {
                    dateStr = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];
                }
            } catch (Exception ignored) {}
        }
        holder.tvDate.setText(dateStr != null ? dateStr : "");

        // 3. Số lượng sao dạng ký tự ★
        double ratingVal = review.rating != null ? review.rating : 5.0;
        int ratingInt = (int) Math.round(ratingVal);
        StringBuilder starBuilder = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= ratingInt) {
                starBuilder.append("★");
            } else {
                starBuilder.append("☆");
            }
        }
        holder.tvRating.setText(starBuilder.toString());

        // 4. Tiêu đề và Nội dung
        holder.tvTitle.setText(review.title);
        holder.tvContent.setText(review.content);

        // 5. Đổ danh sách Điểm tốt (Pros)
        holder.layoutPros.removeAllViews();
        if (review.pros != null && !review.pros.isEmpty()) {
            boolean hasValidPro = false;
            for (String pro : review.pros) {
                if (pro != null && !pro.trim().isEmpty()) {
                    hasValidPro = true;
                    TextView tvPro = new TextView(holder.itemView.getContext());
                    tvPro.setText("✓ " + pro.trim());
                    tvPro.setTextColor(0xFF16A34A); // màu xanh lá gray_600/emerald
                    tvPro.setTextSize(12);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(0, 4, 0, 4);
                    tvPro.setLayoutParams(lp);
                    holder.layoutPros.addView(tvPro);
                }
            }
            holder.layoutPros.setVisibility(hasValidPro ? View.VISIBLE : View.GONE);
        } else {
            holder.layoutPros.setVisibility(View.GONE);
        }

        // 6. Đổ danh sách Điểm cần cải thiện (Cons)
        holder.layoutCons.removeAllViews();
        if (review.cons != null && !review.cons.isEmpty()) {
            boolean hasValidCon = false;
            for (String con : review.cons) {
                if (con != null && !con.trim().isEmpty()) {
                    hasValidCon = true;
                    TextView tvCon = new TextView(holder.itemView.getContext());
                    tvCon.setText("⚠ " + con.trim());
                    tvCon.setTextColor(0xFFEA580C); // màu cam warning/amber
                    tvCon.setTextSize(12);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(0, 4, 0, 4);
                    tvCon.setLayoutParams(lp);
                    holder.layoutCons.addView(tvCon);
                }
            }
            holder.layoutCons.setVisibility(hasValidCon ? View.VISIBLE : View.GONE);
        } else {
            holder.layoutCons.setVisibility(View.GONE);
        }

        // 7. Upvote button — hiển thị số và highlight nếu đã vote
        int upCount = review.upvotes != null ? review.upvotes : 0;
        holder.tvUpvotes.setText("👍 " + upCount);

        boolean isUpvoted = upvotedIds != null && upvotedIds.contains(review.id);
        if (isUpvoted) {
            holder.tvUpvotes.setBackgroundResource(R.drawable.bg_badge_indigo);
            holder.tvUpvotes.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.white));
        } else {
            holder.tvUpvotes.setBackgroundResource(R.drawable.bg_upvote_chip);
            holder.tvUpvotes.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.course_text_body));
        }

        holder.tvUpvotes.setOnClickListener(v -> {
            if (listener != null) listener.onUpvoteClick(review);
        });

        // 8. Downvote button — hiển thị số và highlight nếu đã vote
        int downCount = review.downvotes != null ? review.downvotes : 0;
        holder.tvDownvotes.setText("👎 " + downCount);

        boolean isDownvoted = downvotedIds != null && downvotedIds.contains(review.id);
        if (isDownvoted) {
            holder.tvDownvotes.setBackgroundResource(R.drawable.bg_badge_red);
            holder.tvDownvotes.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.white));
        } else {
            holder.tvDownvotes.setBackgroundResource(R.drawable.bg_upvote_chip);
            holder.tvDownvotes.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.course_text_body));
        }

        holder.tvDownvotes.setOnClickListener(v -> {
            if (listener != null) listener.onDownvoteClick(review);
        });
    }

    @Override
    public int getItemCount() {
        return reviews != null ? reviews.size() : 0;
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvDate, tvRating, tvTitle, tvContent, tvUpvotes, tvDownvotes;
        LinearLayout layoutPros, layoutCons;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tv_reviewer_avatar);
            tvName = itemView.findViewById(R.id.tv_reviewer_name);
            tvDate = itemView.findViewById(R.id.tv_review_date);
            tvRating = itemView.findViewById(R.id.tv_review_rating);
            tvTitle = itemView.findViewById(R.id.tv_review_title);
            tvContent = itemView.findViewById(R.id.tv_review_content);
            tvUpvotes = itemView.findViewById(R.id.tv_review_upvotes);
            tvDownvotes = itemView.findViewById(R.id.tv_review_downvotes);
            layoutPros = itemView.findViewById(R.id.layout_pros);
            layoutCons = itemView.findViewById(R.id.layout_cons);
        }
    }
}