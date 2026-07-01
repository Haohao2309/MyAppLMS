package com.example.myapplms.ui.notification;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplms.LMSApplication;
import com.example.myapplms.R;
import com.example.myapplms.data.local.LmsDatabase;
import com.example.myapplms.data.local.dao.NotificationDao;
import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.repository.NotificationRepository;
import com.example.myapplms.databinding.FragmentNotificationsBinding;
import com.example.myapplms.ui.base.BaseFragment;
import com.example.myapplms.ui.notification.adapter.NotificationAdapter;
import com.example.myapplms.utils.SessionManager;
import com.google.android.material.chip.Chip;

/**
 * FILE: app/src/main/java/com/example/myapplms/ui/notification/NotificationsFragment.java
 * HÀNH ĐỘNG: THAY THẾ TOÀN BỘ file hiện tại.
 *
 * THAY ĐỔI so với bản trước:
 * - setupViews(): lấy thêm NotificationDao từ LmsDatabase, truyền vào Factory.
 * - Chip style giữ nguyên.
 */
public class NotificationsFragment extends BaseFragment<FragmentNotificationsBinding> {

    private NotificationsViewModel viewModel;
    private NotificationAdapter adapter;

    @NonNull
    @Override
    protected FragmentNotificationsBinding inflateBinding(@NonNull LayoutInflater inflater,
                                                          @Nullable ViewGroup container) {
        return FragmentNotificationsBinding.inflate(inflater, container, false);
    }

    @Override
    protected void setupViews() {
        LMSApplication app = (LMSApplication) requireActivity().getApplication();

        // Lấy ApiService từ Retrofit
        LmsApiService apiService = app.getRetrofitClient().create(LmsApiService.class);

        // Lấy DAO từ Room Database
        NotificationDao notificationDao = LmsDatabase
                .getInstance(requireContext().getApplicationContext())
                .notificationDao();

        // Khởi tạo Repository với cả hai dependency
        NotificationRepository repo = new NotificationRepository(apiService, notificationDao);

        // Lấy studentId từ SessionManager
        SessionManager sessionManager = new SessionManager(requireContext());
        int studentId = sessionManager.getStudentId() != null ? sessionManager.getStudentId() : -1;

        // Factory giờ nhận thêm DAO và studentId để truyền vào ViewModel
        NotificationViewModelFactory factory = new NotificationViewModelFactory(repo, notificationDao, studentId);
        viewModel = new ViewModelProvider(this, factory).get(NotificationsViewModel.class);

        setupRecyclerView();
        setupFilters();
    }

    @Override
    protected void setupListeners() {
        getBinding().btnMarkAllRead.setOnClickListener(v -> viewModel.markAllAsRead());
    }

    @Override
    protected void observeViewModel() {
        viewModel.getDisplayedNotifications().observe(getViewLifecycleOwner(), notifications -> {
            if (notifications == null) return;
            adapter.updateData(notifications);

            if (notifications.isEmpty()) {
                getBinding().rvNotifications.setVisibility(View.GONE);
                getBinding().layoutEmpty.setVisibility(View.VISIBLE);
            } else {
                getBinding().rvNotifications.setVisibility(View.VISIBLE);
                getBinding().layoutEmpty.setVisibility(View.GONE);
            }
        });

        viewModel.getUnreadCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null && count > 0) {
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
        adapter = new NotificationAdapter(new java.util.ArrayList<>(),
                (notification, position) -> {
                    // Bấm vào → markAsRead → Room cập nhật → LiveData emit → UI tự refresh
                    viewModel.markAsRead(notification.getId());
                    
                    if (notification.getLink() != null && notification.getLink().startsWith("course://")) {
                        try {
                            int courseId = Integer.parseInt(notification.getLink().replace("course://", ""));
                            android.content.Intent intent = new android.content.Intent(requireContext(), com.example.myapplms.ui.student.course_detail.CourseDetailActivity.class);
                            intent.putExtra("COURSE_ID", courseId);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (notification.getLink() != null && notification.getLink().startsWith("community/")) {
                        try {
                            String postId = notification.getLink().replace("community/", "");
                            android.content.Intent intent = new android.content.Intent(requireContext(), com.example.myapplms.ui.community.PostDetailActivity.class);
                            intent.putExtra("POST_ID", postId);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        getBinding().rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().rvNotifications.setAdapter(adapter);
    }

    private void setupFilters() {
        getBinding().chipGroupFilter.removeAllViews();
        String[] filters = {"All", "Achievement", "Grade", "Message", "Announcement", "Payment"};

        int[][] bgStates = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{}
        };
        int[] bgColors = new int[]{
                ContextCompat.getColor(requireContext(), R.color.chip_bg_selected),
                ContextCompat.getColor(requireContext(), R.color.chip_bg_default)
        };
        ColorStateList chipBgColorList = new ColorStateList(bgStates, bgColors);

        int[][] textStates = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{}
        };
        int[] textColors = new int[]{
                ContextCompat.getColor(requireContext(), R.color.chip_text_selected),
                ContextCompat.getColor(requireContext(), R.color.chip_text_default)
        };
        ColorStateList chipTextColorList = new ColorStateList(textStates, textColors);

        for (String filter : filters) {
            Chip chip = new Chip(requireContext());
            chip.setText(filter);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setChipStrokeWidth(0f);
            chip.setChipBackgroundColor(chipBgColorList);
            chip.setTextColor(chipTextColorList);
            chip.setRippleColorResource(R.color.indigo_200);
            chip.setChipStartPadding(12f);
            chip.setChipEndPadding(12f);

            if (filter.equalsIgnoreCase("All")) chip.setChecked(true);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) viewModel.setFilter(filter.toLowerCase());
            });

            getBinding().chipGroupFilter.addView(chip);
        }
    }
}