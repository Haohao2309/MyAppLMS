package com.example.myapplms.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.myapplms.LMSApplication;
import com.example.myapplms.R;
import com.example.myapplms.data.repository.AuthRepository;
import com.example.myapplms.data.repository.StudentRepository;
import com.example.myapplms.data.repository.TeacherRepository;
import com.example.myapplms.databinding.FragmentProfileBinding;
import com.example.myapplms.model.Student;
import com.example.myapplms.model.Teacher;
import com.example.myapplms.ui.auth.AuthViewModel;
import com.example.myapplms.ui.auth.AuthViewModelFactory;
import com.example.myapplms.ui.auth.LoginActivity;
import com.example.myapplms.ui.base.BaseFragment;
import com.example.myapplms.ui.teacher.TeacherViewModel;
import com.example.myapplms.utils.SessionManager;

public class ProfileFragment extends BaseFragment<FragmentProfileBinding> {

    private static final String TAG          = "ProfileFragment";
    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_TEACHER = "TEACHER";

    public static final String RESULT_KEY_UPDATED = "edit_profile_updated";
    public static final String RESULT_BUNDLE_KEY  = "updated";

    private TeacherViewModel teacherViewModel;
    private StudentViewModel studentViewModel;
    private AuthViewModel    authViewModel;
    private SessionManager   sessionManager;

    private Student currentStudent;
    private Teacher currentTeacher;

    @NonNull
    @Override
    protected FragmentProfileBinding inflateBinding(@NonNull LayoutInflater inflater,
                                                    @Nullable ViewGroup container) {
        return FragmentProfileBinding.inflate(inflater, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LMSApplication app = (LMSApplication) requireActivity().getApplication();
        sessionManager = new SessionManager(requireContext());
        String role = sessionManager.getRole();

        if (ROLE_STUDENT.equalsIgnoreCase(role)) {
            StudentRepository studentRepo =
                    new StudentRepository(app.getRetrofitClient().getApiService());
            studentViewModel = new ViewModelProvider(this,
                    new StudentViewModel.Factory(studentRepo)).get(StudentViewModel.class);
        } else{
            TeacherRepository teacherRepo =
                    new TeacherRepository(app.getRetrofitClient().getApiService());
            teacherViewModel = new ViewModelProvider(this,
                    new TeacherViewModel.Factory(teacherRepo)).get(TeacherViewModel.class);
        }


        AuthRepository authRepo = new AuthRepository(
                app.getRetrofitClient().getApiService(), sessionManager);
        authViewModel = new ViewModelProvider(this,
                new AuthViewModelFactory(authRepo)).get(AuthViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hiển thị avatar đã lưu trong session (tránh nhìn trống khi đang load)
        loadAvatarFromSession();

        observeLogout();
        observeEditResult();
        loadProfileByRole();
    }

    @Override
    protected void setupViews() {}

    @Override
    protected void setupListeners() {
        getBinding().switchNotifications.setOnCheckedChangeListener((btn, isChecked) ->
                showToast(isChecked ? "Push Notifications Enabled" : "Push Notifications Disabled"));

        getBinding().btnLanguage.setOnClickListener(v ->
                showToast("Open Language Selection"));

        getBinding().btnEdit.setOnClickListener(v -> openEditProfile());

        getBinding().btnLogout.setOnClickListener(v -> {
            getBinding().btnLogout.setEnabled(false);
            authViewModel.logout();
        });
    }

    // ── Hiển thị avatar từ URL ────────────────────────────────
    private void loadAvatarFromUrl(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(getBinding().ivAvatar);
        } else {
            getBinding().ivAvatar.setImageResource(R.drawable.ic_profile);
        }
    }

    // Hiển thị ảnh từ session (cached URL) khi mới vào trang
    private void loadAvatarFromSession() {
        String imageUrl = sessionManager.getImageUrl();
        loadAvatarFromUrl(imageUrl);
    }

    // ── Mở EditProfileFragment ────────────────────────────────
    private void openEditProfile() {
        String role = sessionManager.getRole();
        Bundle args;

        if (ROLE_STUDENT.equalsIgnoreCase(role)) {
            if (currentStudent == null) { showToast("Đang tải thông tin..."); return; }
            args = EditProfileFragment.buildArgs(
                    role,
                    currentStudent.getStudentId(),
                    currentStudent.getFirstName(),
                    currentStudent.getLastName(),
                    currentStudent.getPhone(),
                    currentStudent.getLocation(),
                    currentStudent.getBio(),
                    currentStudent.getSchool(),
                    sessionManager.getImageUrl()  // ← truyền imageUrl
            );
        } else {
            if (currentTeacher == null) { showToast("Đang tải thông tin..."); return; }
            args = EditProfileFragment.buildArgs(
                    role,
                    currentTeacher.getTeacherId(),
                    currentTeacher.getFirstName(),
                    currentTeacher.getLastName(),
                    currentTeacher.getPhone(),
                    currentTeacher.getLocation(),
                    currentTeacher.getBio(),
                    currentTeacher.getDegree(),
                    sessionManager.getImageUrl()  // ← truyền imageUrl
            );
        }

        EditProfileFragment editFragment = new EditProfileFragment();
        editFragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragment_container, editFragment)
                .addToBackStack("edit_profile")
                .commit();
    }

    // ── Nhận kết quả từ EditProfileFragment ──────────────────
    private void observeEditResult() {
        getParentFragmentManager().setFragmentResultListener(
                RESULT_KEY_UPDATED,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    if (result.getBoolean(RESULT_BUNDLE_KEY, false)) {
                        loadProfileByRole();
                        // Reload avatar sau khi edit xong
                        loadAvatarFromSession();
                    }
                }
        );
    }

    // ── Load profile theo role ────────────────────────────────
    private void loadProfileByRole() {
        String role = sessionManager.getRole();
        if (role == null) { navigateToLogin(); return; }

        if (ROLE_STUDENT.equalsIgnoreCase(role)) {
            Integer studentId = sessionManager.getStudentId();
            if (studentId != null) {
                observeStudentProfile();
                studentViewModel.getStudentByUserId(studentId);
            }
        } else {
            Integer teacherId = sessionManager.getTeacherId();
            if (teacherId != null) {
                observeTeacherProfile();
                teacherViewModel.getTeacherbyId(teacherId);
            }
        }
    }

    // ── Observer Student ──────────────────────────────────────
    private void observeStudentProfile() {
        studentViewModel.student.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING: showLoading(true); break;
                case SUCCESS:
                    showLoading(false);
                    if (resource.data != null) {
                        currentStudent = resource.data;
                        bindStudentData(resource.data);
                    }
                    break;
                case ERROR:
                    showLoading(false);
                    showToast("Lỗi: " + resource.message);
                    break;
            }
        });
    }

    private void bindStudentData(Student data) {
        getBinding().tvName.setText(data.getFullName());
        getBinding().tvBio.setText(safe(data.getBio()));
        getBinding().tvRole.setText(safe(data.getSchool()));
        // Avatar lấy từ sessionManager (đã lưu khi login hoặc sau upload)
        loadAvatarFromSession();
    }

    // ── Observer Teacher ──────────────────────────────────────
    private void observeTeacherProfile() {
        teacherViewModel.teacher.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING: showLoading(true); break;
                case SUCCESS:
                    showLoading(false);
                    if (resource.data != null) {
                        currentTeacher = resource.data;
                        bindTeacherData(resource.data);
                    }
                    break;
                case ERROR:
                    showLoading(false);
                    showToast("Lỗi: " + resource.message);
                    break;
            }
        });
    }

    private void bindTeacherData(Teacher data) {
        getBinding().tvName.setText(data.getFullName());
        getBinding().tvBio.setText(safe(data.getBio()));
        getBinding().tvRole.setText(safe(data.getDegree()));
        loadAvatarFromSession();
    }

    // ── Observer Logout ───────────────────────────────────────
    private void observeLogout() {
        authViewModel.logoutResult.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    sessionManager.clearSession();
                    navigateToLogin();
                    break;
                case ERROR:
                    showToast("Đang đăng xuất...");
                    sessionManager.clearSession();
                    navigateToLogin();
                    break;
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────
    private void showLoading(boolean show) {
        getBinding().progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private static String safe(@Nullable String s) {
        return s != null ? s.trim() : "";
    }

    private void navigateToLogin() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}