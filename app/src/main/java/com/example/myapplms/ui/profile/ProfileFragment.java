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
import com.example.myapplms.model.Student;
import com.example.myapplms.data.repository.StudentRepository;
import com.example.myapplms.data.repository.TeacherRepository;
import com.example.myapplms.databinding.FragmentProfileBinding;
import com.example.myapplms.ui.auth.AuthViewModel;
import com.example.myapplms.ui.auth.AuthViewModelFactory;
import com.example.myapplms.ui.auth.LoginActivity;
import com.example.myapplms.ui.base.BaseFragment;
import com.example.myapplms.ui.teacher.TeacherViewModel;
import com.example.myapplms.ui.teacher.TeacherViewModelFactory;
import com.example.myapplms.utils.SessionManager;

/**
 * Bước 5 — ProfileFragment cải thiện:
 *   • Đọc role từ SessionManager
 *   • STUDENT  → gọi StudentViewModel
 *   • TEACHER  → gọi TeacherViewModel (giữ nguyên logic cũ)
 *   • Cả hai đều dùng cùng layout fragment_profile.xml
 */
public class ProfileFragment extends BaseFragment<FragmentProfileBinding> {

    private static final String TAG = "ProfileFragment";

    // Hằng số role — phải khớp với giá trị lưu trong SessionManager
    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_TEACHER = "TEACHER";

    private TeacherViewModel teacherViewModel;
    private StudentViewModel studentViewModel;
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
    // onCreate — khởi tạo ViewModel theo role
    // -------------------------------------------------------------------------

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LMSApplication app = (LMSApplication) requireActivity().getApplication();
        sessionManager = new SessionManager(requireContext());

        String role = sessionManager.getRole(); // Lấy role từ session

        // --- TeacherViewModel (khởi tạo luôn để tránh null-check ở dưới) ---
        TeacherRepository teacherRepository =
                new TeacherRepository(app.getRetrofitClient().getApiService());
        teacherViewModel = new ViewModelProvider(
                this, new TeacherViewModelFactory(teacherRepository))
                .get(TeacherViewModel.class);

        // --- StudentViewModel (chỉ khởi tạo khi cần) ---
        if (ROLE_STUDENT.equalsIgnoreCase(role)) {
            StudentRepository studentRepository =
                    new StudentRepository(app.getRetrofitClient().getApiService());
            studentViewModel = new ViewModelProvider(
                    this, new StudentViewModel.Factory(studentRepository))
                    .get(StudentViewModel.class);
        }

        // --- AuthViewModel (dùng chung cho cả hai role) ---
        AuthRepository authRepository = new AuthRepository(
                app.getRetrofitClient().getApiService(), sessionManager);
        authViewModel = new ViewModelProvider(
                this, new AuthViewModelFactory(authRepository))
                .get(AuthViewModel.class);
    }

    // -------------------------------------------------------------------------
    // onViewCreated
    // -------------------------------------------------------------------------

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        observeLogout();
        loadProfileByRole();
    }

    // -------------------------------------------------------------------------
    // setupViews / setupListeners
    // -------------------------------------------------------------------------

    @Override
    protected void setupViews() { /* Observers đã đăng ký trong onViewCreated */ }

    @Override
    protected void setupListeners() {

        getBinding().switchNotifications.setOnCheckedChangeListener((btn, isChecked) ->
                showToast(isChecked ? "Push Notifications Enabled" : "Push Notifications Disabled"));

        getBinding().btnLanguage.setOnClickListener(v ->
                showToast("Open Language Selection"));

        getBinding().btnLogout.setOnClickListener(v -> {
            getBinding().btnLogout.setEnabled(false);
            authViewModel.logout();
        });
    }

    // -------------------------------------------------------------------------
    // Điều hướng load theo role
    // -------------------------------------------------------------------------

    private void loadProfileByRole() {
        String userIdStr = sessionManager.getUserId();
        String role      = sessionManager.getRole();

        Log.d(TAG, "userId=" + userIdStr + "  role=" + role);

        if (userIdStr == null || userIdStr.isEmpty()) {
            showToast("Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại.");
            navigateToLogin();
            return;
        }

        try {
            Integer userId = Integer.parseInt(userIdStr);

            if (ROLE_STUDENT.equalsIgnoreCase(role)) {
                observeStudentProfile();
                studentViewModel.getStudentByUserId(userId);
            } else {
                // Mặc định TEACHER (giữ nguyên logic cũ)
                observeTeacherProfile();
                teacherViewModel.getTeacherbyId(userId);
            }

        } catch (NumberFormatException e) {
            Log.e(TAG, "userId không phải số: " + userIdStr, e);
            showToast("ID không hợp lệ!");
        }
    }

    // -------------------------------------------------------------------------
    // Observer — Student
    // -------------------------------------------------------------------------

    private void observeStudentProfile() {
        studentViewModel.student.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            Log.d(TAG, "Student | Status=" + resource.status
                    + " | Data=" + resource.data
                    + " | Msg=" + resource.message);

            switch (resource.status) {
                case LOADING:
                    showLoading(true);
                    break;

                case SUCCESS:
                    showLoading(false);
                    if (resource.data != null) {
                        bindStudentData(resource.data); // resource.data là Student (domain model)
                    } else {
                        Log.w(TAG, "SUCCESS nhưng data null");
                    }
                    break;

                case ERROR:
                    showLoading(false);
                    Log.e(TAG, "Lỗi tải student profile: " + resource.message);
                    showToast("Lỗi: " + resource.message);
                    break;
            }
        });
    }

    /**
     * Đổ dữ liệu Student (domain model) lên UI.
     * Dùng getter thay vì truy cập field trực tiếp — nhất quán với TeacherViewModel.
     */
    private void bindStudentData(Student data) {
        // getFullName() đã xử lý null và trim bên trong Student
        getBinding().tvName.setText(data.getFullName());
        getBinding().tvBio.setText(trim(data.getBio()));

        // tvRole → trường học (ngữ cảnh sinh viên)
        getBinding().tvRole.setText(trim(data.getSchool()));

        Log.d(TAG, "Hiển thị student: " + data.getFullName() + " | " + data.getSchool());
    }

    // -------------------------------------------------------------------------
    // Observer — Teacher (giữ nguyên logic cũ)
    // -------------------------------------------------------------------------

    private void observeTeacherProfile() {
        teacherViewModel.teacher.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            Log.d(TAG, "Teacher | Status=" + resource.status
                    + " | Data=" + resource.data
                    + " | Msg=" + resource.message);

            switch (resource.status) {
                case LOADING:
                    showLoading(true);
                    break;

                case SUCCESS:
                    showLoading(false);
                    if (resource.data != null) {
                        String fullName = trim(resource.data.getFirstName())
                                + " " + trim(resource.data.getLastName());
                        getBinding().tvName.setText(fullName.trim());
                        getBinding().tvBio.setText(trim(resource.data.getBio()));
                        getBinding().tvRole.setText(trim(resource.data.getDegree()));
                        Log.d(TAG, "Hiển thị teacher: " + fullName);
                    } else {
                        Log.w(TAG, "SUCCESS nhưng data null");
                    }
                    break;

                case ERROR:
                    showLoading(false);
                    Log.e(TAG, "Lỗi tải teacher profile: " + resource.message);
                    showToast("Lỗi: " + resource.message);
                    break;
            }
        });
    }

    // -------------------------------------------------------------------------
    // Observer — Logout (dùng chung)
    // -------------------------------------------------------------------------

    private void observeLogout() {
        authViewModel.logoutResult.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            Log.d(TAG, "Logout | Status=" + resource.status);

            switch (resource.status) {
                case LOADING:
                    break;

                case SUCCESS:
                    Log.d(TAG, "Đăng xuất thành công");
                    sessionManager.clearSession();
                    navigateToLogin();
                    break;

                case ERROR:
                    Log.e(TAG, "Đăng xuất lỗi: " + resource.message);
                    showToast("Đăng xuất thất bại, đang đăng xuất khỏi thiết bị...");
                    sessionManager.clearSession();
                    navigateToLogin();
                    break;
            }
        });
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Bật / tắt ProgressBar (nếu có trong layout).
     * Nếu layout chưa có progressBar thì bỏ comment khi thêm vào.
     */
    private void showLoading(boolean show) {
        // TODO: getBinding().progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private static String trim(@Nullable String s) {
        return s != null ? s.trim() : "";
    }

    /** Chuyển về LoginActivity và xóa back stack. */
    private void navigateToLogin() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}