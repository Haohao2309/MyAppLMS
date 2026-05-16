package com.example.myapplms;

import android.app.Application;

import com.example.myapplms.data.RetrofitClient;
import com.example.myapplms.utils.SessionManager;
import com.example.myapplms.data.local.LmsDatabase;
import com.example.myapplms.data.local.dao.NotificationDao;

public class LMSApplication extends Application {

    private static LMSApplication instance;
    private SessionManager sessionManager;
    private RetrofitClient retrofitClient;
    private LmsDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Khởi tạo một lần duy nhất cho toàn app
        sessionManager   = new SessionManager(this);
        retrofitClient   = RetrofitClient.getInstance(sessionManager);
        database = LmsDatabase.getInstance(this);
    }
    public NotificationDao getNotificationDao() {
        return database.notificationDao();
    }

    public static LMSApplication getInstance() {
        return instance;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public RetrofitClient getRetrofitClient() {
        return retrofitClient;
    }
}