package com.example.myapplms.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplms.data.local.entity.NotificationEntity;

import java.util.List;

/**
 * FILE: app/src/main/java/com/example/myapplms/data/local/dao/NotificationDao.java
 * HÀNH ĐỘNG: TẠO MỚI file này.
 *
 * THIẾT KẾ DAO:
 * - Trả về LiveData<> cho các query dùng trong ViewModel → tự động update UI.
 * - Trả về List<> cho các hàm sync chạy trên background thread.
 * - OnConflictStrategy.REPLACE: khi server trả về data mới, ghi đè hoàn toàn.
 *   (Ngoại lệ: markAsReadLocal dùng @Query UPDATE riêng để tránh ghi đè isSynced)
 */
@Dao
public interface NotificationDao {

    // ── ĐỌC (READ) ───────────────────────────────────────────────────────────────

    /**
     * Lấy TẤT CẢ thông báo, sắp xếp mới nhất trên đầu.
     * Trả về LiveData → Fragment tự observe, UI tự cập nhật khi DB thay đổi.
     */
    @Query("SELECT * FROM notifications WHERE student_id = :studentId ORDER BY created_at DESC")
    LiveData<List<NotificationEntity>> getAllNotifications(int studentId);

    /**
     * Lấy thông báo theo TYPE để phục vụ bộ lọc chip.
     * Ví dụ: type = "achievement", "grade", "message"...
     */
    @Query("SELECT * FROM notifications WHERE type = :type AND student_id = :studentId ORDER BY created_at DESC")
    LiveData<List<NotificationEntity>> getNotificationsByType(String type, int studentId);

    /**
     * Lấy danh sách thông báo CHƯA đọc (dùng để đếm badge).
     */
    @Query("SELECT * FROM notifications WHERE is_read = 0 AND student_id = :studentId ORDER BY created_at DESC")
    LiveData<List<NotificationEntity>> getUnreadNotifications(int studentId);

    /**
     * Đếm số lượng chưa đọc (dùng cho badge số góc icon nav).
     */
    @Query("SELECT COUNT(*) FROM notifications WHERE is_read = 0 AND student_id = :studentId")
    LiveData<Integer> getUnreadCount(int studentId);

    /**
     * Lấy danh sách các thông báo chưa đồng bộ lên server.
     * Chạy trên background thread (không LiveData) để WorkManager xử lý.
     */
    @Query("SELECT * FROM notifications WHERE is_synced = 0 AND student_id = :studentId")
    List<NotificationEntity> getUnsyncedNotifications(int studentId);

    // ── GHI (WRITE) ──────────────────────────────────────────────────────────────

    /**
     * Insert hoặc REPLACE hàng loạt từ API response.
     * OnConflictStrategy.REPLACE: nếu id đã tồn tại → ghi đè hoàn toàn.
     * Chú ý: hàm này sẽ reset isSynced = true (vì data từ server luôn là synced).
     * Đảm bảo set isSynced = true trước khi gọi hàm này.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrReplaceAll(List<NotificationEntity> notifications);

    /**
     * Insert một thông báo đơn lẻ (ví dụ: nhận từ FCM Push Notification).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOne(NotificationEntity notification);

    /**
     * Đánh dấu một thông báo là ĐÃ ĐỌC trên local ngay lập tức (optimistic update).
     * isSynced = 0 → sẽ được sync lên server sau (qua WorkManager hoặc Retrofit).
     * Không dùng @Update để tránh ghi đè các trường khác.
     */
    @Query("UPDATE notifications SET is_read = 1, is_synced = 0 WHERE id = :id AND student_id = :studentId")
    void markAsReadLocal(String id, int studentId);

    /**
     * Đánh dấu TẤT CẢ là đã đọc trên local.
     */
    @Query("UPDATE notifications SET is_read = 1, is_synced = 0 WHERE student_id = :studentId")
    void markAllAsReadLocal(int studentId);

    /**
     * Sau khi sync thành công lên server, cập nhật cờ isSynced = true.
     */
    @Query("UPDATE notifications SET is_synced = 1 WHERE id = :id AND student_id = :studentId")
    void markAsSynced(String id, int studentId);

    /**
     * Xóa toàn bộ cache (dùng khi logout hoặc force refresh).
     */
    @Query("DELETE FROM notifications WHERE student_id = :studentId")
    void clearAll(int studentId);

    /**
     * Xóa các thông báo cũ hơn N ngày để giữ DB không phình to.
     * Tham số: cutoffTimestamp là epoch ms tính từ System.currentTimeMillis() - N_days_ms
     */
    @Query("DELETE FROM notifications WHERE created_at < :cutoffIsoDate AND student_id = :studentId")
    void deleteOlderThan(String cutoffIsoDate, int studentId);
}