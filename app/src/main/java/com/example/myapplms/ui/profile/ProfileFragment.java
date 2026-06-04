package com.example.myapplms.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

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

/**
 * ProfileFragment — hiển thị thông tin cá nhân cho cả STUDENT và TEACHER.
 *
 * Điều hướng sang EditProfileFragment qua FragmentManager (không dùng NavController
 * vì app dùng replaceFragment thủ công, không có NavHost).
 *
 * Nhận kết quả reload từ EditProfileFragment qua FragmentResultListener:
 *   setFragmentResult(RESULT_KEY_UPDATED, bundle) → loadProfileByRole()
 */
public class ProfileFragment extends BaseFragment<FragmentProfileBinding> {

    private static final String TAG          = "ProfileFragment";
    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_TEACHER = "TEACHER";

    // Key nhận kết quả từ EditProfileFragment qua FragmentResultListener
    public static final String RESULT_KEY_UPDATED = "edit_profile_updated";
    public static final String RESULT_BUNDLE_KEY  = "updated";

    private TeacherViewModel teacherViewModel;
    private StudentViewModel studentViewModel;
    private AuthViewModel    authViewModel;
    private SessionManager   sessionManager;

    // Lưu data hiện tại để truyền sang EditProfileFragment
    private Student currentStudent;
    private Teacher currentTeacher;

    // ── Inflate ───────────────────────────────────────────────────────────────

    @NonNull
    @Override
    protected FragmentProfileBinding inflateBinding(@NonNull LayoutInflater inflater,
                                                    @Nullable ViewGroup container) {
        return FragmentProfileBinding.inflate(inflater, container, false);
    }

    // ── onCreate — khởi tạo ViewModel theo role ───────────────────────────────

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LMSApplication app = (LMSApplication) requireActivity().getApplication();
        sessionManager = new SessionManager(requireContext());
        String role = sessionManager.getRole();

        // TeacherViewModel
        TeacherRepository teacherRepo =
                new TeacherRepository(app.getRetrofitClient().getApiService());
        teacherViewModel = new ViewModelProvider(this,
                new TeacherViewModel.Factory(teacherRepo)).get(TeacherViewModel.class);

        // StudentViewModel — chỉ khởi tạo khi role = STUDENT
        if (ROLE_STUDENT.equalsIgnoreCase(role)) {
            StudentRepository studentRepo =
                    new StudentRepository(app.getRetrofitClient().getApiService());
            studentViewModel = new ViewModelProvider(this,
                    new StudentViewModel.Factory(studentRepo)).get(StudentViewModel.class);
        }

        // AuthViewModel
        AuthRepository authRepo = new AuthRepository(
                app.getRetrofitClient().getApiService(), sessionManager);
        authViewModel = new ViewModelProvider(this,
                new AuthViewModelFactory(authRepo)).get(AuthViewModel.class);
    }

    // ── onViewCreated ─────────────────────────────────────────────────────────

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        observeLogout();
        observeEditResult();   // Lắng nghe kết quả từ EditProfileFragment
        loadProfileByRole();
    }

    // ── setupViews / setupListeners ───────────────────────────────────────────

    @Override
    protected void setupViews() {}

    @Override
    protected void setupListeners() {
        getBinding().switchNotifications.setOnCheckedChangeListener((btn, isChecked) ->
                showToast(isChecked ? "Push Notifications Enabled" : "Push Notifications Disabled"));

        getBinding().btnLanguage.setOnClickListener(v ->
                showToast("Open Language Selection"));

        // Nút Edit → mở EditProfileFragment qua FragmentManager
        getBinding().btnEdit.setOnClickListener(v -> openEditProfile());

        getBinding().btnLogout.setOnClickListener(v -> {
            getBinding().btnLogout.setEnabled(false);
            authViewModel.logout();
        });
    }

    // ── Mở EditProfileFragment ────────────────────────────────────────────────

    /**
     * Dùng FragmentManager.beginTransaction() thay NavController.
     * addToBackStack() để bấm Back tự quay về ProfileFragment.
     */
    private void openEditProfile() {
        String role = sessionManager.getRole();
        Bundle args;

        if (ROLE_STUDENT.equalsIgnoreCase(role)) {
            if (currentStudent == null) {
                showToast("Đang tải thông tin, vui lòng thử lại");
                return;
            }
            args = EditProfileFragment.buildArgs(
                    role,
                    currentStudent.getStudentId(),
                    currentStudent.getFirstName(),
                    currentStudent.getLastName(),
                    currentStudent.getPhone(),
                    currentStudent.getLocation(),
                    currentStudent.getBio(),
                    currentStudent.getSchool()
            );
        } else {
            if (currentTeacher == null) {
                showToast("Đang tải thông tin, vui lòng thử lại");
                return;
            }
            args = EditProfileFragment.buildArgs(
                    role,
                    currentTeacher.getTeacherId(),
                    currentTeacher.getFirstName(),
                    currentTeacher.getLastName(),
                    currentTeacher.getPhone(),
                    currentTeacher.getLocation(),
                    currentTeacher.getBio(),
                    currentTeacher.getDegree()
            );
        }

        EditProfileFragment editFragment = new EditProfileFragment();
        editFragment.setArguments(args);

        // Dùng đúng container id mà MainActivity dùng để replaceFragment
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragment_container, editFragment)
                .addToBackStack("edit_profile")   // Back tự pop về ProfileFragment
                .commit();
    }

    // ── Nhận kết quả từ EditProfileFragment ──────────────────────────────────

    /**
     * EditProfileFragment gọi:
     *   getParentFragmentManager().setFragmentResult(RESULT_KEY_UPDATED, bundle)
     * ProfileFragment lắng nghe ở đây → reload profile.
     *
     * setFragmentResultListener phải gọi trong onCreate() hoặc onViewCreated()
     * trước khi EditProfileFragment được tạo.
     */
    private void observeEditResult() {
        getParentFragmentManager().setFragmentResultListener(
                RESULT_KEY_UPDATED,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    if (result.getBoolean(RESULT_BUNDLE_KEY, false)) {
                        Log.d(TAG, "EditProfile saved → reload profile");
                        loadProfileByRole();
                    }
                }
        );
    }

    // ── Load profile theo role ────────────────────────────────────────────────

    private void loadProfileByRole() {

        String role      = sessionManager.getRole();


        if (role == null) {
            showToast("Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại.");
            navigateToLogin();
            return;
        }

        try {
            if (ROLE_STUDENT.equalsIgnoreCase(role)) {
                int userId = sessionManager.getStudentId();
                observeStudentProfile();
                studentViewModel.getStudentByUserId(userId);
            } else {
                int userId = sessionManager.getTeacherId();
                observeTeacherProfile();
                teacherViewModel.getTeacherbyId(userId);
            }

        } catch (NumberFormatException e) {
            showToast("ID không hợp lệ!");
        }
    }

    // ── Observer Student ──────────────────────────────────────────────────────

    private void observeStudentProfile() {
        studentViewModel.student.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            Log.d(TAG, "Student | Status=" + resource.status);

            switch (resource.status) {
                case LOADING:
                    showLoading(true);
                    break;
                case SUCCESS:
                    showLoading(false);
                    if (resource.data != null) {
                        currentStudent = resource.data;
                        bindStudentData(resource.data);
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

    private void bindStudentData(Student data) {
        getBinding().tvName.setText(data.getFullName());
        getBinding().tvBio.setText(safe(data.getBio()));
        getBinding().tvRole.setText(safe(data.getSchool()));
        Log.d(TAG, "Hiển thị student: " + data.getFullName());
    }

    // ── Observer Teacher ──────────────────────────────────────────────────────

    private void observeTeacherProfile() {
        teacherViewModel.teacher.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            Log.d(TAG, "Teacher | Status=" + resource.status);

            switch (resource.status) {
                case LOADING:
                    showLoading(true);
                    break;
                case SUCCESS:
                    showLoading(false);
                    if (resource.data != null) {
                        currentTeacher = resource.data;
                        bindTeacherData(resource.data);
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

    private void bindTeacherData(Teacher data) {
        getBinding().tvName.setText(data.getFullName());
        getBinding().tvBio.setText(safe(data.getBio()));
        getBinding().tvRole.setText(safe(data.getDegree()));
        Log.d(TAG, "Hiển thị teacher: " + data.getFullName());
    }

    // ── Observer Logout ───────────────────────────────────────────────────────

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

    // ── Helpers ───────────────────────────────────────────────────────────────

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