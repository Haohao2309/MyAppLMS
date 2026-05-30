package com.example.myapplms.data.mapper;

import com.example.myapplms.data.local.entity.NotificationEntity;
import com.example.myapplms.data.remote.dto.response.NotificationResponse;
import com.example.myapplms.model.Notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * FILE: app/src/main/java/com/example/myapplms/data/local/entity/NotificationMapper.java
 * HÀNH ĐỘNG: TẠO MỚI file này.
 *
 * NHIỆM VỤ: Tách biệt 3 tầng dữ liệu, không để tầng này "biết" tầng kia:
 *
 *   [Network DTO]          [Room Entity]          [UI Model]
 *   NotificationResponse ──→ NotificationEntity ──→ Notification
 *                                                (dùng trong RecyclerView)
 *
 * Tất cả hàm là static — không cần khởi tạo object, gọi trực tiếp:
 *   NotificationMapper.fromDto(dto)
 *   NotificationMapper.toUiModel(entity)
 */
public class NotificationMapper {

    // ── DTO → Entity (khi nhận data từ API, chuẩn bị lưu vào Room) ─────────────

    /**
     * Chuyển một NotificationResponse (từ Retrofit) thành NotificationEntity (để lưu Room).
     * Tính toán dateGroup ngay tại đây để tránh tính toán lặp lại khi hiển thị.
     */
    public static NotificationEntity fromDto(NotificationResponse dto) {
        return new NotificationEntity(
                dto.getId(),
                dto.getTitle(),
                dto.getMessage(),   // DTO dùng "message", Entity & UI Model dùng "body"
                dto.getLink(),
                dto.getType(),
                dto.isRead(),
                dto.getCreatedAt(),
                calculateDateGroup(dto.getCreatedAt()),   // Tính sẵn khi lưu
                System.currentTimeMillis(),               // Timestamp đồng bộ hiện tại
                true                                      // Vừa lấy từ server → đã synced
        );
    }

    /**
     * Chuyển hàng loạt DTO → Entity (dùng khi nhận List từ API).
     */
    public static List<NotificationEntity> fromDtoList(List<NotificationResponse> dtos) {
        List<NotificationEntity> entities = new ArrayList<>();
        for (NotificationResponse dto : dtos) {
            entities.add(fromDto(dto));
        }
        return entities;
    }

    // ── Entity → UI Model (khi lấy từ Room, chuẩn bị hiển thị lên RecyclerView) ─

    /**
     * Chuyển NotificationEntity (từ Room) thành Notification (UI Model cho Adapter).
     */
    public static Notification toUiModel(NotificationEntity entity) {
        return new Notification(
                entity.getId(),
                entity.getTitle(),
                entity.getBody(),
                entity.getLink(),
                entity.getType(),
                entity.getCreatedAt(),
                entity.getDateGroup(),
                entity.isRead()
        );
    }

    /**
     * Chuyển hàng loạt Entity → UI Model.
     */
    public static List<Notification> toUiModelList(List<NotificationEntity> entities) {
        List<Notification> uiModels = new ArrayList<>();
        for (NotificationEntity entity : entities) {
            uiModels.add(toUiModel(entity));
        }
        return uiModels;
    }

    // ── Helper: Tính nhóm ngày ───────────────────────────────────────────────────

    /**
     * Nhận chuỗi ISO 8601, trả về "Today" / "Yesterday" / "Earlier".
     * Logic giống NotificationRepository.calculateDateGroup() nhưng được centralize tại đây.
     * Repository sẽ KHÔNG cần viết lại hàm này nữa.
     */
    public static String calculateDateGroup(String createdAtString) {
        if (createdAtString == null || createdAtString.isEmpty()) return "Earlier";
        try {
            // Làm sạch chuỗi thời gian: bỏ mili-giây và múi giờ
            String cleanTime = createdAtString;
            if (cleanTime.contains(".")) {
                cleanTime = cleanTime.substring(0, cleanTime.indexOf("."));
            } else if (cleanTime.contains("+")) {
                cleanTime = cleanTime.substring(0, cleanTime.indexOf("+"));
            } else if (cleanTime.endsWith("Z")) {
                cleanTime = cleanTime.substring(0, cleanTime.length() - 1);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(cleanTime);

            if (date != null) {
                Calendar now = Calendar.getInstance();
                Calendar notifDate = Calendar.getInstance();
                notifDate.setTime(date);

                if (now.get(Calendar.YEAR) == notifDate.get(Calendar.YEAR) &&
                        now.get(Calendar.DAY_OF_YEAR) == notifDate.get(Calendar.DAY_OF_YEAR)) {
                    return "Today";
                }

                now.add(Calendar.DAY_OF_YEAR, -1);
                if (now.get(Calendar.YEAR) == notifDate.get(Calendar.YEAR) &&
                        now.get(Calendar.DAY_OF_YEAR) == notifDate.get(Calendar.DAY_OF_YEAR)) {
                    return "Yesterday";
                }

                return "Earlier";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Earlier";
    }
}