package com.example.myapplms;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.model.Course;

import java.util.ArrayList;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    private List<Course> courseList;
    private List<Course> courseListFull;
    private final OnCourseClickListener listener;
    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    // 3. CẬP NHẬT CONSTRUCTOR ĐỂ NHẬN LISTENER
    public CourseAdapter(List<Course> courseList, OnCourseClickListener listener) {
        this.courseList = courseList;
        this.courseListFull = new ArrayList<>(courseList); // Copy list
        this.listener = listener;
    }
    public void updateData(List<Course> newCourses) {
        this.courseList.clear();
        this.courseList.addAll(newCourses);
        this.courseListFull = new ArrayList<>(newCourses); // Sao lưu data gốc
        notifyDataSetChanged();
    }
    public void filter(String text) {
        courseList.clear();
        if (text == null || text.trim().isEmpty()) {
            // Nếu ô tìm kiếm trống, hiển thị lại toàn bộ data gốc
            courseList.addAll(courseListFull);
        } else {
            text = text.toLowerCase().trim();
            for (Course item : courseListFull) {
                // Kiểm tra xem title của khóa học có chứa từ khóa không
                if (item.title != null && item.title.toLowerCase().contains(text)) {
                    courseList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }
    public void advancedFilter(String selectedLevel, String selectedPrice, String selectedRating) {
        courseList.clear();

        for (Course item : courseListFull) {
            boolean matchLevel = true;
            boolean matchRating = true;
            boolean matchPrice = true;

            if (selectedLevel != null && !selectedLevel.equalsIgnoreCase("All Levels")) {

                if (item.level == null || !item.level.equalsIgnoreCase(selectedLevel)) {
                    matchLevel = false;
                }
            }

            if (selectedRating != null && !selectedRating.equalsIgnoreCase("Any")) {
                try {
                    // Lấy con số từ UI (Ví dụ: "★ 4.5+" -> "4.5", "3+" -> "3")
                    String ratingStr = selectedRating.replaceAll("[^0-9.]", "");
                    float targetRate = Float.parseFloat(ratingStr);

                    // Xử lý rating của khóa học từ API
                    float itemRate = 0f;
                    if (item.rating != null && !item.rating.isEmpty()) {
                        // Xóa mọi ký tự lạ từ API (VD: "4.8/5" -> "4.85" -> Cần cẩn thận nếu format API dị,
                        // nhưng thông thường "4.8" hoặc "★ 4.8" sẽ biến thành "4.8")
                        String cleanItemRating = item.rating.replaceAll("[^0-9.]", "");
                        if (!cleanItemRating.isEmpty()) {
                            itemRate = Float.parseFloat(cleanItemRating);
                        }
                    }

                    if (itemRate < targetRate) {
                        matchRating = false;
                    }
                } catch (Exception e) {
                    matchRating = false;
                }
            }

            if (selectedPrice != null && !selectedPrice.equalsIgnoreCase("All")) {
                if (item.priceText == null) {
                    matchPrice = false;
                } else {
                    String priceStr = item.priceText.toLowerCase().trim();
                    float itemPrice = 0f;

                    if (!priceStr.contains("free") && !priceStr.contains("miễn phí")) {
                        try {
                            itemPrice = Float.parseFloat(item.priceText.replaceAll("[^0-9.]", ""));
                        } catch (Exception e) {
                            matchPrice = false;
                        }
                    }

                    if (selectedPrice.equalsIgnoreCase("Free")) {
                        if (itemPrice > 0) matchPrice = false;
                    } else if (selectedPrice.equalsIgnoreCase("Under $50")) {
                        if (itemPrice >= 50) matchPrice = false;
                    } else if (selectedPrice.equalsIgnoreCase("$50-$100")) {
                        if (itemPrice < 50 || itemPrice > 100) matchPrice = false;
                    } else if (selectedPrice.equalsIgnoreCase("Over $100")) {
                        if (itemPrice <= 100) matchPrice = false;
                    }
                }
            }

            if (matchLevel && matchRating && matchPrice) {
                courseList.add(item);
            }
        }

        notifyDataSetChanged(); // Cập nhật lại RecyclerView
    }
    // Thêm hàm này vào CourseAdapter
    public void filterByCategory(String categoryName) {
        courseList.clear();

        if (categoryName.equalsIgnoreCase("All")) {
            courseList.addAll(courseListFull);
        } else {
            // Lọc theo tên category
            for (Course item : courseListFull) {
                if (item.category != null && item.category.equalsIgnoreCase(categoryName)) {
                    courseList.add(item);
                }
            }
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

        holder.ivThumbnail.setImageResource(course.imageRes);



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
