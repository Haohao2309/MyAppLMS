package com.example.myapplms.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplms.R;
import com.example.myapplms.ui.explore.ExploreListCourseFragment;
import com.example.myapplms.ui.notification.NotificationsFragment;
import com.example.myapplms.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TeacherMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // Xử lý sự kiện click menu
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_explore) {
                replaceFragment(new ExploreListCourseFragment());
                return true;
            } else if (itemId == R.id.nav_notifications) {
                replaceFragment(new NotificationsFragment());
                return true;
            } else if (itemId == R.id.nav_profile) {
                replaceFragment(new ProfileFragment());
                return true;
            }
            // Thêm các tab khác (Home, Community) nếu bạn đã có Fragment tương ứng
            return false;
        });

        // Mặc định hiển thị Explore khi vừa vào
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_profile);
        }
    }

    /**
     * Hàm dùng để thay thế Fragment trong fragment_container
     */
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Thiết lập hiệu ứng chuyển cảnh mượt mà (tùy chọn)
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

}