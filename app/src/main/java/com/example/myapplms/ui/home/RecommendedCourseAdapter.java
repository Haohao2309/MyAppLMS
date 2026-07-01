package com.example.myapplms.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplms.R;
import com.example.myapplms.model.Course;

import java.util.ArrayList;
import java.util.List;

public class RecommendedCourseAdapter extends RecyclerView.Adapter<RecommendedCourseAdapter.ViewHolder> {

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    private final List<Course> courses = new ArrayList<>();
    private final OnCourseClickListener listener;

    public RecommendedCourseAdapter(OnCourseClickListener listener) {
        this.listener = listener;
    }

    public void setCourses(List<Course> newCourses) {
        courses.clear();
        if (newCourses != null) {
            courses.addAll(newCourses);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommended_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courses.get(position);

        holder.tvTitle.setText(course.title != null ? course.title : "Chưa có tên");
        holder.tvInstructor.setText(course.instructor != null ? course.instructor : "");
        holder.tvRating.setText(course.rating != null ? course.rating : "0.0");
        holder.tvPrice.setText(course.priceText != null ? course.priceText : "FREE");
        holder.tvDuration.setText(course.duration != null ? course.duration : "0h 0m");

        Glide.with(holder.itemView.getContext())
                .load(course.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(holder.ivThumbnail);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCourseClick(course);
        });
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle, tvInstructor, tvRating, tvPrice, tvDuration;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail  = itemView.findViewById(R.id.ivThumbnail);
            tvTitle      = itemView.findViewById(R.id.tvTitle);
            tvInstructor = itemView.findViewById(R.id.tvInstructor);
            tvRating     = itemView.findViewById(R.id.tvRating);
            tvPrice      = itemView.findViewById(R.id.tvPrice);
            tvDuration   = itemView.findViewById(R.id.tvDuration);
        }
    }
}
