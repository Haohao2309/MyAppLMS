package com.example.myapplms.data.remote.interceptor;

import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.request.RefreshTokenRequest;
import com.example.myapplms.data.remote.dto.response.ApiResponse;
import com.example.myapplms.data.remote.dto.response.AuthResponse;
import com.example.myapplms.utils.SessionManager;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

public class TokenAuthenticator implements Authenticator {

    private final SessionManager sessionManager;
    private final LmsApiService apiService;

    public TokenAuthenticator(SessionManager sessionManager, LmsApiService apiService) {
        this.sessionManager = sessionManager;
        this.apiService      = apiService;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        // Tránh loop vô hạn
        if (responseCount(response) >= 2) return null;

        String refreshToken = sessionManager.getRefreshToken();
        if (refreshToken == null) return null;

        // Gọi đồng bộ để lấy token mới
        Call<ApiResponse<AuthResponse>> call = apiService.refreshToken(new RefreshTokenRequest(refreshToken));
        retrofit2.Response<ApiResponse<AuthResponse>> refreshResponse = call.execute();

        if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
            ApiResponse<AuthResponse> apiResponse = refreshResponse.body();

            if(apiResponse.isSuccess()&&apiResponse.getData()!=null){
                AuthResponse body = apiResponse.getData();
                sessionManager.saveSession(body.accessToken, body.refreshToken, body.role, body.userId, body.email, body.teacherId, body.studentId, body.imageUrl);
                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + body.accessToken)
                        .build();
            }
        }

        // Refresh thất bại → buộc đăng xuất
        sessionManager.clearSession();
        return null;
    }

    private int responseCount(Response response) {
        int count = 1;
        while ((response = response.priorResponse()) != null) count++;
        return count;
    }
}