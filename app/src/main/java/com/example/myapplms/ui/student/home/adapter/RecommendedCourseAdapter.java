package com.example.myapplms.ui.student.home.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplms.R;
import com.example.myapplms.databinding.ItemHomeRecommendedCourseBinding;
import com.example.myapplms.model.Course;

import java.util.ArrayList;
import java.util.List;

public class RecommendedCourseAdapter extends RecyclerView.Adapter<RecommendedCourseAdapter.ViewHolder> {
    private List<Course> courses = new ArrayList<>();
    private final OnCourseClickListener listener;

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    public RecommendedCourseAdapter(OnCourseClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<Course> newCourses) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return courses.size();
            }

            @Override
            public int getNewListSize() {
                return newCourses.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return courses.get(oldItemPosition).id == newCourses.get(newItemPosition).id;
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Course oldCourse = courses.get(oldItemPosition);
                Course newCourse = newCourses.get(newItemPosition);
                return oldCourse.title.equals(newCourse.title) &&
                        oldCourse.priceText.equals(newCourse.priceText) &&
                        oldCourse.rating.equals(newCourse.rating);
            }
        });
        this.courses = new ArrayList<>(newCourses);
        result.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeRecommendedCourseBinding binding = ItemHomeRecommendedCourseBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.bind(course, listener);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemHomeRecommendedCourseBinding binding;
        ViewHolder(ItemHomeRecommendedCourseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Course course, OnCourseClickListener listener) {
            binding.getRoot().setOnClickListener(v -> listener.onCourseClick(course));
            
            binding.tvCourseTitle.setText(course.title);
            binding.tvInstructor.setText(course.instructor);
            binding.tvPrice.setText(course.priceText);
            binding.tvRating.setText(course.rating);
            binding.tvDuration.setText("· " + course.duration);

            Glide.with(binding.ivCourseImage.getContext())
                    .load(course.imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(binding.ivCourseImage);
        }
    }
}
