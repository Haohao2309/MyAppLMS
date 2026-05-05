package com.example.myapplms;

import android.app.Application;

import com.example.myapplms.data.RetrofitClient;
import com.example.myapplms.utils.SessionManager;

public class LMSApplication extends Application {

    private static LMSApplication instance;
    private SessionManager sessionManager;
    private RetrofitClient retrofitClient;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Khởi tạo một lần duy nhất cho toàn app
        sessionManager   = new SessionManager(this);
        retrofitClient   = RetrofitClient.getInstance(sessionManager);
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