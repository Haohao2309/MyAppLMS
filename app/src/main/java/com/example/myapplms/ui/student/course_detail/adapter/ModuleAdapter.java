package com.example.myapplms.ui.student.course_detail.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplms.R;
import com.example.myapplms.model.course_content.CourseModule;
import java.util.List;

public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder> {

    private final List<CourseModule> moduleList;
    // Mảng lưu trạng thái đóng/mở của từng module
    private final boolean[] isExpandedArray;

    public ModuleAdapter(List<CourseModule> moduleList) {
        this.moduleList = moduleList;
        this.isExpandedArray = new boolean[moduleList.size()];
        // Mặc định cho Module đầu tiên mở ra (giống ảnh)
        if(isExpandedArray.length > 0) isExpandedArray[0] = true;
    }

    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_module, parent, false);
        return new ModuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleViewHolder holder, int position) {
        CourseModule module = moduleList.get(position);
        boolean isExpanded = isExpandedArray[position];

        holder.tvModuleTitle.setText(module.title);
        holder.tvLessonCount.setText(module.lessons.size() + " lessons");

        // Đổi icon mũi tên lên/xuống
        holder.ivArrow.setRotation(isExpanded ? 180f : 0f);

        // Hiển thị hoặc ẩn RecyclerView con (Bài học)
        if (isExpanded) {
            holder.rvLessons.setVisibility(View.VISIBLE);
            LessonAdapter lessonAdapter = new LessonAdapter(module.lessons);
            holder.rvLessons.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
            holder.rvLessons.setAdapter(lessonAdapter);
        } else {
            holder.rvLessons.setVisibility(View.GONE);
        }

        // Bắt sự kiện click vào Header của Module để thu/phóng
        holder.layoutHeader.setOnClickListener(v -> {
            isExpandedArray[position] = !isExpandedArray[position];
            notifyItemChanged(position); // Render lại đúng item này mượt mà
        });
    }

    @Override
    public int getItemCount() {
        return moduleList != null ? moduleList.size() : 0;
    }

    static class ModuleViewHolder extends RecyclerView.ViewHolder {
        View layoutHeader;
        TextView tvModuleTitle, tvLessonCount;
        ImageView ivArrow;
        RecyclerView rvLessons;

        public ModuleViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutHeader = itemView.findViewById(R.id.layout_module_header);
            tvModuleTitle = itemView.findViewById(R.id.tv_module_title);
            tvLessonCount = itemView.findViewById(R.id.tv_lesson_count);
            ivArrow = itemView.findViewById(R.id.iv_arrow);
            rvLessons = itemView.findViewById(R.id.rv_lessons);
        }
    }
}