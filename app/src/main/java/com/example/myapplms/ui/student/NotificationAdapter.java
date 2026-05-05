package com.example.myapplms.ui.student;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplms.R;
import com.example.myapplms.model.Notification;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications;

    public NotificationAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.tvTitle.setText(notification.getTitle());
        holder.tvDesc.setText(notification.getDescription());
        holder.tvTime.setText(notification.getTimeAgo());
        
        holder.unreadDot.setVisibility(notification.isUnread() ? View.VISIBLE : View.GONE);

        // Set Icon based on type
        switch (notification.getType()) {
            case ACHIEVEMENT:
                holder.ivIcon.setImageResource(R.drawable.ic_trophy);
                break;
            case QUIZ:
                holder.ivIcon.setImageResource(R.drawable.ic_star);
                break;
            case MESSAGE:
                holder.ivIcon.setImageResource(R.drawable.ic_comment);
                break;
            case UPDATE:
                holder.ivIcon.setImageResource(R.drawable.ic_notifications);
                break;
            case ENROLLMENT:
                holder.ivIcon.setImageResource(R.drawable.ic_explore); // Placeholder
                break;
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvDesc, tvTime;
        View unreadDot;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivNotificationIcon);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvDesc = itemView.findViewById(R.id.tvNotificationDesc);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
            unreadDot = itemView.findViewById(R.id.unreadDot);
        }
    }
}
