package com.example.myapplms.ui.student.home.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.response.DashboardResponse.AchievementDTO;
import com.example.myapplms.databinding.ItemHomeStatCardBinding;

import java.util.ArrayList;
import java.util.List;

public class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.ViewHolder> {

    private final List<AchievementDTO> items = new ArrayList<>();

    public void updateData(List<AchievementDTO> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeStatCardBinding binding = ItemHomeStatCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemHomeStatCardBinding binding;

        public ViewHolder(@NonNull ItemHomeStatCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AchievementDTO dto) {
            binding.tvStatValue.setText(dto.value);
            binding.tvStatLabel.setText(dto.title);

            Context context = itemView.getContext();
            String type = dto.type != null ? dto.type.toUpperCase() : "";

            int iconRes = R.drawable.ic_book;
            int iconBgRes = R.drawable.bg_quick_action_blue;
            int tintColor = ContextCompat.getColor(context, R.color.primary);

            switch (type) {
                case "STREAK":
                    iconRes = R.drawable.ic_fire;
                    iconBgRes = R.drawable.bg_stat_card_amber;
                    tintColor = ContextCompat.getColor(context, R.color.home_streak_orange);
                    break;
                case "POINTS":
                case "XP":
                    iconRes = R.drawable.ic_star;
                    iconBgRes = R.drawable.bg_quick_action_purple;
                    tintColor = ContextCompat.getColor(context, R.color.purple_primary);
                    break;
                case "RANK":
                    iconRes = R.drawable.ic_trophy;
                    iconBgRes = R.drawable.bg_quick_action_orange;
                    tintColor = ContextCompat.getColor(context, R.color.warning);
                    break;
                case "COURSES":
                default:
                    iconRes = R.drawable.ic_book;
                    iconBgRes = R.drawable.bg_quick_action_blue;
                    tintColor = ContextCompat.getColor(context, R.color.primary);
                    break;
            }

            binding.ivStatIcon.setImageResource(iconRes);
            binding.flStatIconBg.setBackgroundResource(iconBgRes);
            binding.ivStatIcon.setImageTintList(ColorStateList.valueOf(tintColor));
        }
    }
}
