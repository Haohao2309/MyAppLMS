package com.example.myapplms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.LMSApplication;
import com.example.myapplms.R;
import com.example.myapplms.data.repository.AuthRepository;
import com.example.myapplms.ui.auth.AuthViewModel;
import com.example.myapplms.ui.auth.AuthViewModelFactory;
import com.example.myapplms.ui.auth.LoginActivity;

public class StudentMainActivity extends AppCompatActivity {

    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);

        LMSApplication app = (LMSApplication) getApplication();
        AuthRepository repository = new AuthRepository(
                app.getRetrofitClient().getApiService(),
                app.getSessionManager()
        );
        viewModel = new ViewModelProvider(this,
                new AuthViewModelFactory(repository)).get(AuthViewModel.class);
        findViewById(R.id.btn_logout).setOnClickListener(v -> showLogoutDialog());
    }

    // ── Menu trên ActionBar ───────────────────────────────────
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            showLogoutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ── Hộp thoại xác nhận đăng xuất ─────────────────────────
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> logout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void logout() {
        // Xóa session local trước
        LMSApplication app = (LMSApplication) getApplication();
        app.getSessionManager().clearSession();

        // Gọi API logout background (không cần chờ)
        viewModel.logout();

        // Navigate ngay lập tức
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}