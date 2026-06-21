package com.example.myapplms.ui.notification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.myapplms.data.local.dao.NotificationDao;
import com.example.myapplms.data.local.entity.NotificationEntity;
import com.example.myapplms.data.mapper.NotificationMapper;
import com.example.myapplms.data.repository.NotificationRepository;
import com.example.myapplms.model.Notification;

import java.util.List;
import java.util.stream.Collectors;

/**
 * FILE: app/src/main/java/com/example/myapplms/ui/notification/NotificationsViewModel.java
 * HÀNH ĐỘNG: THAY THẾ TOÀN BỘ file hiện tại.
 *
 * LUỒNG DỮ LIỆU MỚI:
 *
 *   Room DB ──(LiveData)──► ViewModel filter ──► displayedNotifications ──► Fragment/UI
 *
 * - Nguồn sự thật duy nhất (Single Source of Truth) = Room DB.
 * - allNotificationsLive: LiveData<List<NotificationEntity>> trực tiếp từ DAO.
 *   Bất cứ khi nào Room DB thay đổi (markAsRead, insert...) → LiveData tự emit → UI tự cập nhật.
 * - MediatorLiveData gom allNotificationsLive + currentFilterLive lại,
 *   áp dụng filter rồi map sang UI Model.
 *
 * KHI BẤM VÀO THÔNG BÁO:
 *   Fragment.onClick → ViewModel.markAsRead(id)
 *     → Repository.markAsRead(id)
 *       → Room: UPDATE is_read=1 (background thread)  ← DB thay đổi
 *       → API: markAsRead (ngầm, best-effort)
 *     → Room LiveData tự emit list mới
 *     → MediatorLiveData re-filter
 *     → displayedNotifications emit
 *     → Adapter.updateData() → UI refresh
 *   (Không cần set gì thủ công trong ViewModel nữa)
 */
public class NotificationsViewModel extends ViewModel {

    private final NotificationRepository repository;
    private final NotificationDao notificationDao;
    private final int studentId;

    // ── LiveData nguồn từ Room (tự động update khi DB thay đổi) ──────────────────
    private final LiveData<List<NotificationEntity>> allNotificationsLive;

    // ── Filter đang chọn ──────────────────────────────────────────────────────────
    private final MutableLiveData<String> currentFilterLive = new MutableLiveData<>("all");

    // ── LiveData đầu ra cho Fragment observe ─────────────────────────────────────
    // MediatorLiveData: lắng nghe cả Room data lẫn filter, tự re-compute khi một trong hai thay đổi
    private final MediatorLiveData<List<Notification>> displayedNotifications = new MediatorLiveData<>();
    private final MediatorLiveData<Integer> unreadCount = new MediatorLiveData<>();

    // ── Constructor ───────────────────────────────────────────────────────────────
    public NotificationsViewModel(NotificationRepository repository, NotificationDao notificationDao, int studentId) {
        this.repository = repository;
        this.notificationDao = notificationDao;
        this.studentId = studentId;

        // 1. Lấy LiveData trực tiếp từ Room DAO — tự động observe DB
        allNotificationsLive = notificationDao.getAllNotifications(studentId);

        // 2. Mỗi khi Room data thay đổi → re-apply filter → emit displayedNotifications
        displayedNotifications.addSource(allNotificationsLive, entities -> recompute());
        displayedNotifications.addSource(currentFilterLive, filter -> recompute());

        // 3. Đếm unread từ Room trực tiếp
        unreadCount.addSource(notificationDao.getUnreadCount(studentId), count ->
                unreadCount.setValue(count != null ? count : 0));

        // 4. Fetch data mới từ API về (sẽ insert vào Room → Room LiveData tự trigger)
        fetchFromApi();
    }

    // ── Public API cho Fragment ───────────────────────────────────────────────────

    public LiveData<List<Notification>> getDisplayedNotifications() {
        return displayedNotifications;
    }

    public LiveData<Integer> getUnreadCount() {
        return unreadCount;
    }

    /**
     * Đổi filter chip (all / achievement / grade / message / announcement / payment).
     * MediatorLiveData tự trigger recompute().
     */
    public void setFilter(String filter) {
        currentFilterLive.setValue(filter);
    }

    /**
     * Đánh dấu một thông báo đã đọc khi bấm vào.
     * Repository sẽ:
     *   1. UPDATE Room ngay (background thread) → Room LiveData emit → UI tự refresh
     *   2. Gọi API ngầm để sync lên server
     */
    public void markAsRead(String id) {
        repository.markAsRead(id, studentId);
        // KHÔNG cần sửa allNotifications thủ công nữa.
        // Room tự emit LiveData mới → recompute() chạy → UI cập nhật.
    }

    /**
     * Đánh dấu TẤT CẢ đã đọc.
     */
    public void markAllAsRead() {
        repository.markAllAsRead(studentId);
        // Tương tự, Room tự trigger update UI.
    }

    /**
     * Fetch data mới từ API (gọi lúc khởi tạo hoặc khi pull-to-refresh).
     * Data từ API sẽ được Repository insert vào Room → Room LiveData tự emit.
     */
    public void fetchFromApi() {
        repository.fetchNotifications(studentId, new NotificationRepository.DataCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> data) {
                // Không cần làm gì ở đây.
                // Repository đã insertOrReplaceAll vào Room rồi.
                // Room LiveData sẽ tự emit và recompute() sẽ chạy.
            }

            @Override
            public void onError(String error) {
                // TODO: Emit error LiveData để hiển thị snackbar/toast
                // Nếu lỗi mạng: Room vẫn giữ cache cũ → UI không trống
            }
        });
    }

    // ── Private: Re-apply filter mỗi khi data hoặc filter thay đổi ───────────────

    private void recompute() {
        List<NotificationEntity> entities = allNotificationsLive.getValue();
        String filter = currentFilterLive.getValue();

        if (entities == null) {
            displayedNotifications.setValue(null);
            return;
        }

        // Áp dụng filter theo type
        List<NotificationEntity> filtered;
        if (filter == null || filter.equalsIgnoreCase("all")) {
            filtered = entities;
        } else {
            final String f = filter;
            filtered = entities.stream()
                    .filter(e -> e.getType() != null && e.getType().equalsIgnoreCase(f))
                    .collect(Collectors.toList());
        }

        // Map Entity → UI Model rồi đẩy ra Fragment
        displayedNotifications.setValue(NotificationMapper.toUiModelList(filtered));
    }
}