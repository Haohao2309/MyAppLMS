package com.example.myapplms.ui.student.course_detail.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplms.R;
import com.example.myapplms.model.course_content.CourseLesson;
import java.util.ArrayList;
import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private final List<CourseLesson> lessonList;
    private final OnLessonClickListener listener;
    private List<String> completedLessons = new ArrayList<>();

    public interface OnLessonClickListener {
        void onLessonClick(CourseLesson lesson);
    }

    public LessonAdapter(List<CourseLesson> lessonList, OnLessonClickListener listener) {
        this.lessonList = lessonList;
        this.listener = listener;
    }

    public void setCompletedLessons(List<String> completedLessons) {
        if (completedLessons != null) {
            this.completedLessons = completedLessons;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        CourseLesson lesson = lessonList.get(position);

        holder.tvTitle.setText(lesson.title);

        // Kiểm tra an toàn danh sách bài đã học để gán icon tích tròn chuẩn UX
        if (completedLessons != null && completedLessons.contains(lesson.lessonId)) {
            holder.ivIcon.setImageResource(R.drawable.ic_check_circle);
            int successColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.success_green);
            holder.ivIcon.setColorFilter(successColor);
            holder.tvTitle.setTextColor(successColor);
        } else {
            int textPrimaryColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary);
            int textHintColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.text_hint);

            holder.tvTitle.setTextColor(textPrimaryColor);
            holder.ivIcon.setColorFilter(textHintColor);

            if ("video".equalsIgnoreCase(lesson.type)) {
                holder.ivIcon.setImageResource(R.drawable.ic_play_circle);
            } else if ("quiz".equalsIgnoreCase(lesson.type)) {
                holder.ivIcon.setImageResource(R.drawable.ic_help_outline);
            } else {
                holder.ivIcon.setImageResource(R.drawable.ic_assignment);
            }
        }

        int minutes = lesson.duration / 60;
        int seconds = lesson.duration % 60;
        holder.tvDuration.setText(String.format("%d:%02d", minutes, seconds));

        // ĐÃ SỬA: lesson.isPreview là kiểu primitive boolean, gọi trực tiếp không check null
        if (lesson.isPreview) {
            holder.tvPreviewBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvPreviewBadge.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLessonClick(lesson);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lessonList != null ? lessonList.size() : 0;
    }

    static class LessonViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDuration, tvPreviewBadge;
        ImageView ivIcon; // ĐÃ SỬA: Khai báo ivIcon đầy đủ

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_lesson_title);
            tvDuration = itemView.findViewById(R.id.tv_lesson_duration);
            tvPreviewBadge = itemView.findViewById(R.id.tv_preview_badge);
            ivIcon = itemView.findViewById(R.id.iv_lesson_icon); // ĐÃ SỬA: Ánh xạ ivIcon từ XML
        }
    }
}