package com.example.myapplms.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplms.R;
import com.example.myapplms.model.Course;

import java.util.ArrayList;
import java.util.List;

public class ContinueLearningAdapter extends RecyclerView.Adapter<ContinueLearningAdapter.ViewHolder> {

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    private final List<ContinueLearningItem> items = new ArrayList<>();
    private final OnCourseClickListener listener;

    public ContinueLearningAdapter(OnCourseClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ContinueLearningItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_continue_learning, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContinueLearningItem item = items.get(position);
        Course course = item.course;

        holder.tvTitle.setText(course.title != null ? course.title : "Chưa có tên");
        holder.tvInstructor.setText(course.instructor != null ? course.instructor : "");
        holder.tvDuration.setText(course.duration != null ? course.duration : "0h 0m");
        holder.tvLessons.setText(course.lessons != null ? course.lessons : "0 lessons");

        int progressInt = (int) Math.round(item.progressPercent);
        holder.progressBar.setProgress(progressInt);
        holder.tvProgress.setText(progressInt + "%");

        Glide.with(holder.itemView.getContext())
                .load(course.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(holder.ivThumbnail);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCourseClick(course);
        });
        holder.btnPlay.setOnClickListener(v -> {
            if (listener != null) listener.onCourseClick(course);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle, tvInstructor, tvDuration, tvLessons, tvProgress;
        ProgressBar progressBar;
        ImageButton btnPlay;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail  = itemView.findViewById(R.id.ivCourseThumbnail);
            tvTitle      = itemView.findViewById(R.id.tvCourseTitle);
            tvInstructor = itemView.findViewById(R.id.tvInstructor);
            tvDuration   = itemView.findViewById(R.id.tvDuration);
            tvLessons    = itemView.findViewById(R.id.tvLessons);
            progressBar  = itemView.findViewById(R.id.progressBar);
            tvProgress   = itemView.findViewById(R.id.tvProgress);
            btnPlay      = itemView.findViewById(R.id.btnPlay);
        }
    }

    /** Data class ghép Course + tiến độ học */
    public static class ContinueLearningItem {
        public final Course course;
        public final double progressPercent;

        public ContinueLearningItem(Course course, double progressPercent) {
            this.course = course;
            this.progressPercent = progressPercent;
        }
    }
}
