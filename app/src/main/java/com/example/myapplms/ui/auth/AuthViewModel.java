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

    // Constructor thủ công — không cần @HiltViewModel / @Inject
    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void login(String username, String password) {
        _loginResult.setValue(Resource.loading());

        executor.execute(() -> {
            Resource<AuthResponse> result = authRepository.login(username, password);
            _loginResult.postValue(result);
        });
    }

    public void logout() {
        executor.execute(authRepository::logout);
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