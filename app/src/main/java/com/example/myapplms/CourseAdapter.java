package com.example.myapplms;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    private List<Course> courseList;

    public CourseAdapter(List<Course> courseList) {
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.tvCourseName.setText(course.getName());
        holder.tvInstructor.setText(course.getInstructor());
        holder.tvRating.setText(course.getRating());
        holder.tvStudents.setText(course.getStudents());
        holder.tvLessons.setText(course.getLessons());
        holder.tvDuration.setText(course.getDuration());
        holder.tvPrice.setText(course.getPrice());
        holder.tvCategory.setText(course.getCategory());
        holder.tvLevel.setText(course.getLevel());
        holder.ivThumbnail.setImageResource(course.getImageResId());
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvCourseName, tvInstructor, tvRating, tvStudents, tvLessons, tvDuration, tvPrice, tvCategory, tvLevel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvInstructor = itemView.findViewById(R.id.tvInstructor);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvStudents = itemView.findViewById(R.id.tvStudents);
            tvLessons = itemView.findViewById(R.id.tvLessons);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvLevel = itemView.findViewById(R.id.tvLevel);
        }
    }
}
