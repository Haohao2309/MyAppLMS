package com.example.myapplms.data;
import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.interceptor.AuthInterceptor;
import com.example.myapplms.data.remote.interceptor.TokenAuthenticator;
import com.example.myapplms.utils.Constants;
import com.example.myapplms.utils.SessionManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static RetrofitClient instance;
    private final LmsApiService apiService;

    private RetrofitClient(SessionManager sessionManager) {
        // Logging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // OkHttpClient với AuthInterceptor + TokenAuthenticator
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(sessionManager))
                .authenticator(new TokenAuthenticator(sessionManager, getApiServiceWithoutAuth()))
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(LmsApiService.class);
    }

    // Retrofit không có interceptor — dùng riêng cho TokenAuthenticator
    private LmsApiService getApiServiceWithoutAuth() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(LmsApiService.class);
    }

    public static synchronized RetrofitClient getInstance(SessionManager sessionManager) {
        if (instance == null) {
            instance = new RetrofitClient(sessionManager);
        }
        return instance;
    }

    public LmsApiService getApiService() {
        return apiService;
    }
}
