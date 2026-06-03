package com.example.myapplms.data.repository;

import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.request.LoginRequest;
import com.example.myapplms.data.remote.dto.request.RefreshTokenRequest;
import com.example.myapplms.data.remote.dto.request.RegisterRequest;
import com.example.myapplms.data.remote.dto.response.AuthResponse;
import com.example.myapplms.utils.Resource;
import com.example.myapplms.utils.SessionManager;

import java.io.IOException;

import retrofit2.Response;

public class AuthRepository {

    private final LmsApiService apiService;
    private final SessionManager sessionManager;

    public AuthRepository(LmsApiService apiService, SessionManager sessionManager) {
        this.apiService     = apiService;
        this.sessionManager = sessionManager;
    }

    // ── Register ──────────────────────────────────────────────
    public Resource<AuthResponse> register(String username, String email,
                                           String password, String role) {
        try {
            //  ĐÃ SỬA: Hứng trực tiếp AuthResponse từ Server (Bỏ ApiResponse)
            Response<AuthResponse> response = apiService
                    .register(new RegisterRequest(username, email, password, role))
                    .execute();
            System.out.println(username + " " + email + " " + password + " " + role);

            if (response.isSuccessful() && response.body() != null) {
                AuthResponse body = response.body();

                // Lưu session trực tiếp từ dữ liệu trả về
                sessionManager.saveSession(
                        body.accessToken,
                        body.refreshToken,
                        body.tokenType, // Lưu ý: Tham số thứ 3 của saveSession thường là tokenType hoặc role, bạn check kỹ thứ tự nhé
                        String.valueOf(body.userId),
                        body.email,
                        body.teacherId,
                        body.studentId
                );
                return Resource.success(body);
            }

            if (response.code() == 409) {
                return Resource.error("Email đã được sử dụng", null);
            }
            return Resource.error("Lỗi server: " + response.code(), null);

        } catch (IOException e) {
            return Resource.error("Không có kết nối mạng", null);
        }
    }

    // ── Login ─────────────────────────────────────────────────
    public Resource<AuthResponse> login(String username, String password) {
        try {
            //  ĐÃ SỬA: Đổi từ Response<ApiResponse<AuthResponse>> sang Response<AuthResponse>
            Response<AuthResponse> response = apiService
                    .login(new LoginRequest(username, password))
                    .execute();

            if (response.isSuccessful() && response.body() != null) {
                AuthResponse body = response.body();

                // Đăng nhập thành công -> Tiến hành lưu Session dữ liệu
                sessionManager.saveSession(
                        body.accessToken,
                        body.refreshToken,
                        body.role,
                        String.valueOf(body.userId),
                        body.email,
                        body.teacherId,
                        body.studentId
                );
                return Resource.success(body);
            }

            if (response.code() == 401) {
                return Resource.error("Email hoặc mật khẩu không đúng", null);
            }
            return Resource.error("Lỗi server: " + response.code(), null);

        } catch (IOException e) {
            return Resource.error("Không có kết nối mạng", null);
        }
    }

    // ── Logout ────────────────────────────────────────────────
    public void logout() {
        String refreshToken = sessionManager.getRefreshToken();
        if (refreshToken != null) {
            try {
                apiService.logout(new RefreshTokenRequest(refreshToken)).execute();
            } catch (IOException ignored) {}
        }
        sessionManager.clearSession();
    }

    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public String getUserRole() {
        return sessionManager.getRole();
    }
}