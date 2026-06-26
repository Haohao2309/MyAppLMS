package com.example.myapplms.ui.teacher;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.response.RecentActivityResponse;

import java.util.List;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.VH> {

    private final List<RecentActivityResponse> items;

    // Màu avatar theo thứ tự
    private static final int[] AVATAR_COLORS = {
            0xFF6C63FF, // tím
            0xFFFF6584, // hồng
            0xFF43CFAD, // xanh
            0xFFFF9F43, // cam
            0xFF5F27CD, // tím đậm
    };

    public RecentActivityAdapter(List<RecentActivityResponse> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_activity, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        RecentActivityResponse item = items.get(pos);

        // Avatar initials với màu xoay vòng
        h.tvInitials.setText(item.initials != null ? item.initials : "??");
        h.tvInitials.setBackgroundResource(R.drawable.bg_avatar_circle);
        h.tvInitials.getBackground().mutate().setColorFilter(
                new android.graphics.PorterDuffColorFilter(
                        AVATAR_COLORS[pos % AVATAR_COLORS.length],
                        android.graphics.PorterDuff.Mode.SRC_IN));

        h.tvStudentName.setText(item.studentName != null ? item.studentName : "");
        h.tvAction.setText(item.action != null ? item.action : "");
        h.tvTimeAgo.setText(item.timeAgo != null ? item.timeAgo : "");

        // Badge course name (ví dụ INT101)
        if (item.courseName != null && !item.courseName.isEmpty()) {
            h.tvCourse.setVisibility(View.VISIBLE);
            h.tvCourse.setText(item.courseName);
        } else {
            h.tvCourse.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvInitials, tvStudentName, tvAction, tvTimeAgo, tvCourse;

        VH(View v) {
            super(v);
            tvInitials    = v.findViewById(R.id.tvActivityInitials);
            tvStudentName = v.findViewById(R.id.tvActivityStudentName);
            tvAction      = v.findViewById(R.id.tvActivityAction);
            tvTimeAgo     = v.findViewById(R.id.tvActivityTimeAgo);
            tvCourse      = v.findViewById(R.id.tvActivityCourse);
        }
    }
}
