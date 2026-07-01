package com.example.myapplms.ui.teacher;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.myapplms.LMSApplication;
import com.example.myapplms.data.repository.CourseRepository;
import com.example.myapplms.data.repository.MediaRepository;
import com.example.myapplms.databinding.ActivityCourseFormBinding;
import com.example.myapplms.ui.explore.TeacherCourseViewModel;
import com.example.myapplms.ui.explore.TeacherCourseViewModelFactory;
import com.example.myapplms.utils.SessionManager;
import com.example.myapplms.utils.UriToFileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.example.myapplms.data.remote.dto.response.CategoryResponse;

public class CourseFormActivity extends AppCompatActivity {

    public static final String EXTRA_COURSE_ID          = "extra_course_id";
    public static final String EXTRA_COURSE_TITLE       = "extra_course_title";
    public static final String EXTRA_COURSE_DESCRIPTION = "extra_course_description";
    public static final String EXTRA_COURSE_PRICE       = "extra_course_price";
    public static final String EXTRA_COURSE_IMAGE_URL   = "extra_course_image_url";
    public static final String EXTRA_COURSE_CATEGORY    = "extra_course_category";

    private ActivityCourseFormBinding binding;
    private TeacherCourseViewModel viewModel;

    // -1 = chế độ tạo mới, > 0 = chế độ sửa
    private int courseId  = -1;
    private int teacherId = -1;
    private int categoryId = 1;
    private String selectedLevel = "Beginner";
    
    private List<CategoryResponse> loadedCategories = new ArrayList<>();

    private String uploadedImageUrl = "";

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                // Hiện overlay uploading
                showUploadingState(true);
                // Convert URI → File rồi upload
                File imageFile = UriToFileUtil.from(this, uri);
                if (imageFile != null) {
                    viewModel.uploadCourseImage(imageFile);
                } else {
                    showUploadingState(false);
                    Toast.makeText(this, "Không đọc được file ảnh", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCourseFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lấy teacherId từ session
        teacherId = getTeacherId();

        // Lấy courseId từ Intent — -1 nghĩa là tạo mới
        courseId = getIntent().getIntExtra(EXTRA_COURSE_ID, -1);

        setupViewModel();
        setupSpinners();
        setupLevelButtons();
        setupFormMode();
        setupListeners();
        observeResults();
        observeUpload();
        
        viewModel.loadCategories();
    }

    private int getTeacherId() {
        SessionManager session = new SessionManager(this);
        Integer id = session.getTeacherId();
        return id != null ? id : -1;
    }

    private void setupViewModel() {
        LMSApplication app = (LMSApplication) getApplication();
        CourseRepository courseRepo = new CourseRepository(app.getRetrofitClient().getApiService());

        // ── Thêm MediaRepository ───────────────────────────────
        MediaRepository mediaRepo = new MediaRepository(
                app.getRetrofitClient().getApiService(),
                new SessionManager(this)
        );

        viewModel = new ViewModelProvider(this,
                new TeacherCourseViewModelFactory(courseRepo, mediaRepo))
                .get(TeacherCourseViewModel.class);
    }

    private void setupSpinners() {
        // Cập nhật categoryId khi chọn
        binding.spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (loadedCategories != null && position < loadedCategories.size()) {
                    categoryId = loadedCategories.get(position).getId();
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Language
        String[] languages = {"English", "Vietnamese", "French", "Spanish"};
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, languages);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerLanguage.setAdapter(languageAdapter);
    }

    private void setupLevelButtons() {
        // Mặc định Beginner được chọn
        selectLevel("Beginner");

        binding.btnBeginner.setOnClickListener(v -> selectLevel("Beginner"));
        binding.btnIntermediate.setOnClickListener(v -> selectLevel("Intermediate"));
        binding.btnAdvanced.setOnClickListener(v -> selectLevel("Advanced"));
    }

    private void selectLevel(String level) {
        selectedLevel = level;

        // Reset tất cả về unselected
        binding.btnBeginner.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFFF0EFFF));
        binding.btnBeginner.setTextColor(0xFF6C63FF);
        binding.btnIntermediate.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFFF0EFFF));
        binding.btnIntermediate.setTextColor(0xFF6C63FF);
        binding.btnAdvanced.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFFF0EFFF));
        binding.btnAdvanced.setTextColor(0xFF6C63FF);

        // Highlight level được chọn
        switch (level) {
            case "Beginner":
                binding.btnBeginner.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(0xFF6C63FF));
                binding.btnBeginner.setTextColor(0xFFFFFFFF);
                break;
            case "Intermediate":
                binding.btnIntermediate.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(0xFF6C63FF));
                binding.btnIntermediate.setTextColor(0xFFFFFFFF);
                break;
            case "Advanced":
                binding.btnAdvanced.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(0xFF6C63FF));
                binding.btnAdvanced.setTextColor(0xFFFFFFFF);
                break;
        }
    }

    private void setupFormMode() {
        if (isEditMode()) {
            // ── Chế độ SỬA ───────────────────────────────────
            binding.tvFormTitle.setText("Edit Course");
            binding.tvFormSubtitle.setText("Update course details");
            binding.btnSubmit.setText("Save Changes");

            // Điền dữ liệu cũ vào form từ Intent
            String title = getIntent().getStringExtra(EXTRA_COURSE_TITLE);
            String description = getIntent().getStringExtra(EXTRA_COURSE_DESCRIPTION);
            String imageUrl = getIntent().getStringExtra(EXTRA_COURSE_IMAGE_URL);
            String priceText = getIntent().getStringExtra(EXTRA_COURSE_PRICE);
            String category = getIntent().getStringExtra(EXTRA_COURSE_CATEGORY);

            if (title != null) binding.etTitle.setText(title);
            if (description != null) binding.etDescription.setText(description);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                uploadedImageUrl = imageUrl;
                Glide.with(this).load(imageUrl).into(binding.ivThumbnailPreview);
                binding.ivThumbnailPreview.setVisibility(View.VISIBLE);
                binding.layoutUploadPlaceholder.setVisibility(View.GONE);
                binding.btnChangeThumbnail.setVisibility(View.VISIBLE);
            }
            if (priceText != null && !priceText.equals("FREE")) {
                binding.etPrice.setText(priceText.replace("$", "").trim());
            }
            // Category sẽ được chọn trong observeResults() sau khi call API thành công
        } else {
            // ── Chế độ TẠO MỚI ───────────────────────────────
            binding.tvFormTitle.setText("Create New Course");
            binding.tvFormSubtitle.setText("Fill in the course details");
            binding.btnSubmit.setText("Create Course");
        }
    }

    // Sửa lại:
    private boolean isEditMode() {
        return courseId != -1;  // ← tự động detect dựa vào courseId
    }
    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCancel.setOnClickListener(v -> finish());
        binding.btnSubmit.setOnClickListener(v -> attemptSubmit());
        binding.layoutUploadPlaceholder.setOnClickListener(v ->
                pickImageLauncher.launch("image/*"));
        binding.btnChangeThumbnail.setOnClickListener(v ->
                pickImageLauncher.launch("image/*"));
    }

    private void attemptSubmit() {
        String title       = binding.etTitle.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String priceStr    = binding.etPrice.getText().toString().trim();

        if (title.isEmpty() || title.length() < 5 || title.length() > 100) {
            binding.etTitle.setError("Tiêu đề phải từ 5 đến 100 ký tự");
            binding.etTitle.requestFocus(); return;
        }
        if (description.isEmpty() || description.length() < 20) {
            binding.etDescription.setError("Mô tả khóa học tối thiểu 20 ký tự");
            binding.etDescription.requestFocus(); return;
        }
        if (categoryId == -1) {
            Toast.makeText(this, "Vui lòng đợi tải và chọn danh mục", Toast.LENGTH_SHORT).show();
            return;
        }
        if (uploadedImageUrl.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ảnh thumbnail", Toast.LENGTH_SHORT).show();
            return;
        }
        if (teacherId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin giảng viên", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = 0.0;
        if (!priceStr.isEmpty()) {
            try { 
                price = Double.parseDouble(priceStr); 
                if (price < 0) {
                    binding.etPrice.setError("Giá không được nhỏ hơn 0"); return;
                }
            }
            catch (NumberFormatException e) {
                binding.etPrice.setError("Giá không hợp lệ"); return;
            }
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSubmit.setEnabled(false);

        // ── Dùng uploadedImageUrl thay vì etImageUrl ──────────
        if (isEditMode()) {
            viewModel.updateCourse(courseId, teacherId, categoryId,
                    title, description, uploadedImageUrl, price);
        } else {
            viewModel.createCourse(teacherId, categoryId,
                    title, description, uploadedImageUrl, price);
        }
    }

    private void observeUpload() {
        viewModel.uploadResult.observe(this, result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    showUploadingState(true);
                    break;
                case SUCCESS:
                    showUploadingState(false);
                    uploadedImageUrl = result.data != null ? result.data : "";
                    // Hiển thị preview
                    Glide.with(this).load(uploadedImageUrl).into(binding.ivThumbnailPreview);
                    binding.ivThumbnailPreview.setVisibility(View.VISIBLE);
                    binding.layoutUploadPlaceholder.setVisibility(View.GONE);
                    binding.btnChangeThumbnail.setVisibility(View.VISIBLE);
                    break;
                case ERROR:
                    showUploadingState(false);
                    Toast.makeText(this, "Upload ảnh thất bại: " + result.message,
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void observeResults() {
        viewModel.createResult.observe(this, result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Tạo khóa học thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSubmit.setEnabled(true);
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });

        viewModel.updateResult.observe(this, result -> {
            if (result == null) return;
            switch (result.status) {
                case LOADING:
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSubmit.setEnabled(true);
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });

        viewModel.categories.observe(this, result -> {
            if (result == null) return;
            switch (result.status) {
                case SUCCESS:
                    if (result.data != null) {
                        loadedCategories.clear();
                        loadedCategories.addAll(result.data);

                        List<String> categoryNames = new ArrayList<>();
                        for (CategoryResponse cat : loadedCategories) {
                            categoryNames.add(cat.getName());
                        }

                        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, categoryNames);
                        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerCategory.setAdapter(categoryAdapter);

                        // Chọn category nếu đang ở chế độ sửa
                        if (isEditMode()) {
                            String category = getIntent().getStringExtra(EXTRA_COURSE_CATEGORY);
                            if (category != null) {
                                for (int i = 0; i < loadedCategories.size(); i++) {
                                    if (loadedCategories.get(i).getName().equalsIgnoreCase(category)) {
                                        binding.spinnerCategory.setSelection(i);
                                        categoryId = loadedCategories.get(i).getId();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    break;
                case ERROR:
                    Toast.makeText(this, "Không thể tải danh sách thể loại", Toast.LENGTH_SHORT).show();
                    break;
                case LOADING:
                    // Có thể show loading nhẹ ở spinner
                    break;
            }
        });
    }

    private void showUploadingState(boolean isUploading) {
        binding.layoutUploading.setVisibility(isUploading ? View.VISIBLE : View.GONE);
        binding.btnSubmit.setEnabled(!isUploading);
    }

}