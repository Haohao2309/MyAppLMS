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

public class FeaturedCourseAdapter extends RecyclerView.Adapter<FeaturedCourseAdapter.ViewHolder> {

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    private final List<Course> courses = new ArrayList<>();
    private final OnCourseClickListener listener;

    public FeaturedCourseAdapter(OnCourseClickListener listener) {
        this.listener = listener;
    }

    public void setCourses(List<Course> newCourses) {
        courses.clear();
        if (newCourses != null) {
            int limit = Math.min(newCourses.size(), 8);
            courses.addAll(newCourses.subList(0, limit));
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_featured_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courses.get(position);

        holder.tvTitle.setText(course.title != null ? course.title : "Chưa có tên");
        holder.tvInstructor.setText(course.instructor != null ? course.instructor : "");
        holder.tvRating.setText(course.rating != null ? course.rating : "0.0");
        holder.tvStudents.setText(course.students != null ? course.students : "0 students");
        holder.tvLessons.setText(course.lessons != null ? course.lessons : "0 lessons");
        holder.tvDuration.setText(course.duration != null ? course.duration : "0h 0m");
        holder.tvCategory.setText(course.category != null ? course.category : "General");
        holder.tvLevel.setText(course.level != null ? course.level : "Beginner");

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
        TextView tvTitle, tvInstructor, tvRating, tvStudents, tvLessons, tvDuration, tvCategory, tvLevel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail   = itemView.findViewById(R.id.ivFeaturedThumbnail);
            tvTitle       = itemView.findViewById(R.id.tvFeaturedTitle);
            tvInstructor  = itemView.findViewById(R.id.tvFeaturedInstructor);
            tvRating      = itemView.findViewById(R.id.tvFeaturedRating);
            tvStudents    = itemView.findViewById(R.id.tvFeaturedStudents);
            tvLessons     = itemView.findViewById(R.id.tvFeaturedLessons);
            tvDuration    = itemView.findViewById(R.id.tvFeaturedDuration);
            tvCategory    = itemView.findViewById(R.id.tvFeaturedCategory);
            tvLevel       = itemView.findViewById(R.id.tvFeaturedLevel);
        }
    }
}
