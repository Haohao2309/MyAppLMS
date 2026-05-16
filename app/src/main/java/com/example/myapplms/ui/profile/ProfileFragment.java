package com.example.myapplms.ui.profile;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplms.databinding.FragmentProfileBinding;
import com.example.myapplms.ui.StudentMainActivity;
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
            // Kiểm tra xem Fragment này đang nằm trong StudentMainActivity không
            if (getActivity() instanceof StudentMainActivity) {
                // Ép kiểu và gọi hàm hiển thị Dialog đăng xuất từ Activity
                ((StudentMainActivity) getActivity()).showLogoutDialog();
            }
            // Lưu ý: Sau này nếu bạn gắn ProfileFragment vào TeacherMainActivity,
            // bạn chỉ cần thêm cục "else if" tương tự vào đây là xong!
        });
    }
}