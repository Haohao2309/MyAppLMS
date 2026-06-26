package com.example.myapplms.ui.notification;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.data.local.dao.NotificationDao;
import com.example.myapplms.data.repository.NotificationRepository;

/**
 * FILE: app/src/main/java/com/example/myapplms/ui/notification/NotificationViewModelFactory.java
 * HÀNH ĐỘNG: THAY THẾ TOÀN BỘ file hiện tại.
 *
 * THAY ĐỔI: Thêm NotificationDao vào constructor để truyền vào ViewModel.
 */
public class NotificationViewModelFactory implements ViewModelProvider.Factory {

    private final NotificationRepository repository;
    private final NotificationDao notificationDao;
    private final int studentId;

    public NotificationViewModelFactory(NotificationRepository repository,
                                        NotificationDao notificationDao,
                                        int studentId) {
        this.repository = repository;
        this.notificationDao = notificationDao;
        this.studentId = studentId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new NotificationsViewModel(repository, notificationDao, studentId);
    }
}