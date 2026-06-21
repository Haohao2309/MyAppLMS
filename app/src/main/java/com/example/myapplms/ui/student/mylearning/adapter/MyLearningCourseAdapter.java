package com.example.myapplms.ui.student.mylearning.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplms.R;
import com.example.myapplms.databinding.ItemMyLearningCourseBinding;
import com.example.myapplms.model.Course;
import java.util.List;

public class MyLearningCourseAdapter extends RecyclerView.Adapter<MyLearningCourseAdapter.ViewHolder> {
    private List<Course> courses;
    private final OnCourseClickListener listener;

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    public MyLearningCourseAdapter(List<Course> courses, OnCourseClickListener listener) {
        this.courses = courses;
        this.listener = listener;
    }

    public void updateData(List<Course> newCourses) {
        this.courses = newCourses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyLearningCourseBinding binding = ItemMyLearningCourseBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) listener.onCourseClick(course);
        });
        holder.binding.tvCourseTitle.setText(course.title);
        holder.binding.pbCourseProgress.setProgress(course.progressPercent);
        holder.binding.tvProgressPercent.setText(course.progressPercent + "%");

        int totalLessons = 10;
        try {
            if (course.lessons != null) {
                String digits = course.lessons.replaceAll("\\D+", "");
                if (!digits.isEmpty()) {
                    totalLessons = Integer.parseInt(digits);
                }
            }
        } catch (Exception ignored) {}
        int completed = (course.progressPercent * totalLessons) / 100;
        holder.binding.tvLessonsCount.setText(completed + " Lesson / " + totalLessons + " Lesson");
        


        Glide.with(holder.binding.ivCourseImage.getContext())
                .load(course.imageRes != 0 ? course.imageRes : R.drawable.ic_launcher_background)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.binding.ivCourseImage);
    }

    @Override
    public int getItemCount() {
        return courses == null ? 0 : courses.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemMyLearningCourseBinding binding;
        ViewHolder(ItemMyLearningCourseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
