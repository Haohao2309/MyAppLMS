package com.example.myapplms.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.myapplms.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    // Step controls
    private ViewFlipper viewFlipper;
    private View dot1, dot2, dot3;
    private TextView tvStepLabel;

    // Step 1
    private EditText etForgotEmail;
    private Button btnSendCode;
    private TextView tvBackToLogin1;

    // Step 2 — OTP
    private EditText[] otpBoxes = new EditText[6];
    private TextView tvOtpSentTo, tvTimer, tvResendCode, tvBackStep1;
    private Button btnVerifyOtp;
    private CountDownTimer countDownTimer;

    // Step 3 — New password
    private EditText etNewPassword, etConfirmNewPassword;
    private ImageView ivToggleNewPwd, ivToggleConfirmPwd;
    private View[] strengthBars = new View[4];
    private TextView tvStrengthLabel, tvBackStep2;
    private Button btnResetPassword;

    private boolean isNewPwdVisible = false;
    private boolean isConfirmPwdVisible = false;
    private String userEmail = "";

    // Simulated OTP (in production: received from server)
    private static final String DEMO_OTP = "123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();
        setupStep1();
        setupStep2();
        setupStep3();
    }

    // ── Init ──────────────────────────────────────────────────────────────────
    private void initViews() {
        viewFlipper   = findViewById(R.id.viewFlipper);
        dot1          = findViewById(R.id.dot1);
        dot2          = findViewById(R.id.dot2);
        dot3          = findViewById(R.id.dot3);
        tvStepLabel   = findViewById(R.id.tvStepLabel);

        // Step 1
        etForgotEmail = findViewById(R.id.etForgotEmail);
        btnSendCode   = findViewById(R.id.btnSendCode);
        tvBackToLogin1 = findViewById(R.id.tvBackToLogin1);

        // Step 2
        otpBoxes[0] = findViewById(R.id.otp1);
        otpBoxes[1] = findViewById(R.id.otp2);
        otpBoxes[2] = findViewById(R.id.otp3);
        otpBoxes[3] = findViewById(R.id.otp4);
        otpBoxes[4] = findViewById(R.id.otp5);
        otpBoxes[5] = findViewById(R.id.otp6);
        tvOtpSentTo   = findViewById(R.id.tvOtpSentTo);
        tvTimer       = findViewById(R.id.tvTimer);
        tvResendCode  = findViewById(R.id.tvResendCode);
        tvBackStep1   = findViewById(R.id.tvBackStep1);
        btnVerifyOtp  = findViewById(R.id.btnVerifyOtp);

        // Step 3
        etNewPassword       = findViewById(R.id.etNewPassword);
        etConfirmNewPassword= findViewById(R.id.etConfirmNewPassword);
        ivToggleNewPwd      = findViewById(R.id.ivToggleNewPwd);
        ivToggleConfirmPwd  = findViewById(R.id.ivToggleConfirmPwd);
        strengthBars[0] = findViewById(R.id.strengthBar1);
        strengthBars[1] = findViewById(R.id.strengthBar2);
        strengthBars[2] = findViewById(R.id.strengthBar3);
        strengthBars[3] = findViewById(R.id.strengthBar4);
        tvStrengthLabel = findViewById(R.id.tvStrengthLabel);
        tvBackStep2     = findViewById(R.id.tvBackStep2);
        btnResetPassword = findViewById(R.id.btnResetPassword);
    }

    // ── Step 1: Enter Email ───────────────────────────────────────────────────
    private void setupStep1() {
        btnSendCode.setOnClickListener(v -> {
            String email = etForgotEmail.getText().toString().trim();
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etForgotEmail.setError("Please enter a valid email");
                return;
            }
            userEmail = email;

            // TODO: call backend API to send OTP
            Toast.makeText(this, "Code sent to " + email, Toast.LENGTH_SHORT).show();
            tvOtpSentTo.setText("We sent a 6-digit code to\n" + email);

            goToStep(2);
            startTimer(300); // 5 min
        });

        tvBackToLogin1.setOnClickListener(v -> finish());
    }

    // ── Step 2: OTP Verification ──────────────────────────────────────────────
    private void setupStep2() {
        // Auto-focus next box
        for (int i = 0; i < 6; i++) {
            final int idx = i;
            otpBoxes[i].setFilters(new InputFilter[]{ new InputFilter.LengthFilter(1) });
            otpBoxes[i].addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                    if (s.length() == 1 && idx < 5) {
                        otpBoxes[idx + 1].requestFocus();
                    }
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }

        btnVerifyOtp.setOnClickListener(v -> {
            StringBuilder otp = new StringBuilder();
            for (EditText box : otpBoxes) otp.append(box.getText().toString());

            if (otp.length() < 6) {
                Toast.makeText(this, "Please enter the full 6-digit code", Toast.LENGTH_SHORT).show();
                return;
            }

            if (otp.toString().equals(DEMO_OTP)) {
                if (countDownTimer != null) countDownTimer.cancel();
                goToStep(3);
            } else {
                Toast.makeText(this, "Invalid code. Try again.", Toast.LENGTH_SHORT).show();
                for (EditText box : otpBoxes) box.setText("");
                otpBoxes[0].requestFocus();
            }
        });

        tvResendCode.setOnClickListener(v -> {
            // TODO: re-send OTP API call
            Toast.makeText(this, "Code resent to " + userEmail, Toast.LENGTH_SHORT).show();
            if (countDownTimer != null) countDownTimer.cancel();
            startTimer(300);
            for (EditText box : otpBoxes) box.setText("");
            otpBoxes[0].requestFocus();
        });

        tvBackStep1.setOnClickListener(v -> goToStep(1));
    }

    private void startTimer(int seconds) {
        countDownTimer = new CountDownTimer(seconds * 1000L, 1000) {
            @Override public void onTick(long millisLeft) {
                long m = millisLeft / 60000;
                long s = (millisLeft % 60000) / 1000;
                tvTimer.setText(String.format("Code expires in %02d:%02d", m, s));
            }
            @Override public void onFinish() {
                tvTimer.setText("Code expired. Please resend.");
            }
        }.start();
    }

    // ── Step 3: New Password ──────────────────────────────────────────────────
    private void setupStep3() {
        // Toggle visibility
        ivToggleNewPwd.setOnClickListener(v -> {
            isNewPwdVisible = !isNewPwdVisible;
            togglePwd(etNewPassword, ivToggleNewPwd, isNewPwdVisible);
        });
        ivToggleConfirmPwd.setOnClickListener(v -> {
            isConfirmPwdVisible = !isConfirmPwdVisible;
            togglePwd(etConfirmNewPassword, ivToggleConfirmPwd, isConfirmPwdVisible);
        });

        // Password strength checker
        etNewPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                updateStrength(s.toString());
            }
        });

        btnResetPassword.setOnClickListener(v -> {
            String newPwd     = etNewPassword.getText().toString().trim();
            String confirmPwd = etConfirmNewPassword.getText().toString().trim();

            if (newPwd.length() < 6) {
                etNewPassword.setError("Mật khẩu tối thiểu 6 ký tự"); return;
            }
            if (!newPwd.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]+$")) {
                etNewPassword.setError("Mật khẩu phải chứa ít nhất một chữ cái và một chữ số"); return;
            }
            if (!newPwd.equals(confirmPwd)) {
                etConfirmNewPassword.setError("Mật khẩu không khớp"); return;
            }

            // TODO: call backend to save new password
            Toast.makeText(this, "Password reset successfully! 🎉", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        tvBackStep2.setOnClickListener(v -> goToStep(2));
    }

    private void togglePwd(EditText et, ImageView icon, boolean visible) {
        et.setInputType(visible
                ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        icon.setImageResource(visible ? R.drawable.ic_eye_off : R.drawable.ic_eye);
        et.setSelection(et.getText().length());
    }

    private void updateStrength(String pwd) {
        int score = 0;
        if (pwd.length() >= 6)  score++;
        if (pwd.length() >= 10) score++;
        if (pwd.matches(".*[A-Z].*") && pwd.matches(".*[0-9].*")) score++;
        if (pwd.matches(".*[!@#$%^&*()].*")) score++;

        int[] colors = {
                android.R.color.holo_red_light,
                0xFFFF9800,  // orange
                0xFF8BC34A,  // light green
                android.R.color.holo_green_dark
        };
        String[] labels = { "Weak", "Fair", "Good", "Strong" };

        for (int i = 0; i < 4; i++) {
            if (i < score) {
                strengthBars[i].setBackgroundColor(ContextCompat.getColor(this,
                        i == 0 ? android.R.color.holo_red_light
                                : i == 1 ? android.R.color.holo_orange_light
                                : i == 2 ? android.R.color.holo_green_light
                                : android.R.color.holo_green_dark));
            } else {
                strengthBars[i].setBackgroundColor(
                        ContextCompat.getColor(this, R.color.input_border));
            }
        }

        if (score > 0) {
            tvStrengthLabel.setText("Password strength: " + labels[score - 1]);
        } else {
            tvStrengthLabel.setText("Password strength: —");
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    private void goToStep(int step) {
        viewFlipper.setDisplayedChild(step - 1);
        tvStepLabel.setText("Step " + step + " of 3");
        updateDots(step);
    }

    private void updateDots(int activeStep) {
        View[] dots = { dot1, dot2, dot3 };
        for (int i = 0; i < 3; i++) {
            dots[i].setBackgroundResource(
                    i + 1 == activeStep
                            ? R.drawable.bg_badge_indigo
                            : R.drawable.bg_step_inactive);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}