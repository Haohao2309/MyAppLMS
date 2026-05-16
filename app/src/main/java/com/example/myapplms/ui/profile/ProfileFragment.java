package com.example.myapplms.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.LMSApplication;
import com.example.myapplms.data.repository.AuthRepository;
import com.example.myapplms.data.repository.TeacherRepository;
import com.example.myapplms.databinding.FragmentProfileBinding;
import com.example.myapplms.ui.auth.AuthViewModel;
import com.example.myapplms.ui.auth.AuthViewModelFactory;
import com.example.myapplms.ui.auth.LoginActivity;
import com.example.myapplms.ui.base.BaseFragment;
import com.example.myapplms.ui.teacher.TeacherViewModel;
import com.example.myapplms.ui.teacher.TeacherViewModelFactory;
import com.example.myapplms.utils.SessionManager;

public class ProfileFragment extends BaseFragment<FragmentProfileBinding> {

    private static final String TAG = "ProfileFragment";

    private TeacherViewModel teacherViewModel;
    private AuthViewModel authViewModel;
    private SessionManager sessionManager;

    // -------------------------------------------------------------------------
    // Inflate binding
    // -------------------------------------------------------------------------

    @NonNull
    @Override
    protected FragmentProfileBinding inflateBinding(@NonNull LayoutInflater inflater,
                                                    @Nullable ViewGroup container) {
        return FragmentProfileBinding.inflate(inflater, container, false);
    }

    // -------------------------------------------------------------------------
    // onCreate — khởi tạo ViewModel & SessionManager
    // -------------------------------------------------------------------------

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LMSApplication app = (LMSApplication) requireActivity().getApplication();
        sessionManager = new SessionManager(requireContext());

        // TeacherViewModel
        TeacherRepository teacherRepository =
                new TeacherRepository(app.getRetrofitClient().getApiService());
        TeacherViewModelFactory teacherFactory = new TeacherViewModelFactory(teacherRepository);
        teacherViewModel = new ViewModelProvider(this, teacherFactory).get(TeacherViewModel.class);

        // AuthViewModel — dùng để gọi logout
        AuthRepository authRepository = new AuthRepository(
                app.getRetrofitClient().getApiService(), sessionManager);
        AuthViewModelFactory authFactory = new AuthViewModelFactory(authRepository);
        authViewModel = new ViewModelProvider(this, authFactory).get(AuthViewModel.class);
    }

    // -------------------------------------------------------------------------
    // onViewCreated — đăng ký observers & load dữ liệu
    // -------------------------------------------------------------------------

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeTeacher();
        observeLogout();
        loadTeacherProfile();
    }

    // -------------------------------------------------------------------------
    // setupViews
    // -------------------------------------------------------------------------

    @Override
    protected void setupViews() {
        // Observer đã chuyển sang onViewCreated() để đảm bảo lifecycle đúng
    }

    // -------------------------------------------------------------------------
    // setupListeners
    // -------------------------------------------------------------------------

    @Override
    protected void setupListeners() {

        getBinding().switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showToast("Push Notifications Enabled");
            } else {
                showToast("Push Notifications Disabled");
            }
        });

        getBinding().btnLanguage.setOnClickListener(v ->
                showToast("Open Language Selection"));

        // Nút đăng xuất — gọi authViewModel.logout() thay vì xóa session thủ công
        getBinding().btnLogout.setOnClickListener(v -> {
            getBinding().btnLogout.setEnabled(false); // tránh bấm nhiều lần
            authViewModel.logout();
        });
    }

    // -------------------------------------------------------------------------
    // Observers
    // -------------------------------------------------------------------------

    /** Quan sát thông tin giáo viên và cập nhật UI. */
    private void observeTeacher() {
        teacherViewModel.teacher.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            Log.d(TAG, "Teacher | Status: " + resource.status
                    + " | Data: " + resource.data
                    + " | Message: " + resource.message);

            switch (resource.status) {
                case LOADING:
                    Log.d(TAG, "Đang tải thông tin giáo viên...");
                    // TODO: getBinding().progressBar.setVisibility(View.VISIBLE);
                    break;

                case SUCCESS:
                    // TODO: getBinding().progressBar.setVisibility(View.GONE);
                    if (resource.data != null) {
                        String fullName = resource.data.getFirstName()
                                + " " + resource.data.getLastName();
                        getBinding().tvName.setText(fullName.trim());
                        getBinding().tvBio.setText(resource.data.getBio());
                        getBinding().tvRole.setText(resource.data.getDegree());
                        Log.d(TAG, "Hiển thị thành công: " + fullName);
                    } else {
                        Log.w(TAG, "SUCCESS nhưng data null");
                    }
                    break;

                case ERROR:
                    // TODO: getBinding().progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Lỗi tải profile: " + resource.message);
                    showToast("Lỗi: " + resource.message);
                    break;
            }
        });
    }

    /** Quan sát kết quả đăng xuất và điều hướng về LoginActivity. */
    private void observeLogout() {
        authViewModel.logoutResult.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            Log.d(TAG, "Logout | Status: " + resource.status);

            switch (resource.status) {
                case LOADING:
                    // Nút đã bị disable ở trên, không cần làm thêm
                    break;

                case SUCCESS:
                    Log.d(TAG, "Đăng xuất thành công, chuyển về Login");
                    sessionManager.clearSession(); // đảm bảo xóa session local
                    navigateToLogin();
                    break;

                case ERROR:
                    Log.e(TAG, "Đăng xuất lỗi: " + resource.message);
                    // Dù API lỗi vẫn xóa session local và về Login
                    showToast("Đăng xuất thất bại, đang đăng xuất khỏi thiết bị...");
                    sessionManager.clearSession();
                    navigateToLogin();
                    break;
            }
        });
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /** Lấy userId từ session rồi gọi API. */
    private void loadTeacherProfile() {
        String userIdStr = sessionManager.getUserId();
        Log.d(TAG, "userId từ session: " + userIdStr);

        if (userIdStr == null || userIdStr.isEmpty()) {
            Log.w(TAG, "Không tìm thấy userId trong session");
            showToast("Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại.");
            navigateToLogin();
            return;
        }

        try {
            Integer userId = Integer.parseInt(userIdStr);
            teacherViewModel.getTeacherbyId(userId);
        } catch (NumberFormatException e) {
            Log.e(TAG, "userId không phải số: " + userIdStr, e);
            showToast("ID không hợp lệ!");
        }
    }

    /** Chuyển về LoginActivity và xóa back stack. */
    private void navigateToLogin() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        // Xóa toàn bộ back stack, người dùng không thể bấm back về lại
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}