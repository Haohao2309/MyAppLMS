package com.example.myapplms.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.LMSApplication;
import com.example.myapplms.data.remote.dto.request.StudentRequest;
import com.example.myapplms.data.remote.dto.request.TeacherRequest;
import com.example.myapplms.data.repository.StudentRepository;
import com.example.myapplms.data.repository.TeacherRepository;
import com.example.myapplms.databinding.FragmentEditProfileBinding;
import com.example.myapplms.ui.base.BaseFragment;
import com.example.myapplms.ui.teacher.TeacherViewModel;

/**
 * EditProfileFragment — mở từ ProfileFragment qua FragmentManager.
 *
 * Nhận data qua Bundle args (buildArgs).
 * Khi save thành công → setFragmentResult → popBackStack về ProfileFragment.
 */
public class EditProfileFragment extends BaseFragment<FragmentEditProfileBinding> {

    // ── Bundle keys ───────────────────────────────────────────────────────────
    public static final String ARG_ROLE       = "arg_role";
    public static final String ARG_PROFILE_ID = "arg_profile_id";
    public static final String ARG_FIRST_NAME = "arg_first_name";
    public static final String ARG_LAST_NAME  = "arg_last_name";
    public static final String ARG_PHONE      = "arg_phone";
    public static final String ARG_LOCATION   = "arg_location";
    public static final String ARG_BIO        = "arg_bio";
    public static final String ARG_OCCUPATION = "arg_occupation";

    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_TEACHER = "TEACHER";

    private StudentViewModel studentViewModel;
    private TeacherViewModel teacherViewModel;

    private String role;
    private int    profileId;

    // ── Factory method ────────────────────────────────────────────────────────

    public static Bundle buildArgs(String role, int profileId,
                                   String firstName, String lastName,
                                   String phone, String location,
                                   String bio, String occupation) {
        Bundle b = new Bundle();
        b.putString(ARG_ROLE,       role);
        b.putInt   (ARG_PROFILE_ID, profileId);
        b.putString(ARG_FIRST_NAME, firstName);
        b.putString(ARG_LAST_NAME,  lastName);
        b.putString(ARG_PHONE,      phone);
        b.putString(ARG_LOCATION,   location);
        b.putString(ARG_BIO,        bio);
        b.putString(ARG_OCCUPATION, occupation);
        return b;
    }

    // ── Inflate ───────────────────────────────────────────────────────────────

    @NonNull
    @Override
    protected FragmentEditProfileBinding inflateBinding(@NonNull LayoutInflater inflater,
                                                        @Nullable ViewGroup container) {
        return FragmentEditProfileBinding.inflate(inflater, container, false);
    }

    // ── onCreate ─────────────────────────────────────────────────────────────

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            role      = args.getString(ARG_ROLE, ROLE_TEACHER);
            profileId = args.getInt(ARG_PROFILE_ID, -1);
        }

        LMSApplication app = (LMSApplication) requireActivity().getApplication();

        if (ROLE_STUDENT.equalsIgnoreCase(role)) {
            StudentRepository repo =
                    new StudentRepository(app.getRetrofitClient().getApiService());
            studentViewModel = new ViewModelProvider(this,
                    new StudentViewModel.Factory(repo)).get(StudentViewModel.class);
        } else {
            TeacherRepository repo =
                    new TeacherRepository(app.getRetrofitClient().getApiService());
            teacherViewModel = new ViewModelProvider(this,
                    new TeacherViewModel.Factory(repo)).get(TeacherViewModel.class);
        }
    }

    // ── onViewCreated ─────────────────────────────────────────────────────────

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fillFormFromArgs();
        setupOccupationLabel();
        observeUpdateResult();
    }

    @Override
    protected void setupViews() {}

    // ── setupListeners ────────────────────────────────────────────────────────

    @Override
    protected void setupListeners() {
        // Back → popBackStack (tương đương finish() của Activity)
        getBinding().btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        getBinding().btnSave.setOnClickListener(v -> attemptSave());

        getBinding().btnChoosePhoto.setOnClickListener(v ->
                showToast("Tính năng upload ảnh đang phát triển"));
    }

    // ── Fill form ─────────────────────────────────────────────────────────────

    private void fillFormFromArgs() {
        Bundle args = getArguments();
        if (args == null) return;

        getBinding().etFirstName.setText(safe(args.getString(ARG_FIRST_NAME)));
        getBinding().etLastName.setText(safe(args.getString(ARG_LAST_NAME)));
        getBinding().etPhone.setText(safe(args.getString(ARG_PHONE)));
        getBinding().etLocation.setText(safe(args.getString(ARG_LOCATION)));
        getBinding().etBio.setText(safe(args.getString(ARG_BIO)));
        getBinding().etOccupation.setText(safe(args.getString(ARG_OCCUPATION)));
    }

    private void setupOccupationLabel() {
        if (ROLE_STUDENT.equalsIgnoreCase(role)) {
            getBinding().tvOccupationLabel.setText("School / University");
            getBinding().etOccupation.setHint("HUCE, VNU...");
        } else {
            getBinding().tvOccupationLabel.setText("Degree / Title");
            getBinding().etOccupation.setHint("MSc Computer Science...");
        }
    }

    // ── Validate & save ───────────────────────────────────────────────────────

    private void attemptSave() {
        String firstName  = getBinding().etFirstName.getText().toString().trim();
        String lastName   = getBinding().etLastName.getText().toString().trim();
        String phone      = getBinding().etPhone.getText().toString().trim();
        String location   = getBinding().etLocation.getText().toString().trim();
        String bio        = getBinding().etBio.getText().toString().trim();
        String occupation = getBinding().etOccupation.getText().toString().trim();

        if (firstName.isEmpty()) {
            getBinding().etFirstName.setError("Không được để trống");
            getBinding().etFirstName.requestFocus();
            return;
        }
        if (profileId == -1) {
            showToast("Không tìm thấy thông tin profile");
            return;
        }

        setLoadingState(true);

        if (ROLE_STUDENT.equalsIgnoreCase(role)) {
            StudentRequest request = StudentRequest.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .phone(phone)
                    .location(location)
                    .bio(bio)
                    .school(occupation)
                    .build();
            studentViewModel.updateStudent(profileId, request);
        } else {
            TeacherRequest request = new TeacherRequest(
                    firstName, lastName, null, location, phone, bio, occupation);
            teacherViewModel.updateTeacher(profileId, request);
        }
    }

    // ── Observe update result ─────────────────────────────────────────────────

    private void observeUpdateResult() {
        if (ROLE_STUDENT.equalsIgnoreCase(role) && studentViewModel != null) {
            studentViewModel.updateResult.observe(getViewLifecycleOwner(), result -> {
                if (result == null) return;
                handleUpdateResult(result.status, result.message);
            });
        } else if (teacherViewModel != null) {
            teacherViewModel.updateResult.observe(getViewLifecycleOwner(), result -> {
                if (result == null) return;
                handleUpdateResult(result.status, result.message);
            });
        }
    }

    private void handleUpdateResult(Object status, String message) {
        switch (status.toString()) {
            case "LOADING":
                break;

            case "SUCCESS":
                setLoadingState(false);
                showToast("Cập nhật thành công!");

                // Gửi kết quả về ProfileFragment qua FragmentResultListener
                // (thay NavController.getSavedStateHandle)
                Bundle result = new Bundle();
                result.putBoolean(ProfileFragment.RESULT_BUNDLE_KEY, true);
                getParentFragmentManager().setFragmentResult(
                        ProfileFragment.RESULT_KEY_UPDATED, result);

                // Quay về ProfileFragment
                requireActivity().getSupportFragmentManager().popBackStack();
                break;

            case "ERROR":
                setLoadingState(false);
                showToast(message != null ? message : "Cập nhật thất bại");
                break;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setLoadingState(boolean loading) {
        getBinding().progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        getBinding().btnSave.setEnabled(!loading);
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }
}