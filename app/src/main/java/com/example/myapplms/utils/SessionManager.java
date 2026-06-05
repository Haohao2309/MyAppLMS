package com.example.myapplms.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME     = "lms_session";
    private static final String KEY_ACCESS    = "access_token";
    private static final String KEY_REFRESH   = "refresh_token";
    private static final String KEY_ROLE      = "role";
    private static final String KEY_USER_ID   = "user_id";
    private static final String KEY_EMAIL     = "email";
    private static final String KEY_TEACHER_ID = "teacher_id";
    private static final String KEY_STUDENT_ID = "student_id";
    private static final String KEY_IMAGE_URL  = "image_url";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Lưu toàn bộ session sau khi login thành công.
     * imageUrl được lưu luôn ở đây — không cần gọi saveImageUrl() riêng.
     */
    public void saveSession(String accessToken, String refreshToken,
                            String role, String userId, String email,
                            Integer teacherId, Integer studentId,
                            String imageUrl) {               // ← thêm imageUrl
        prefs.edit()
                .putString(KEY_ACCESS,     accessToken)
                .putString(KEY_REFRESH,    refreshToken)
                .putString(KEY_ROLE,       role)
                .putString(KEY_USER_ID,    userId)
                .putString(KEY_EMAIL,      email)
                .putInt   (KEY_TEACHER_ID, teacherId != null ? teacherId : -1)
                .putInt   (KEY_STUDENT_ID, studentId != null ? studentId : -1)
                .putString(KEY_IMAGE_URL,  imageUrl != null ? imageUrl : "") // ← lưu luôn
                .apply();
    }

    /** Gọi riêng khi user vừa upload avatar thành công */
    public void saveImageUrl(String imageUrl) {
        prefs.edit().putString(KEY_IMAGE_URL, imageUrl).apply();
    }

    public String getImageUrl()     { return prefs.getString(KEY_IMAGE_URL,  null); }
    public String getAccessToken()  { return prefs.getString(KEY_ACCESS,     null); }
    public String getRefreshToken() { return prefs.getString(KEY_REFRESH,    null); }
    public String getRole()         { return prefs.getString(KEY_ROLE,       null); }
    public String getUserId()       { return prefs.getString(KEY_USER_ID,    null); }
    public String getKeyEmail()     { return prefs.getString(KEY_EMAIL,      null); }

    public Integer getTeacherId() {
        int id = prefs.getInt(KEY_TEACHER_ID, -1);
        return id == -1 ? null : id;
    }

    public Integer getStudentId() {
        int id = prefs.getInt(KEY_STUDENT_ID, -1);
        return id == -1 ? null : id;
    }

    public boolean isLoggedIn() { return getAccessToken() != null; }

    public void clearSession() { prefs.edit().clear().apply(); }
}