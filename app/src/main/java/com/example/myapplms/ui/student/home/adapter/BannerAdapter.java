package com.example.myapplms.ui.student.home.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.response.BannerResponse;
import com.example.myapplms.databinding.ItemHomeBannerBinding;

import java.util.ArrayList;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    public interface OnBannerClickListener {
        void onBannerClick(BannerResponse banner);
    }

    private List<BannerResponse> banners = new ArrayList<>();
    private final OnBannerClickListener listener;

    public BannerAdapter(OnBannerClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<BannerResponse> newBanners) {
        this.banners = newBanners != null ? newBanners : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeBannerBinding binding = ItemHomeBannerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BannerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        holder.bind(banners.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return banners.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        private final ItemHomeBannerBinding binding;

        // Fallback color khi background_color null
        private static final int DEFAULT_COLOR = Color.parseColor("#5C6BC0");

        BannerViewHolder(ItemHomeBannerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(BannerResponse banner, OnBannerClickListener listener) {
            // ── Text ───────────────────────────────────────────
            binding.tvBannerTitle.setText(banner.title);
            binding.tvBannerAction.setText(
                    banner.actionText != null ? banner.actionText : "Xem ngay");

            // subtitle nullable
            if (banner.subtitle != null && !banner.subtitle.isEmpty()) {
                binding.tvBannerSubtitle.setVisibility(View.VISIBLE);
                binding.tvBannerSubtitle.setText(banner.subtitle);
            } else {
                binding.tvBannerSubtitle.setVisibility(View.GONE);
            }

            // ── Background color ───────────────────────────────
            int bgColor = DEFAULT_COLOR;
            if (banner.backgroundColor != null && !banner.backgroundColor.isEmpty()) {
                try {
                    bgColor = Color.parseColor(banner.backgroundColor);
                } catch (IllegalArgumentException ignored) { }
            }
            binding.clBannerBg.setBackgroundColor(bgColor);

            // ── Banner image (nullable) ────────────────────────
            if (banner.imageUrl != null && !banner.imageUrl.isEmpty()) {
                binding.ivBannerImage.setVisibility(View.VISIBLE);
                Glide.with(binding.ivBannerImage.getContext())
                        .load(banner.imageUrl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .centerCrop()
                        .error(R.drawable.ic_launcher_background)
                        .into(binding.ivBannerImage);
            } else {
                // Khi không có ảnh: dùng placeholder mờ mặc định
                binding.ivBannerImage.setVisibility(View.VISIBLE);
                Glide.with(binding.ivBannerImage.getContext())
                        .load(R.drawable.ic_launcher_background)
                        .centerCrop()
                        .into(binding.ivBannerImage);
            }

            // ── Click listener ─────────────────────────────────
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onBannerClick(banner);
            });
            binding.tvBannerAction.setOnClickListener(v -> {
                if (listener != null) listener.onBannerClick(banner);
            });
        }
    }
}
