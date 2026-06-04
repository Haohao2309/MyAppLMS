package com.example.myapplms.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.myapplms.data.local.dao.NotificationDao;
import com.example.myapplms.data.local.entity.NotificationEntity;

/**
 * FILE: app/src/main/java/com/example/myapplms/data/local/LmsDatabase.java
 * HÀNH ĐỘNG: TẠO MỚI file này.
 *
 * HƯỚNG DẪN TÍCH HỢP:
 * 1. Thêm dependency vào app/build.gradle (nếu chưa có):
 *    implementation "androidx.room:room-runtime:2.6.1"
 *    annotationProcessor "androidx.room:room-compiler:2.6.1"
 *
 * 2. Gọi LmsDatabase.getInstance(context) từ LMSApplication.java để lấy instance.
 *    Sau đó expose getNotificationDao() giống cách bạn expose RetrofitClient.
 *
 * 3. Mỗi khi thêm Entity mới (Course, Assignment...) → thêm vào mảng entities[] ở đây
 *    và tăng version lên (ví dụ: version = 2) đồng thời viết Migration.
 */
@Database(
        entities = { NotificationEntity.class },
        version = 2,
        exportSchema = false
)
public abstract class LmsDatabase extends RoomDatabase {

    private static volatile LmsDatabase INSTANCE;

    public abstract NotificationDao notificationDao();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "ALTER TABLE notifications ADD COLUMN link TEXT"
            );
        }
    };

    public static LmsDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (LmsDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    LmsDatabase.class,
                                    "lms_database"
                            )
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}