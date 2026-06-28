package com.example.myapplms.ui.teacher.grading;

import static com.example.myapplms.utils.TimeUtils.formatToRelativeTime;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.response.GradingListResponse;
import com.google.android.material.chip.Chip;

import java.util.List;

public class SubmissionListAdapter extends RecyclerView.Adapter<SubmissionListAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onClick(GradingListResponse.StudentGradingItem item);
    }

    private List<GradingListResponse.StudentGradingItem> items;
    private final OnItemClickListener listener;

    public SubmissionListAdapter(List<GradingListResponse.StudentGradingItem> items,
                                 OnItemClickListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    public void updateData(List<GradingListResponse.StudentGradingItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_submission, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        h.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime, tvScore, tvAvatarInitial;
        Chip chipStatus;

        ViewHolder(@NonNull View v) {
            super(v);
            tvName          = v.findViewById(R.id.tv_student_name);
            tvTime          = v.findViewById(R.id.tv_submit_time);
            tvScore         = v.findViewById(R.id.tv_score);
            chipStatus      = v.findViewById(R.id.chip_status);
            tvAvatarInitial = v.findViewById(R.id.tv_avatar_initial);
        }

        void bind(GradingListResponse.StudentGradingItem item, OnItemClickListener listener) {
            String name = item.getFullName() != null ? item.getFullName() : "Sinh viên #" + item.getStudentId();
            tvName.setText(name);
            tvAvatarInitial.setText(name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase());

            // Thời gian nộp
            tvTime.setText(item.getSubmittedAt() != null
                    ? formatToRelativeTime(item.getSubmittedAt()) : "Chưa nộp");

            // Trạng thái
            if (item.isGraded()) {
                chipStatus.setText("Đã chấm");
                chipStatus.setChipBackgroundColorResource(R.color.success_light);
                chipStatus.setTextColor(itemView.getContext().getColor(R.color.success));
                if (item.getFinalScore() != null) {
                    tvScore.setVisibility(View.VISIBLE);
                    tvScore.setText(String.format("%.1f/100", item.getFinalScore()));
                } else {
                    tvScore.setVisibility(View.GONE);
                }
            } else {
                chipStatus.setText("Chờ chấm");
                chipStatus.setChipBackgroundColorResource(R.color.warning_light);
                chipStatus.setTextColor(itemView.getContext().getColor(R.color.warning));
                tvScore.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onClick(item));
        }

        private String formatRelativeTime(String isoTime) {
            try {
                java.time.OffsetDateTime dt = java.time.OffsetDateTime.parse(isoTime);
                long mins = java.time.Duration.between(dt.toInstant(), java.time.Instant.now()).toMinutes();
                if (mins < 60)   return mins + " phút trước";
                if (mins < 1440) return (mins / 60) + " giờ trước";
                return (mins / 1440) + " ngày trước";
            } catch (Exception e) {
                return isoTime;
            }
        }
    }
}