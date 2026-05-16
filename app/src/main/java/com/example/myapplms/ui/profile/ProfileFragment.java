package com.example.myapplms.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.LMSApplication;
import com.example.myapplms.data.repository.TeacherRepository;
import com.example.myapplms.databinding.FragmentProfileBinding;
import com.example.myapplms.ui.base.BaseFragment;
import com.example.myapplms.ui.teacher.TeacherViewModel;
import com.example.myapplms.ui.teacher.TeacherViewModelFactory;
import com.example.myapplms.utils.SessionManager;

public class ProfileFragment extends BaseFragment<FragmentProfileBinding> {

    private TeacherViewModel teacherViewModel;
    private SessionManager sessionManager;

    @NonNull
    @Override
    protected FragmentProfileBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentProfileBinding.inflate(inflater, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LMSApplication app = (LMSApplication) requireActivity().getApplication();

        TeacherRepository teacherRepository = new TeacherRepository(app.getRetrofitClient().getApiService());
        TeacherViewModelFactory teacherViewModelFactory = new TeacherViewModelFactory(teacherRepository);
        teacherViewModel = new ViewModelProvider(this, teacherViewModelFactory).get(TeacherViewModel.class);

        // Khởi tạo SessionManager ở đây
        sessionManager = new SessionManager(requireContext());
    }
    @Override
    protected void setupViews() {
        teacherViewModel.teacher.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            System.out.println("PROFILE Status: " + resource.status);          // Xem status
            System.out.println("PROFILE Data: " + resource.data);              // Xem data null không
            System.out.println("PROFILE Message: " + resource.message);        // Xem lỗi gì

            switch (resource.status) {
                case LOADING:
                    Log.d("PROFILE", "Đang loading...");
                    break;
                case SUCCESS:
                    if (resource.data != null) {
                        getBinding().tvName.setText(resource.data.getFirstName());
                        getBinding().tvBio.setText(resource.data.getBio());
                        getBinding().tvRole.setText(resource.data.getDegree());
                    }
                    break;
                case ERROR:
                    Log.e("PROFILE", "Lỗi: " + resource.message);
                    showToast("Lỗi: " + resource.message);
                    break;
            }
        });

        String userIdStr = sessionManager.getUserId();
        Log.d("PROFILE", "userId từ session: " + userIdStr); // Xem có lấy được không

        if (userIdStr != null) {
            try {
                Integer userId = Integer.parseInt(userIdStr);
                teacherViewModel.getTeacherbyId(userId);
            } catch (NumberFormatException e) {
                showToast("ID không hợp lệ!");
            }
        }
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
            // Xóa Session đăng nhập khi nhấn nút Đăng xuất
            sessionManager.clearSession();
            showToast("Logging out...");
            // Chuyển hướng về LoginActivity tại đây nếu cần
        });
    }
}