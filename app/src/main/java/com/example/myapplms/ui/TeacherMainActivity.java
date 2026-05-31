package com.example.myapplms.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplms.R;
import com.example.myapplms.ui.community.CommunityFragment;
import com.example.myapplms.ui.explore.ExploreListCourseFragment;
import com.example.myapplms.ui.home.HomeFragment;
import com.example.myapplms.ui.notification.NotificationsFragment;
import com.example.myapplms.ui.profile.ProfileFragment;
import com.example.myapplms.ui.teacher.TeacherHomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TeacherMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                replaceFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_explore) {
                replaceFragment(new ExploreListCourseFragment());
                return true;
            } else if (id == R.id.nav_community) {
                replaceFragment(new CommunityFragment());
                return true;
            } else if (id == R.id.nav_notifications) {
                replaceFragment(new NotificationsFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                replaceFragment(new ProfileFragment());
                return true;
            }
            return false;
        });

        // Mặc định vào Home (Overview)
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}