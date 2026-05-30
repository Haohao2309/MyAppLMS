package com.example.myapplms.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.LMSApplication;
import com.example.myapplms.R;
import com.example.myapplms.ui.TeacherMainActivity;
import com.example.myapplms.data.repository.AuthRepository;
import com.example.myapplms.ui.AdminMainActivity;
import com.example.myapplms.ui.StudentMainActivity;
import com.example.myapplms.ui.TeacherMainActivity;
import com.example.myapplms.ui.community.CommunityActivity;

// KHÔNG cần @AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    private AuthViewModel viewModel;
    private EditText etEmail, etPassword;

    private ImageView ivTogglePassword;
    private TextView tabStudent, tabInstructor;
    private TextView tvForgotPassword, tvSignUpFree;
    private boolean isPasswordVisible = false;
    private String selectedRole = "Student";
    private Button btnLogin;
    private ProgressBar progressBar;

    private static final String STUDENT_EMAIL    = "student@learnhub.com";
    private static final String INSTRUCTOR_EMAIL = "instructor@learnhub.com";
    private static final String DEMO_PASSWORD    = "";

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

        etEmail     = findViewById(R.id.edtEmail);
        etPassword  = findViewById(R.id.edtPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        tabStudent       = findViewById(R.id.tabStudent);
        tabInstructor    = findViewById(R.id.tabInstructor);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUpFree     = findViewById(R.id.tvSignUpFree);
        btnLogin    = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progress_bar);
        btnLogin.setOnClickListener(v -> attemptLogin());
        setupTabListeners();
        setupPasswordToggle();
        setupLinks();
        observeLoginResult();
    }


    private void setupTabListeners() {
        tabStudent.setOnClickListener(v    -> selectTab("Student"));
        tabInstructor.setOnClickListener(v -> selectTab("TEACHER"));
    }

    private void selectTab(String role) {
        selectedRole = role;

        // Reset all tabs
        resetTab(tabStudent);
        resetTab(tabInstructor);


        // Highlight selected
        TextView selectedTab;
        String prefillEmail;
        switch (role) {
            case "TEACHER":
                selectedTab  = tabInstructor;
                prefillEmail = INSTRUCTOR_EMAIL;
                break;

            default: // Student
                selectedTab  = tabStudent;
                prefillEmail = STUDENT_EMAIL;
                break;
        }

        selectedTab.setBackgroundResource(R.drawable.bg_tab_selected);
        selectedTab.setTextColor(getResources().getColor(R.color.purple_primary, null));
        selectedTab.setTextSize(14);

        // Auto-fill demo email
        etEmail.setHint(prefillEmail);
        etPassword.setText(DEMO_PASSWORD);
    }

    private void resetTab(TextView tab) {
        tab.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        tab.setTextColor(getResources().getColor(R.color.text_grey, null));
    }

    private void setupPasswordToggle() {
        ivTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                etPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_eye_off);
            } else {
                etPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_eye);
            }
            // Keep cursor at end
            etPassword.setSelection(etPassword.getText().length());
        });
    }

    private void setupLinks() {
        tvForgotPassword.setOnClickListener(v -> {
            Intent in = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(in);
        });

        tvSignUpFree.setOnClickListener(v -> {
            Intent in = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(in);
        });
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
            default:        intent = new Intent(this, StudentMainActivity.class); break;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}