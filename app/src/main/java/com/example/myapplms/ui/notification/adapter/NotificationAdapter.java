package com.example.myapplms.ui.notification.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplms.R;
import com.example.myapplms.model.Notification;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * FILE: app/src/main/java/com/example/myapplms/ui/notification/adapter/NotificationAdapter.java
 * HÀNH ĐỘNG: THAY THẾ TOÀN BỘ nội dung file hiện tại.
 *
 * THAY ĐỔI SO VỚI BẢN CŨ:
 * 1. ViewHolder thêm tham chiếu cardNotification (ConstraintLayout) để set background động.
 * 2. onBindViewHolder — set background DRAWABLE tương ứng dựa vào isRead().
 * 3. Card chưa đọc: background tím nhạt + elevation nhẹ.
 * 4. Card đã đọc: background trắng + elevation = 0.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notifications;
    private final OnItemClickListener listener;

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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification current = notifications.get(position);
        Context ctx = holder.itemView.getContext();

        // 1. Logic phân nhóm ngày (hiển thị header "Today", "Yesterday", "Earlier")
        if (position == 0 || !current.getDateGroup().equals(notifications.get(position - 1).getDateGroup())) {
            holder.tvDateGroup.setVisibility(View.VISIBLE);
            holder.tvDateGroup.setText(current.getDateGroup().toUpperCase());
        } else {
            holder.tvDateGroup.setVisibility(View.GONE);
        }

        // 2. Set text
        holder.tvTitle.setText(current.getTitle());
        holder.tvBody.setText(current.getBody());
        holder.tvTime.setText(getRelativeTime(current.getTimestamp()));

        // 3. Mapping icon & màu sắc theo type
        int bgRes, iconColorRes, iconDrawable;
        switch (current.getType().toLowerCase()) {
            case "achievement":
                bgRes = R.color.achievement_bg;
                iconColorRes = R.color.achievement_icon;
                iconDrawable = R.drawable.ic_trophy;
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
            default: // announcement / payment / default
                bgRes = R.color.indigo_100;
                iconColorRes = R.color.indigo_600;
                iconDrawable = R.drawable.ic_bell_outline;
                break;
        }
        holder.iconContainer.getBackground().setTint(ContextCompat.getColor(ctx, bgRes));
        holder.ivIcon.setImageResource(iconDrawable);
        holder.ivIcon.setColorFilter(ContextCompat.getColor(ctx, iconColorRes));

        // 4. ĐÂY LÀ PHẦN MỚI: Set background card động dựa vào trạng thái isRead()
        if (!current.isRead()) {
            // CHƯA ĐỌC: nền tím nhạt + viền tím + title in đậm + dot hiện
            holder.cardNotification.setBackgroundResource(R.drawable.bg_notification_card_unread);
            holder.cardNotification.setElevation(dpToPx(ctx, 3)); // bóng đổ nhẹ
            holder.viewUnreadDot.setVisibility(View.VISIBLE);
            holder.tvTitle.setTypeface(null, Typeface.BOLD);
            holder.tvTitle.setTextColor(ContextCompat.getColor(ctx, R.color.gray_900));
            holder.tvBody.setTextColor(ContextCompat.getColor(ctx, R.color.gray_700));
        } else {
            // ĐÃ ĐỌC: nền trắng + viền xám + title thường + dot ẩn
            holder.cardNotification.setBackgroundResource(R.drawable.bg_notification_card);
            holder.cardNotification.setElevation(0f);
            holder.viewUnreadDot.setVisibility(View.GONE);
            holder.tvTitle.setTypeface(null, Typeface.NORMAL);
            holder.tvTitle.setTextColor(ContextCompat.getColor(ctx, R.color.gray_600));
            holder.tvBody.setTextColor(ContextCompat.getColor(ctx, R.color.gray_400));
        }

        // 5. Click listener
        holder.cardNotification.setOnClickListener(v -> {
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

    // ── ViewHolder ────────────────────────────────────────────────────────────────
    static class ViewHolder extends RecyclerView.ViewHolder {
        // THÊM MỚI: tham chiếu đến card container để set background động
        ConstraintLayout cardNotification;
        TextView tvDateGroup, tvTitle, tvBody, tvTime;
        FrameLayout iconContainer;
        ImageView ivIcon;
        View viewUnreadDot;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNotification = itemView.findViewById(R.id.cardNotification); // MỚI
            tvDateGroup = itemView.findViewById(R.id.tvDateGroup);
            iconContainer = itemView.findViewById(R.id.iconContainer);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    // ── Helper Methods ────────────────────────────────────────────────────────────

    private float dpToPx(Context ctx, float dp) {
        return dp * ctx.getResources().getDisplayMetrics().density;
    }

    private String getRelativeTime(String createdAtString) {
        if (createdAtString == null || createdAtString.isEmpty()) return "";
        try {
            String cleanTime = createdAtString;
            if (cleanTime.contains(".")) {
                cleanTime = cleanTime.substring(0, cleanTime.indexOf("."));
            } else if (cleanTime.contains("+")) {
                cleanTime = cleanTime.substring(0, cleanTime.indexOf("+"));
            } else if (cleanTime.endsWith("Z")) {
                cleanTime = cleanTime.substring(0, cleanTime.length() - 1);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(cleanTime);

            if (date != null) {
                return DateUtils.getRelativeTimeSpanString(
                        date.getTime(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_RELATIVE
                ).toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return createdAtString;
    }
}