package com.example.myapplms.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.myapplms.LMSApplication;
import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.request.StudentRequest;
import com.example.myapplms.data.remote.dto.request.TeacherRequest;
import com.example.myapplms.data.repository.MediaRepository;
import com.example.myapplms.data.repository.StudentRepository;
import com.example.myapplms.data.repository.TeacherRepository;
import com.example.myapplms.databinding.FragmentEditProfileBinding;
import com.example.myapplms.ui.base.BaseFragment;
import com.example.myapplms.ui.teacher.TeacherViewModel;
import com.example.myapplms.utils.ImageUtils;
import com.example.myapplms.utils.SessionManager;

import java.io.File;

public class EditProfileFragment extends BaseFragment<FragmentEditProfileBinding> {

    // ── Bundle keys ───────────────────────────────────────────
    public static final String ARG_ROLE        = "arg_role";
    public static final String ARG_PROFILE_ID  = "arg_profile_id";
    public static final String ARG_FIRST_NAME  = "arg_first_name";
    public static final String ARG_LAST_NAME   = "arg_last_name";
    public static final String ARG_PHONE       = "arg_phone";
    public static final String ARG_LOCATION    = "arg_location";
    public static final String ARG_BIO         = "arg_bio";
    public static final String ARG_OCCUPATION  = "arg_occupation";
    public static final String ARG_IMAGE_URL   = "arg_image_url";  // ← thêm mới

    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_TEACHER = "TEACHER";

    private StudentViewModel studentViewModel;
    private TeacherViewModel teacherViewModel;
    private AvatarViewModel  avatarViewModel;

    private SessionManager sessionManager;
    private String role;
    private int    profileId;

    // ── Factory method ────────────────────────────────────────
    public static Bundle buildArgs(String role, int profileId,
                                   String firstName, String lastName,
                                   String phone, String location,
                                   String bio, String occupation,
                                   String imageUrl) {
        Bundle b = new Bundle();
        b.putString(ARG_ROLE,       role);
        b.putInt   (ARG_PROFILE_ID, profileId);
        b.putString(ARG_FIRST_NAME, firstName);
        b.putString(ARG_LAST_NAME,  lastName);
        b.putString(ARG_PHONE,      phone);
        b.putString(ARG_LOCATION,   location);
        b.putString(ARG_BIO,        bio);
        b.putString(ARG_OCCUPATION, occupation);
        b.putString(ARG_IMAGE_URL,  imageUrl);   // ← thêm mới
        return b;
    }

    // ── Launcher chọn ảnh từ gallery ─────────────────────────
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {
                            Uri imageUri = result.getData().getData();
                            if (imageUri != null) {
                                handleImageSelected(imageUri);
                            }
                        }
                    });

    // ── Inflate ───────────────────────────────────────────────
    @NonNull
    @Override
    protected FragmentEditProfileBinding inflateBinding(@NonNull LayoutInflater inflater,
                                                        @Nullable ViewGroup container) {
        return FragmentEditProfileBinding.inflate(inflater, container, false);
    }

    // ── onCreate ─────────────────────────────────────────────
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            role      = args.getString(ARG_ROLE, ROLE_TEACHER);
            profileId = args.getInt(ARG_PROFILE_ID, -1);
        }

        LMSApplication app = (LMSApplication) requireActivity().getApplication();
        sessionManager = new SessionManager(requireContext());

        // Profile ViewModel theo role
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

        // AvatarViewModel — dùng chung cho cả 2 role
        MediaRepository avatarRepo = new MediaRepository(
                app.getRetrofitClient().getApiService(), sessionManager);
        avatarViewModel = new ViewModelProvider(this,
                new AvatarViewModel.Factory(avatarRepo)).get(AvatarViewModel.class);
    }

    // ── onViewCreated ─────────────────────────────────────────
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fillFormFromArgs();
        setupOccupationLabel();
        observeUpdateResult();
        observeUploadResult();
    }

    @Override
    protected void setupViews() {}

    // ── setupListeners ────────────────────────────────────────
    @Override
    protected void setupListeners() {
        getBinding().btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        getBinding().btnSave.setOnClickListener(v -> attemptSave());

        // Chọn ảnh từ gallery
        getBinding().btnChoosePhoto.setOnClickListener(v -> openGallery());
    }

    // ── Mở gallery ────────────────────────────────────────────
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    // ── Xử lý ảnh được chọn ──────────────────────────────────
    private void handleImageSelected(Uri imageUri) {
        // Hiển thị preview ngay lập tức (trước khi upload xong)
        loadAvatarFromUri(imageUri);

        // Chuyển Uri → File rồi upload
        File imageFile = ImageUtils.uriToFile(requireContext(), imageUri);
        if (imageFile != null) {
            setAvatarLoading(true);
            avatarViewModel.uploadAvatar(imageFile);
        } else {
            showToast("Không thể đọc file ảnh");
        }
    }

    // ── Fill form ─────────────────────────────────────────────
    private void fillFormFromArgs() {
        Bundle args = getArguments();
        if (args == null) return;

        getBinding().etFirstName.setText(safe(args.getString(ARG_FIRST_NAME)));
        getBinding().etLastName.setText(safe(args.getString(ARG_LAST_NAME)));
        getBinding().etPhone.setText(safe(args.getString(ARG_PHONE)));
        getBinding().etLocation.setText(safe(args.getString(ARG_LOCATION)));
        getBinding().etBio.setText(safe(args.getString(ARG_BIO)));
        getBinding().etOccupation.setText(safe(args.getString(ARG_OCCUPATION)));

        // Hiển thị avatar hiện tại
        String imageUrl = args.getString(ARG_IMAGE_URL);
        if (imageUrl == null || imageUrl.isEmpty()) {
            // Thử lấy từ session nếu args không có
            imageUrl = sessionManager.getImageUrl();
        }
        loadAvatarFromUrl(imageUrl);
    }

    // ── Hiển thị avatar từ URL (Glide) ────────────────────────
    private void loadAvatarFromUrl(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(getBinding().ivAvatar);
        } else {
            getBinding().ivAvatar.setImageResource(R.drawable.ic_profile);
        }
    }

    // ── Hiển thị avatar từ Uri (preview local) ────────────────
    private void loadAvatarFromUri(Uri uri) {
        Glide.with(this)
                .load(uri)
                .transform(new CircleCrop())
                .placeholder(R.drawable.ic_profile)
                .into(getBinding().ivAvatar);
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

    // ── Validate và save thông tin ────────────────────────────
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
                    .phone(phone.isEmpty() ? null : phone)
                    .location(location.isEmpty() ? null : location)
                    .bio(bio.isEmpty() ? null : bio)
                    .school(occupation.isEmpty() ? null : occupation)
                    .build();
            studentViewModel.updateStudent(profileId, request);
        } else {
            TeacherRequest request = new TeacherRequest(
                    firstName, lastName, null,
                    location.isEmpty() ? null : location,
                    phone.isEmpty() ? null : phone,
                    bio.isEmpty() ? null : bio,
                    occupation.isEmpty() ? null : occupation);
            teacherViewModel.updateTeacher(profileId, request);
        }
    }

    // ── Observe kết quả upload avatar ────────────────────────
    private void observeUploadResult() {
        avatarViewModel.uploadResult.observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    setAvatarLoading(true);
                    break;
                case SUCCESS:
                    setAvatarLoading(false);
                    showToast("Cập nhật ảnh đại diện thành công!");
                    // imageUrl đã được lưu vào SessionManager trong Repository
                    break;
                case ERROR:
                    setAvatarLoading(false);
                    showToast("Upload ảnh thất bại: " + result.message);
                    break;
            }
        });
    }

    // ── Observe kết quả update profile ───────────────────────
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
                Bundle result = new Bundle();
                result.putBoolean(ProfileFragment.RESULT_BUNDLE_KEY, true);
                getParentFragmentManager().setFragmentResult(
                        ProfileFragment.RESULT_KEY_UPDATED, result);
                requireActivity().getSupportFragmentManager().popBackStack();
                break;
            case "ERROR":
                setLoadingState(false);
                showToast(message != null ? message : "Cập nhật thất bại");
                break;
        }
    }

    // ── Helpers ───────────────────────────────────────────────
    private void setLoadingState(boolean loading) {
        getBinding().progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        getBinding().btnSave.setEnabled(!loading);
    }

    private void setAvatarLoading(boolean loading) {
        getBinding().btnChoosePhoto.setEnabled(!loading);
        if (loading) {
            getBinding().btnChoosePhoto.setText("Đang upload...");
        } else {
            getBinding().btnChoosePhoto.setText("Choose New Photo");
        }
    }

    private static String safe(String s) { return s != null ? s : ""; }
}