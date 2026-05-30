package com.example.myapplms.ui.teacher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.R;
import com.example.myapplms.model.Course;

import java.util.List;

public class TeacherCourseAdapter extends RecyclerView.Adapter<TeacherCourseAdapter.ViewHolder> {

    public interface OnEditClickListener {
        void onEdit(Course course);
    }

    private final List<Course> courseList;
    private final OnEditClickListener onEditClick;

    public TeacherCourseAdapter(List<Course> courseList, OnEditClickListener onEditClick) {
        this.courseList  = courseList;
        this.onEditClick = onEditClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courseList.get(position);

        holder.tvTitle.setText(course.title != null ? course.title : "");
        holder.tvCategory.setText(course.category != null ? course.category : "");
        holder.tvPrice.setText(course.priceText != null ? course.priceText : "FREE");
        holder.tvStudents.setText(course.students != null ? course.students : "");
        holder.tvLessons.setText(course.lessons != null ? course.lessons : "");
        holder.tvDuration.setText(course.duration != null ? course.duration : "");
        holder.tvStatus.setText("Published");

        // Truyền toàn bộ Course object để CourseFormActivity có đủ data điền vào form
        holder.btnEdit.setOnClickListener(v -> {
            if (onEditClick != null) onEditClick.onEdit(course);
        });
    }

    @Override
    public int getItemCount() {
        return courseList != null ? courseList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvPrice, tvStudents, tvLessons, tvDuration, tvStatus;
        ImageButton btnEdit;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle    = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice    = itemView.findViewById(R.id.tvPrice);
            tvStudents = itemView.findViewById(R.id.tvStudents);
            tvLessons  = itemView.findViewById(R.id.tvLessons);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvStatus   = itemView.findViewById(R.id.tvStatus);
            btnEdit    = itemView.findViewById(R.id.btnEdit);
        }
    }
}