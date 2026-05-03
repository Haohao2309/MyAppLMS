package com.example.myapplms.ui.notification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplms.model.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class NotificationsViewModel extends ViewModel {

    // Nguồn dữ liệu gốc (Single Source of Truth)
    private List<Notification> allNotifications = new ArrayList<>();

    // LiveData để View (Fragment) lắng nghe
    private final MutableLiveData<List<Notification>> displayedNotifications = new MutableLiveData<>();
    private final MutableLiveData<Integer> unreadCount = new MutableLiveData<>(0);
    private String currentFilter = "all";

    public NotificationsViewModel() {
        loadMockData(); // Trong thực tế, bạn gọi Repository ở đây
    }

    public LiveData<List<Notification>> getDisplayedNotifications() { return displayedNotifications; }
    public LiveData<Integer> getUnreadCount() { return unreadCount; }

    public void setFilter(String filter) {
        this.currentFilter = filter;
        applyFilter();
    }

    public void markAllAsRead() {
        for (Notification n : allNotifications) {
            n.setRead(true);
        }
        applyFilter();
    }

    public void markAsRead(String id) {
        for (Notification n : allNotifications) {
            if (n.getId().equals(id)) {
                n.setRead(true);
                break;
            }
        }
        applyFilter();
    }

    private void applyFilter() {
        List<Notification> filteredList;
        if (currentFilter.equalsIgnoreCase("all")) {
            filteredList = new ArrayList<>(allNotifications);
        } else {
            filteredList = allNotifications.stream()
                    .filter(n -> n.getType().equalsIgnoreCase(currentFilter))
                    .collect(Collectors.toList());
        }

        // Đếm số lượng chưa đọc
        int count = (int) allNotifications.stream().filter(n -> !n.isRead()).count();

        unreadCount.setValue(count);
        displayedNotifications.setValue(filteredList);
    }

    private void loadMockData() {
        allNotifications.add(new Notification("1", "Achievement Unlocked!", "You earned the 'On a Roll' badge.", "achievement", "Just now", "Today", false));
        allNotifications.add(new Notification("2", "Quiz Results Available", "Your HTML quiz has been graded.", "grade", "15 min ago", "Today", false));
        allNotifications.add(new Notification("3", "System Maintenance", "Server downtime at midnight.", "announcement", "Yesterday", "Yesterday", true));
        applyFilter();
    }
}