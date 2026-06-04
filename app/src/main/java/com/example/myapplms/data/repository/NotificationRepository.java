package com.example.myapplms.data.repository;

import android.os.Handler;
import android.os.Looper;

import com.example.myapplms.data.local.dao.NotificationDao;
import com.example.myapplms.data.local.entity.NotificationEntity;
import com.example.myapplms.data.mapper.NotificationMapper;
import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.response.NotificationResponse;
import com.example.myapplms.model.Notification;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * FILE: app/src/main/java/com/example/myapplms/data/repository/NotificationRepository.java
 * HÀNH ĐỘNG: THAY THẾ TOÀN BỘ nội dung file hiện tại.
 *
 * CHIẾN LƯỢC: Cache-first (Offline-first)
 *
 * Luồng dữ liệu:
 * 1. fetchNotifications() → Gọi API.
 * 2. Nhận data từ server → Map sang Entity → UPSERT vào Room (insertOrReplaceAll).
 * 3. Callback.onSuccess() trả về List<Notification> (UI Model) cho ViewModel.
 * 4. ViewModel cập nhật LiveData → Fragment tự refresh.
 *
 * markAsRead():
 * - Optimistic update: ghi vào Room NGAY LẬP TỨC (isSynced = false).
 * - Gọi API ngầm để sync lên server.
 * - Nếu API thành công → cập nhật isSynced = true trong Room.
 * - Nếu API thất bại → giữ nguyên isSynced = false, WorkManager sẽ retry sau.
 *
 * QUAN TRỌNG: Room KHÔNG CHO phép thao tác DB trên Main Thread.
 * Dùng ExecutorService để chạy tất cả Room operations trên background thread.
 * Kết quả trả về ViewModel qua Handler(Main Looper).
 */
public class NotificationRepository {

    private final LmsApiService apiService;
    private final NotificationDao notificationDao;

    // Single-thread executor cho các tác vụ DB tuần tự
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    // Handler để post kết quả về Main Thread cho ViewModel
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Constructor nhận cả ApiService và DAO.
     * LMSApplication hoặc Factory sẽ cung cấp cả hai dependency này.
     */
    public NotificationRepository(LmsApiService apiService, NotificationDao notificationDao) {
        this.apiService = apiService;
        this.notificationDao = notificationDao;
    }

    // ── FETCH NOTIFICATIONS (Network → Room → UI) ─────────────────────────────────

    /**
     * Lấy danh sách thông báo từ API, cache vào Room, rồi trả về UI Model.
     * Callback được gọi trên Main Thread, an toàn để cập nhật LiveData.
     */
    public void fetchNotifications(DataCallback<List<Notification>> callback) {
        apiService.getMyNotifications().enqueue(new Callback<List<NotificationResponse>>() {
            @Override
            public void onResponse(Call<List<NotificationResponse>> call,
                                   Response<List<NotificationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<NotificationResponse> dtos = response.body();

                    // Map DTO → Entity
                    List<NotificationEntity> entities = NotificationMapper.fromDtoList(dtos);

                    // Lưu vào Room trên background thread
                    dbExecutor.execute(() -> {
                        notificationDao.insertOrReplaceAll(entities);

                        // Map Entity → UI Model rồi trả về Main Thread
                        List<Notification> uiModels = NotificationMapper.toUiModelList(entities);
                        mainHandler.post(() -> callback.onSuccess(uiModels));
                    });

                } else {
                    mainHandler.post(() -> callback.onError("Lỗi server: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<List<NotificationResponse>> call, Throwable t) {
                mainHandler.post(() -> callback.onError("Mất kết nối: " + t.getMessage()));
            }
        });
    }

    // ── MARK AS READ (Optimistic local update + background sync) ──────────────────

    /**
     * Đánh dấu một thông báo đã đọc.
     * Bước 1: Cập nhật Room ngay lập tức (optimistic update, isSynced = false).
     * Bước 2: Gọi API ngầm để sync lên server.
     * Bước 3: Nếu thành công → cập nhật isSynced = true trong Room.
     */
    public void markAsRead(String id) {
        // Bước 1: Cập nhật local ngay (trên background thread)
        dbExecutor.execute(() -> notificationDao.markAsReadLocal(id));

        // Bước 2: Sync lên server ngầm
        apiService.markAsRead(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Bước 3: Đánh dấu đã sync thành công
                    dbExecutor.execute(() -> notificationDao.markAsSynced(id));
                }
                // Nếu thất bại: giữ nguyên isSynced = false để retry sau
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Mạng lỗi: isSynced vẫn = false, WorkManager sẽ retry
            }
        });
    }

    /**
     * Đánh dấu TẤT CẢ đã đọc (local trước, sync server sau).
     * Gọi API markAllRead nếu backend có endpoint đó.
     */
    public void markAllAsRead() {
        dbExecutor.execute(() -> notificationDao.markAllAsReadLocal());
        // TODO: Gọi apiService.markAllAsRead() nếu backend có endpoint này
    }

    // ── INTERFACE CALLBACK ────────────────────────────────────────────────────────

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }
}