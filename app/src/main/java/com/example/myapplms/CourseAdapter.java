package com.example.myapplms;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.model.Course; // Đảm bảo import đúng package chứa model Course

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    private List<Course> courseList;

    public CourseAdapter(List<Course> courseList) {
        this.courseList = courseList;
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

        // Đã sửa lại các thuộc tính cho khớp với file Course.java
        // Lưu ý: Nếu trong Course.java bạn để public các biến, hãy dùng course.title
        // Nếu bạn dùng private và tạo getter, hãy dùng course.getTitle()

        holder.tvCourseName.setText(course.title != null ? course.title : "Chưa có tên");
        holder.tvInstructor.setText(course.instructor);
        holder.tvRating.setText(course.rating);
        holder.tvStudents.setText(course.students);
        holder.tvLessons.setText(course.lessons);
        holder.tvDuration.setText(course.duration);
        holder.tvPrice.setText(course.priceText); // Đổi từ price sang priceText
        holder.tvCategory.setText(course.category);
        holder.tvLevel.setText(course.level);

        // Tạm thời gán ảnh local bằng imageRes.
        // Lời khuyên: Sau này nếu API trả về link ảnh thực (imageUrl), bạn nên tích hợp thư viện Glide hoặc Picasso ở đoạn này.
        holder.ivThumbnail.setImageResource(course.imageRes);
    }

    @Override
    public int getItemCount() {
        if (courseList != null) {
            return courseList.size();
        }
        return 0; // Tránh lỗi NullPointerException khi list chưa có dữ liệu
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvCourseName, tvInstructor, tvRating, tvStudents, tvLessons, tvDuration, tvPrice, tvCategory, tvLevel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Các ID này hoàn toàn khớp với file item_course.xml của bạn
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