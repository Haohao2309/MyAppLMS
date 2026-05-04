package com.example.myapplms.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME     = "lms_session";
    private static final String KEY_ACCESS    = "access_token";
    private static final String KEY_REFRESH   = "refresh_token";
    private static final String KEY_ROLE      = "role";
    private static final String KEY_USER_ID   = "user_id";
    private static final String KEY_FULL_NAME = "full_name";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String accessToken, String refreshToken,
                            String role, String userId, String fullName) {
        prefs.edit()
                .putString(KEY_ACCESS,    accessToken)
                .putString(KEY_REFRESH,   refreshToken)
                .putString(KEY_ROLE,      role)
                .putString(KEY_USER_ID,   userId)
                .putString(KEY_FULL_NAME, fullName)
                .apply();
    }

    public String getAccessToken()  { return prefs.getString(KEY_ACCESS,    null); }
    public String getRefreshToken() { return prefs.getString(KEY_REFRESH,   null); }
    public String getRole()         { return prefs.getString(KEY_ROLE,      null); }
    public String getUserId()       { return prefs.getString(KEY_USER_ID,   null); }
    public String getFullName()     { return prefs.getString(KEY_FULL_NAME, null); }

    public boolean isLoggedIn() {
        return getAccessToken() != null;
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}