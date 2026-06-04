package com.example.myapplms.ui.student.course_detail.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplms.R;
import com.example.myapplms.model.course_content.CourseLesson;
import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private final List<CourseLesson> lessonList;

    public LessonAdapter(List<CourseLesson> lessonList) {
        this.lessonList = lessonList;
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

        // Chuyển đổi duration (giây) thành phút:giây (ví dụ: 300s -> 5:00)
        int minutes = lesson.duration / 60;
        int seconds = lesson.duration % 60;
        holder.tvDuration.setText(String.format("%d:%02d", minutes, seconds));

        // Nếu isPreview = true thì hiện thẻ "Free", ngược lại ẩn đi
        if (lesson.isPreview) {
            holder.tvPreviewBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvPreviewBadge.setVisibility(View.GONE);
        }

        // TODO: Bắt sự kiện click vào bài học để chuyển sang màn hình Video Player
        holder.itemView.setOnClickListener(v -> {
            // Callback click
        });
    }

    @Override
    public int getItemCount() {
        return lessonList != null ? lessonList.size() : 0;
    }

    static class LessonViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDuration, tvPreviewBadge;

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_lesson_title);
            tvDuration = itemView.findViewById(R.id.tv_lesson_duration);
            tvPreviewBadge = itemView.findViewById(R.id.tv_preview_badge);
        }
    }
}