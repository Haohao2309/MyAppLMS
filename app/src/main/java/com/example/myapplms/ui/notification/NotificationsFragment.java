package com.example.myapplms.ui.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplms.databinding.FragmentNotificationsBinding;
import com.example.myapplms.ui.base.BaseFragment;
import com.example.myapplms.ui.notification.adapter.NotificationAdapter;
import com.google.android.material.chip.Chip;

public class NotificationsFragment extends BaseFragment<FragmentNotificationsBinding> {

    private NotificationsViewModel viewModel;
    private NotificationAdapter adapter;

    // SỬA LỖI 1: Trả về đối tượng Binding, không trả về null.
    @NonNull
    @Override
    protected FragmentNotificationsBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentNotificationsBinding.inflate(inflater, container, false);
    }

    // SỬA LỖI 2: XÓA hàm onCreateView. BaseFragment đã làm việc này rồi.

    // Sử dụng các hàm vòng đời đã được BaseFragment cung cấp:
    @Override
    protected void setupViews() {
        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        setupRecyclerView();
        setupFilters();
    }

    @Override
    protected void setupListeners() {
        // LƯU Ý: Gọi getBinding() để truy cập các view trên giao diện
        getBinding().btnMarkAllRead.setOnClickListener(v -> viewModel.markAllAsRead());
        getBinding().btnSettings.setOnClickListener(v -> {
            showToast("Settings Clicked"); // showToast là hàm có sẵn từ BaseFragment
        });
    }

    @Override
    protected void observeViewModel() {
        viewModel.getDisplayedNotifications().observe(getViewLifecycleOwner(), notifications -> {
            adapter.updateData(notifications); // Cập nhật danh sách mới vào Adapter

            if (notifications.isEmpty()) {
                getBinding().rvNotifications.setVisibility(View.GONE);
                getBinding().layoutEmpty.setVisibility(View.VISIBLE);
            } else {
                getBinding().rvNotifications.setVisibility(View.VISIBLE);
                getBinding().layoutEmpty.setVisibility(View.GONE);
            }
        });

        viewModel.getUnreadCount().observe(getViewLifecycleOwner(), count -> {
            if (count > 0) {
                getBinding().tvUnreadBadge.setVisibility(View.VISIBLE);
                getBinding().tvUnreadBadge.setText(count + " new");
                getBinding().btnMarkAllRead.setVisibility(View.VISIBLE);
            } else {
                getBinding().tvUnreadBadge.setVisibility(View.GONE);
                getBinding().btnMarkAllRead.setVisibility(View.GONE);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(new java.util.ArrayList<>(), (notification, position) -> {
            viewModel.markAsRead(notification.getId());
        });
        getBinding().rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().rvNotifications.setAdapter(adapter);
    }

    private void setupFilters() {
        String[] filters = {"All", "Achievement", "Grade", "Message", "Announcement", "Payment"};
        for (String filter : filters) {
            Chip chip = new Chip(requireContext());
            chip.setText(filter);
            chip.setCheckable(true);
            if (filter.equalsIgnoreCase("All")) chip.setChecked(true);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) viewModel.setFilter(filter.toLowerCase());
            });
            getBinding().chipGroupFilter.addView(chip);
        }
    }
}