package com.example.myapplms.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.LMSApplication;
import com.example.myapplms.R;
import com.example.myapplms.data.repository.AuthRepository;
import com.example.myapplms.ui.AdminMainActivity;
import com.example.myapplms.ui.StudentMainActivity;
import com.example.myapplms.ui.TeacherMainActivity;

// KHÔNG cần @AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    private AuthViewModel viewModel;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Lấy từ Application — không cần Hilt
        LMSApplication app = (LMSApplication) getApplication();
        AuthRepository repository = new AuthRepository(
                app.getRetrofitClient().getApiService(),
                app.getSessionManager()
        );

        viewModel = new ViewModelProvider(this,
                new AuthViewModelFactory(repository)).get(AuthViewModel.class);

        // Tự động chuyển nếu đã đăng nhập
        if (viewModel.isLoggedIn()) {
            navigateByRole(viewModel.getUserRole());
            return;
        }

        etEmail     = findViewById(R.id.et_email);
        etPassword  = findViewById(R.id.et_password);
        btnLogin    = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);

        btnLogin.setOnClickListener(v -> attemptLogin());

        observeLoginResult();
    }

    private void attemptLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty())    { etEmail.setError("Nhập tài khoản");       return; }
        if (password.isEmpty()) { etPassword.setError("Nhập mật khẩu"); return; }

        viewModel.login(email, password);
    }

    private void observeLoginResult() {
        viewModel.loginResult.observe(this, result -> {
            switch (result.status) {
                case LOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    btnLogin.setEnabled(false);
                    break;

                case SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    navigateByRole(result.data.role);
                    break;

                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void navigateByRole(String role) {
        Intent intent;
        switch (role) {
            case "TEACHER": intent = new Intent(this, TeacherMainActivity.class); break;
            case "ADMIN":   intent = new Intent(this, AdminMainActivity.class);   break;
            default:        intent = new Intent(this, Explore_list_Course.class); break;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}