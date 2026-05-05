package com.example.myapplms.ui.profile;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplms.databinding.FragmentProfileBinding;
import com.example.myapplms.ui.base.BaseFragment;

public class ProfileFragment extends BaseFragment<FragmentProfileBinding> {

    @NonNull
    @Override
    protected FragmentProfileBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentProfileBinding.inflate(inflater, container, false);
    }

    @Override
    protected void setupViews() {
        // Sau này có thể gọi ViewModel để set Text cho Tên, Avatar từ API
    }

    @Override
    protected void setupListeners() {
        // Bắt sự kiện thay đổi Switch (Toggle)
        getBinding().switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showToast("Push Notifications Enabled");
            } else {
                showToast("Push Notifications Disabled");
            }
        });

        // Bắt sự kiện Click chuyển đổi ngôn ngữ
        getBinding().btnLanguage.setOnClickListener(v -> {
            showToast("Open Language Selection");
        });

        // Bắt sự kiện Đăng xuất
        getBinding().btnLogout.setOnClickListener(v -> {
            // Thực hiện xóa Token, Room DB ở đây
            showToast("Logging out...");
            // Chuyển hướng về LoginActivity
        });
    }
}