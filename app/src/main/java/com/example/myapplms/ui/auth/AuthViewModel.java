package com.example.myapplms.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplms.data.remote.dto.response.AuthResponse;
import com.example.myapplms.data.repository.AuthRepository;
import com.example.myapplms.utils.Resource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<Resource<AuthResponse>> _loginResult = new MutableLiveData<>();
    public final LiveData<Resource<AuthResponse>> loginResult = _loginResult;

    private final MutableLiveData<Resource<AuthResponse>> _registerResult = new MutableLiveData<>();
    public final LiveData<Resource<AuthResponse>> registerResult = _registerResult;

    // LiveData cho kết quả đăng xuất (Boolean — true = thành công)
    private final MutableLiveData<Resource<Boolean>> _logoutResult = new MutableLiveData<>();
    public final LiveData<Resource<Boolean>> logoutResult = _logoutResult;

    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }



    public void register(String fullName, String email, String password, String role) {
        _registerResult.setValue(Resource.loading());

        executor.execute(() -> {
            Resource<AuthResponse> result =
                    authRepository.register(fullName, email, password, role);
            _registerResult.postValue(result);
        });
    }

    public void login(String username, String password) {
        _loginResult.setValue(Resource.loading());
        executor.execute(() -> {
            Resource<AuthResponse> result = authRepository.login(username, password);
            _loginResult.postValue(result);
        });
    }

    /**
     * Gọi API logout, sau đó emit kết quả qua logoutResult LiveData.
     * Fragment quan sát logoutResult để điều hướng về màn hình Login.
     */
    public void logout() {
        _logoutResult.setValue(Resource.loading());
        executor.execute(() -> {
            try {
                authRepository.logout();
                // logout() trong repository không trả về gì → coi là thành công
                _logoutResult.postValue(Resource.success(true));
            } catch (Exception e) {
                _logoutResult.postValue(Resource.error("Đăng xuất thất bại: " + e.getMessage(), false));
            }
        });
    }

    public boolean isLoggedIn() {
        return authRepository.isLoggedIn();
    }

    public String getUserRole() {
        return authRepository.getUserRole();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}