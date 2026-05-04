package com.example.myapplms.ui.notification.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplms.R;
import com.example.myapplms.model.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notifications;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Notification notification, int position);
    }

    public NotificationAdapter(List<Notification> notifications, OnItemClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification current = notifications.get(position);
        Context ctx = holder.itemView.getContext();

        // 1. Logic phân nhóm ngày (Hiển thị "Today", "Yesterday")
        if (position == 0 || !current.getDateGroup().equals(notifications.get(position - 1).getDateGroup())) {
            holder.tvDateGroup.setVisibility(View.VISIBLE);
            holder.tvDateGroup.setText(current.getDateGroup());
        } else {
            holder.tvDateGroup.setVisibility(View.GONE);
        }

        // 2. Set Text
        holder.tvTitle.setText(current.getTitle());
        holder.tvBody.setText(current.getBody());
        holder.tvTime.setText(current.getTimestamp());

        // 3. Mapping Icon và Màu sắc (Giống ICON_MAP bên React)
        int bgRes, iconColorRes, iconDrawable;
        switch (current.getType().toLowerCase()) {
            case "achievement":
                bgRes = R.color.achievement_bg;
                iconColorRes = R.color.achievement_icon;
                iconDrawable = R.drawable.ic_trophy; // Nhớ add vector asset
                break;
            case "grade":
                bgRes = R.color.grade_bg;
                iconColorRes = R.color.grade_icon;
                iconDrawable = R.drawable.ic_star;
                break;
            case "message":
                bgRes = R.color.message_bg;
                iconColorRes = R.color.message_icon;
                iconDrawable = R.drawable.ic_message;
                break;
            default: // Announcement / Default
                bgRes = R.color.indigo_100;
                iconColorRes = R.color.indigo_600;
                iconDrawable = R.drawable.ic_bell_outline;
                break;
        }

        holder.iconContainer.getBackground().setTint(ContextCompat.getColor(ctx, bgRes));
        holder.ivIcon.setImageResource(iconDrawable);
        holder.ivIcon.setColorFilter(ContextCompat.getColor(ctx, iconColorRes));

        // 4. Trạng thái Đã đọc / Chưa đọc
        if (!current.isRead()) {
            holder.viewUnreadDot.setVisibility(View.VISIBLE);
            holder.tvTitle.setTypeface(null, Typeface.BOLD);
            holder.tvTitle.setTextColor(ContextCompat.getColor(ctx, R.color.gray_900));
        } else {
            holder.viewUnreadDot.setVisibility(View.GONE);
            holder.tvTitle.setTypeface(null, Typeface.NORMAL);
            holder.tvTitle.setTextColor(ContextCompat.getColor(ctx, R.color.gray_700));
        }

        // 5. Bắt sự kiện Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(current, position);
        });
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    public void updateData(List<Notification> newNotifs) {
        this.notifications = newNotifs;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateGroup, tvTitle, tvBody, tvTime;
        FrameLayout iconContainer;
        ImageView ivIcon;
        View viewUnreadDot;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateGroup = itemView.findViewById(R.id.tvDateGroup);
            iconContainer = itemView.findViewById(R.id.iconContainer);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
