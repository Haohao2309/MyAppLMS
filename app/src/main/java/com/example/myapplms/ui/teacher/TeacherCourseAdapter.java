package com.example.myapplms.ui.teacher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.myapplms.R;
import com.example.myapplms.model.Course;

import java.util.List;

public class TeacherCourseAdapter extends RecyclerView.Adapter<TeacherCourseAdapter.ViewHolder> {

    public interface OnEditClickListener {
        void onEdit(Course course);
    }

    public interface OnDeleteClickListener {
        void onDelete(Course course);
    }

    public interface OnRestoreClickListener {
        void onRestore(Course course);
    }

    private final List<Course> courseList;
    private final OnEditClickListener onEditClick;
    private final OnDeleteClickListener onDeleteClick;
    private final OnRestoreClickListener onRestoreClick;

    public TeacherCourseAdapter(List<Course> courseList,
                                OnEditClickListener onEditClick,
                                OnDeleteClickListener onDeleteClick,
                                OnRestoreClickListener onRestoreClick) {
        this.courseList     = courseList;
        this.onEditClick    = onEditClick;
        this.onDeleteClick  = onDeleteClick;
        this.onRestoreClick = onRestoreClick;
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

        if (course.imageUrl != null && !course.imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(course.imageUrl)
                    .placeholder(R.drawable.ic_person)
                    .transform(new CenterCrop(), new RoundedCorners(24))
                    .into(holder.ivThumbnail);
        }

        // ── Hiển thị trạng thái theo isDeleted ──────────────
        if (course.isDeleted) {
            // Badge "Deleted" màu đỏ
            holder.tvStatus.setText("Deleted");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
            holder.tvStatus.setBackgroundColor(0xFFFFEBEE);

            // Hiển thị stripe đỏ + overlay ảnh
            holder.viewDeletedStripe.setVisibility(View.VISIBLE);
            holder.viewDeletedOverlay.setVisibility(View.VISIBLE);

            // Ẩn nút Delete, hiện nút Restore
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnRestore.setVisibility(View.VISIBLE);

            // Mờ toàn bộ card
            holder.itemView.setAlpha(0.75f);
        } else {
            // Badge "Published" màu xanh
            holder.tvStatus.setText("Published");
            holder.tvStatus.setTextColor(0xFF4CAF50);
            holder.tvStatus.setBackgroundColor(0xFFE8F5E9);

            // Ẩn stripe + overlay
            holder.viewDeletedStripe.setVisibility(View.GONE);
            holder.viewDeletedOverlay.setVisibility(View.GONE);

            // Hiện nút Delete, ẩn nút Restore
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnRestore.setVisibility(View.GONE);

            holder.itemView.setAlpha(1.0f);
        }

        // ── Gắn sự kiện click ───────────────────────────────
        holder.btnEdit.setOnClickListener(v -> {
            if (onEditClick != null) onEditClick.onEdit(course);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (onDeleteClick != null) onDeleteClick.onDelete(course);
        });

        holder.btnRestore.setOnClickListener(v -> {
            if (onRestoreClick != null) onRestoreClick.onRestore(course);
        });
    }

    @Override
    public int getItemCount() {
        return courseList != null ? courseList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvPrice, tvStudents, tvLessons, tvDuration, tvStatus;
        ImageButton btnEdit, btnDelete, btnRestore;
        ImageView ivThumbnail;
        View viewDeletedStripe, viewDeletedOverlay;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle            = itemView.findViewById(R.id.tvTitle);
            tvCategory         = itemView.findViewById(R.id.tvCategory);
            tvPrice            = itemView.findViewById(R.id.tvPrice);
            tvStudents         = itemView.findViewById(R.id.tvStudents);
            tvLessons          = itemView.findViewById(R.id.tvLessons);
            tvDuration         = itemView.findViewById(R.id.tvDuration);
            tvStatus           = itemView.findViewById(R.id.tvStatus);
            btnEdit            = itemView.findViewById(R.id.btnEdit);
            btnDelete          = itemView.findViewById(R.id.btnDelete);
            btnRestore         = itemView.findViewById(R.id.btnRestore);
            ivThumbnail        = itemView.findViewById(R.id.ivThumbnail);
            viewDeletedStripe  = itemView.findViewById(R.id.viewDeletedStripe);
            viewDeletedOverlay = itemView.findViewById(R.id.viewDeletedOverlay);
        }
    }
}