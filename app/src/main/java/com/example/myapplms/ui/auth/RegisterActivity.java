package com.example.myapplms.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.LMSApplication;
import com.example.myapplms.R;
import com.example.myapplms.data.repository.AuthRepository;

public class RegisterActivity extends AppCompatActivity {

    private AuthViewModel viewModel;

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private ImageView ivTogglePassword, ivToggleConfirm;
    private TextView tabStudent, tabInstructor;
    private Button btnSignUp;
    private TextView tvSignIn;
    private ProgressBar progressBar;

    private boolean isPwdVisible     = false;
    private boolean isConfirmVisible = false;
    private String  selectedRole     = "STUDENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Dùng chung ViewModel + Repository với LoginActivity
        LMSApplication app = (LMSApplication) getApplication();
        AuthRepository repository = new AuthRepository(
                app.getRetrofitClient().getApiService(),
                app.getSessionManager()
        );
        viewModel = new ViewModelProvider(this,
                new AuthViewModelFactory(repository)).get(AuthViewModel.class);

        initViews();
        setupTabs();
        setupPasswordToggles();
        //setupTermsText();
        setupSignUp();
        setupSignInLink();
        observeRegisterResult();
    }

    private void initViews() {
        etFullName        = findViewById(R.id.etFullName);
        etEmail           = findViewById(R.id.edtEmail);
        etPassword        = findViewById(R.id.edtPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        ivTogglePassword  = findViewById(R.id.ivTogglePassword);
        ivToggleConfirm   = findViewById(R.id.ivToggleConfirm);
        tabStudent        = findViewById(R.id.tabStudent);
        tabInstructor     = findViewById(R.id.tabInstructor);
        btnSignUp         = findViewById(R.id.btnSignUp);
        tvSignIn          = findViewById(R.id.tvSignIn);
        progressBar       = findViewById(R.id.progress_bar); // thêm vào layout nếu chưa có
    }

    // ── Role Tabs ──────────────────────────────────────────────────────────────
    private void setupTabs() {
        tabStudent.setOnClickListener(v    -> selectTab("STUDENT"));
        tabInstructor.setOnClickListener(v -> selectTab("TEACHER"));
        selectTab("STUDENT"); // default highlight
    }

    private void selectTab(String role) {
        selectedRole = role;
        resetTab(tabStudent);
        resetTab(tabInstructor);

        TextView active = role.equals("STUDENT") ? tabStudent : tabInstructor;
        active.setBackgroundResource(R.drawable.bg_tab_selected);
        active.setTextColor(ContextCompat.getColor(this, R.color.purple_primary));
    }

    private void resetTab(TextView tab) {
        tab.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        tab.setTextColor(ContextCompat.getColor(this, R.color.text_grey));
    }

    // ── Password Toggles ───────────────────────────────────────────────────────
    private void setupPasswordToggles() {
        ivTogglePassword.setOnClickListener(v -> {
            isPwdVisible = !isPwdVisible;
            toggleVisibility(etPassword, ivTogglePassword, isPwdVisible);
        });
        ivToggleConfirm.setOnClickListener(v -> {
            isConfirmVisible = !isConfirmVisible;
            toggleVisibility(etConfirmPassword, ivToggleConfirm, isConfirmVisible);
        });
    }

    private void toggleVisibility(EditText et, ImageView icon, boolean visible) {
        et.setInputType(visible
                ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        icon.setImageResource(visible ? R.drawable.ic_eye_off : R.drawable.ic_eye);
        et.setSelection(et.getText().length());
    }

    // ── Terms Text ─────────────────────────────────────────────────────────────
//    private void setupTermsText() {
//        TextView tvTerms = findViewById(R.id.tvTerms); // đảm bảo id này tồn tại trong layout
//        if (tvTerms == null) return;
//
//        String full = "I agree to the Terms of Service and Privacy Policy";
//        SpannableString ss = new SpannableString(full);
//        int color = ContextCompat.getColor(this, R.color.purple_primary);
//
//        int tosStart = full.indexOf("Terms of Service");
//        ss.setSpan(new ForegroundColorSpan(color),
//                tosStart, tosStart + "Terms of Service".length(),
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        int ppStart = full.indexOf("Privacy Policy");
//        ss.setSpan(new ForegroundColorSpan(color),
//                ppStart, ppStart + "Privacy Policy".length(),
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        tvTerms.setText(ss);
//    }

    // ── Validation & Sign Up ───────────────────────────────────────────────────
    private void setupSignUp() {
        btnSignUp.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String name     = etFullName.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm  = etConfirmPassword.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ tên"); return;
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ"); return;
        }
        if (password.length() < 6) {
            etPassword.setError("Mật khẩu tối thiểu 6 ký tự"); return;
        }
        if (!password.equals(confirm)) {
            etConfirmPassword.setError("Mật khẩu không khớp"+name+"  "+ email+" "+selectedRole); return;
        }


        viewModel.register(name, email, password, selectedRole);
    }

    // ── Observe Result ─────────────────────────────────────────────────────────
    private void observeRegisterResult() {
        viewModel.registerResult.observe(this, result -> {
            switch (result.status) {
                case LOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    btnSignUp.setEnabled(false);
                    break;

                case SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    btnSignUp.setEnabled(true);
                    Toast.makeText(this,
                            "Đăng ký thành công! Vui lòng đăng nhập.",
                            Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                    break;

                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    btnSignUp.setEnabled(true);
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    // ── Sign In Link ───────────────────────────────────────────────────────────
    private void setupSignInLink() {
        tvSignIn.setOnClickListener(v -> finish());
    }
}