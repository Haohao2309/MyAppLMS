package com.example.myapplms;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplms.model.Course;

import java.util.ArrayList;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    private List<Course> courseList;
    private final OnCourseClickListener listener;
    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    // 3. CẬP NHẬT CONSTRUCTOR ĐỂ NHẬN LISTENER
    public CourseAdapter(List<Course> courseList, OnCourseClickListener listener) {
        this.courseList = courseList;
        this.listener = listener;
    }
    public void updateData(List<Course> newCourses) {
        if (this.courseList != newCourses) {
            this.courseList.clear();
            this.courseList.addAll(newCourses);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courseList.get(position);

        holder.tvCourseName.setText(course.title != null ? course.title : "Chưa có tên");
        holder.tvInstructor.setText(course.instructor);
        holder.tvRating.setText(course.rating);
        holder.tvStudents.setText(course.students);
        holder.tvLessons.setText(course.lessons);
        holder.tvDuration.setText(course.duration);
        holder.tvPrice.setText(course.priceText);
        holder.tvCategory.setText(course.category);
        holder.tvLevel.setText(course.level);

        Glide.with(holder.itemView.getContext())
                .load(course.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(holder.ivThumbnail);

        // 4. BẮT SỰ KIỆN KHI NGƯỜI DÙNG BẤM VÀO ITEM KHÓA HỌC
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCourseClick(course);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (courseList != null) {
            return courseList.size();
        }
        return 0;
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