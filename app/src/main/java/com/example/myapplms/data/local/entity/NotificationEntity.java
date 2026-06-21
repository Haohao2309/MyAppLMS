package com.example.myapplms.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * FILE: app/src/main/java/com/example/myapplms/data/local/entity/NotificationEntity.java
 * HÀNH ĐỘNG: TẠO MỚI file này. Đây là Entity cho Room Database.
 *
 * THIẾT KẾ:
 * - id: String (UUID từ server) — PRIMARY KEY, không tự tăng vì dùng ID server.
 * - Tất cả các trường ánh xạ 1-1 với JSON từ API.
 * - Thêm trường localUpdatedAt để biết lần cuối đồng bộ.
 * - Thêm trường isSynced để biết đã đồng bộ lên server chưa
 *   (ví dụ: người dùng đọc offline → đánh dấu chờ sync).
 *
 * TABLENAME: "notifications" — snake_case theo convention SQL.
 */
@Entity(tableName = "notifications")
public class NotificationEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "body")
    private String body;

    // Loại thông báo: "achievement", "grade", "message", "announcement", "payment"
    @ColumnInfo(name = "link")
    private String link;
    @ColumnInfo(name = "type")
    private String type;

    // Trạng thái đọc — có thể thay đổi offline
    @ColumnInfo(name = "is_read")
    private boolean isRead;

    // Thời gian tạo từ server (ISO 8601: "2026-05-14T10:30:00")
    @ColumnInfo(name = "created_at")
    private String createdAt;

    // Nhóm ngày hiển thị: "Today", "Yesterday", "Earlier"
    // Được tính toán khi insert vào DB, không lấy từ server
    @ColumnInfo(name = "date_group")
    private String dateGroup;

    // Timestamp (epoch ms) ghi nhận lần cuối đồng bộ từ server
    // Dùng để biết data có "cũ" chưa (stale check)
    @ColumnInfo(name = "local_updated_at")
    private long localUpdatedAt;

    // true = trạng thái isRead đã được gửi lên server thành công
    // false = đang chờ sync (ví dụ: user đọc khi offline)
    @ColumnInfo(name = "is_synced")
    private boolean isSynced;

    @ColumnInfo(name = "student_id")
    private int studentId;

    // ── Constructor ───────────────────────────────────────────────────────────────

    public NotificationEntity(@NonNull String id, String title, String body, String link,
                              String type, boolean isRead, String createdAt, String dateGroup,
                              long localUpdatedAt, boolean isSynced, int studentId) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.link = link;   // ← thêm dòng này
        this.type = type;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.dateGroup = dateGroup;
        this.localUpdatedAt = localUpdatedAt;
        this.isSynced = isSynced;
        this.studentId = studentId;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────────

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getDateGroup() { return dateGroup; }
    public void setDateGroup(String dateGroup) { this.dateGroup = dateGroup; }

    public long getLocalUpdatedAt() { return localUpdatedAt; }
    public void setLocalUpdatedAt(long localUpdatedAt) { this.localUpdatedAt = localUpdatedAt; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }
}