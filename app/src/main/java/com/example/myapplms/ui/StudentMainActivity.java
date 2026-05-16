package com.example.myapplms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.LMSApplication;
import com.example.myapplms.R;
import com.example.myapplms.data.repository.AuthRepository;
import com.example.myapplms.ui.auth.AuthViewModel;
import com.example.myapplms.ui.auth.AuthViewModelFactory;
import com.example.myapplms.ui.auth.LoginActivity;
import com.example.myapplms.ui.notification.NotificationsFragment;
import com.example.myapplms.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class StudentMainActivity extends AppCompatActivity {

    private AuthViewModel viewModel;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);

        // 1. Khởi tạo Auth cho việc đăng xuất
        LMSApplication app = (LMSApplication) getApplication();
        AuthRepository repository = new AuthRepository(
                app.getRetrofitClient().getApiService(),
                app.getSessionManager()
        );
        viewModel = new ViewModelProvider(this, new AuthViewModelFactory(repository)).get(AuthViewModel.class);

        // 2. Setup Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // selectedFragment = new HomeFragment(); // TODO: Tạo Fragment sau
                Toast.makeText(this, "Chưa làm Home", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_explore) {
                // selectedFragment = new ExploreFragment(); // TODO: Tạo Fragment sau
            } else if (itemId == R.id.nav_community) {
                // selectedFragment = new CommunityFragment(); // TODO: Tạo Fragment sau
            } else if (itemId == R.id.nav_notifications) {
                selectedFragment = new NotificationsFragment(); // Kéo Fragment Thông báo của bạn vào đây!
            } else if (itemId == R.id.nav_profile) {
                 selectedFragment = new ProfileFragment(); // TODO: Tạo Fragment sau
            }

            // Thực hiện thay thế Fragment vào cái khung
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // 3. Mặc định mở tab Notifications khi vừa vào app để bạn test cho nhanh
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_notifications);
        }
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
    public void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> logout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void logout() {
        LMSApplication app = (LMSApplication) getApplication();
        app.getSessionManager().clearSession();
        viewModel.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}