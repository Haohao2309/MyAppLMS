package com.example.myapplms.ui.teacher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.response.TaskItemResponse;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.VH> {

    private final List<TaskItemResponse> items;

    public TaskAdapter(List<TaskItemResponse> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        TaskItemResponse item = items.get(pos);
        h.tvTitle.setText(item.title != null ? item.title : "");
        String course = item.courseTitle != null ? item.courseTitle : "Không xác định";
        String time = item.dueDate != null ? item.dueDate : "Không có hạn";
        h.tvSubtitle.setText(course + " · " + time);

        if (item.isUrgent) {
            h.tvUrgent.setVisibility(View.VISIBLE);
        } else {
            h.tvUrgent.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvUrgent;

        VH(View v) {
            super(v);
            tvTitle    = v.findViewById(R.id.tvTaskTitle);
            tvSubtitle = v.findViewById(R.id.tvTaskSubtitle);
            tvUrgent   = v.findViewById(R.id.tvTaskUrgent);
        }
    }
}
