package com.example.myapplms.ui.teacher.grading;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.R;
import com.example.myapplms.data.RetrofitClient;
import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.request.SubmitGradeRequest;
import com.example.myapplms.data.remote.dto.response.GradingListResponse;
import com.example.myapplms.data.repository.GradingRepository;
import com.example.myapplms.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

public class GradingActivity extends AppCompatActivity {

    private static final String EXTRA_ITEM      = "grading_item";
    private static final String EXTRA_COURSE_ID = "course_id";

    private TextView tvStudentName, tvSubmitTime, tvCurrentScore, tvTotalScore;
    private MaterialButton btnOpenFile, btnSubmitGrade;
    private TextInputEditText etContent, etStructure, etLanguage, etCritical;
    private ChipGroup chipGroup;
    private EditText etComment;

    private GradingListResponse.StudentGradingItem item;
    private int courseId;
    private GradingViewModel viewModel;

    private static final int MAX_PER = 25; // thang 100 chia 4 tiêu chí

    // ── Start helper ────────────────────────────────────────────

    public static void start(Context context, GradingListResponse.StudentGradingItem item, int courseId) {
        Intent intent = new Intent(context, GradingActivity.class);
        intent.putExtra(EXTRA_ITEM, new Gson().toJson(item));
        intent.putExtra(EXTRA_COURSE_ID, courseId);
        context.startActivity(intent);
    }

    // ── Lifecycle ────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grading);

        item     = new Gson().fromJson(getIntent().getStringExtra(EXTRA_ITEM),
                GradingListResponse.StudentGradingItem.class);
        courseId = getIntent().getIntExtra(EXTRA_COURSE_ID, -1);

        initViews();
        setupViewModel();
        populateHeader();
        setupScoreInputs();
        setupFeedbackChips();
        setupButtons();
        observeGradeResult();
    }

    // ── Init ─────────────────────────────────────────────────────

    private void initViews() {
        tvStudentName  = findViewById(R.id.tv_grading_student_name);
        tvSubmitTime   = findViewById(R.id.tv_grading_submit_time);
        tvCurrentScore = findViewById(R.id.tv_current_score);
        tvTotalScore   = findViewById(R.id.tv_total_score);

        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        btnOpenFile    = findViewById(R.id.btn_open_file);
        btnSubmitGrade = findViewById(R.id.btn_submit_grade);

        etContent   = findViewById(R.id.et_score_content);
        etStructure = findViewById(R.id.et_score_structure);
        etLanguage  = findViewById(R.id.et_score_language);
        etCritical  = findViewById(R.id.et_score_critical);

        chipGroup = findViewById(R.id.chip_group_feedback);
        etComment = findViewById(R.id.et_feedback_comment);
    }

    private void setupViewModel() {
        SessionManager sm = new SessionManager(this);
        LmsApiService api = RetrofitClient.getInstance(sm).create(LmsApiService.class);
        GradingRepository repo = new GradingRepository(api);
        viewModel = new ViewModelProvider(this, new GradingViewModelFactory(repo))
                .get(GradingViewModel.class);
    }

    // ── Populate ─────────────────────────────────────────────────

    private void populateHeader() {
        String name = item.getFullName() != null
                ? item.getFullName() : "Sinh viên #" + item.getStudentId();
        tvStudentName.setText(name);
        tvSubmitTime.setText(item.getSubmittedAt() != null ? item.getSubmittedAt() : "--");

        // Nếu đã chấm trước → hiện điểm cũ
        if (item.getFinalScore() != null) {
            tvCurrentScore.setText(String.format("%.1f", item.getFinalScore()));
        } else {
            tvCurrentScore.setText("--");
        }
    }

    private void setupScoreInputs() {
        // Điền lại điểm cũ nếu đã chấm (examScore BE lưu theo thang 100)
        if (item.getExamScore() != null) {
            int old = item.getExamScore().intValue();
            // Chia đều cho 4 ô (chỉ hiển thị tham khảo)
            int each = old / 4;
            etContent.setText(String.valueOf(each));
            etStructure.setText(String.valueOf(each));
            etLanguage.setText(String.valueOf(each));
            etCritical.setText(String.valueOf(old - each * 3));
        }

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { recalcTotal(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        etContent.addTextChangedListener(watcher);
        etStructure.addTextChangedListener(watcher);
        etLanguage.addTextChangedListener(watcher);
        etCritical.addTextChangedListener(watcher);
    }

    private void recalcTotal() {
        int total = parseScore(etContent) + parseScore(etStructure)
                + parseScore(etLanguage) + parseScore(etCritical);
        tvTotalScore.setText(String.valueOf(total));
        tvCurrentScore.setText(String.valueOf(total));
    }

    private int parseScore(EditText et) {
        try {
            return Math.min(Integer.parseInt(et.getText().toString().trim()), MAX_PER);
        } catch (NumberFormatException e) { return 0; }
    }

    private void setupFeedbackChips() {
        String[] tags = {"Lập luận tốt", "Cần ví dụ hơn", "Ngôn ngữ rõ ràng",
                "Thiếu phân tích", "Cấu trúc logic", "Tư duy sáng tạo"};
        for (String tag : tags) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setCheckable(true);
            chipGroup.addView(chip);
        }
    }

    // ── Submit ───────────────────────────────────────────────────

    private void setupButtons() {
        btnOpenFile.setOnClickListener(v -> {
            String url = item.getFileUrl();
            if (url != null && !url.isEmpty()) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } else {
                Toast.makeText(this, "Không có file đính kèm", Toast.LENGTH_SHORT).show();
            }
        });

        btnSubmitGrade.setOnClickListener(v -> submitGrade());
    }

    private void submitGrade() {
        // examScore = tổng 4 tiêu chí (thang 100)
        int examScore = parseScore(etContent) + parseScore(etStructure)
                + parseScore(etLanguage) + parseScore(etCritical);

        if (examScore <= 0) {
            Toast.makeText(this, "Vui lòng nhập điểm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển về thang 10 để BE tính gradeLevel nhất quán
        double examScore10 = examScore;

        SubmitGradeRequest request = new SubmitGradeRequest(
                item.getStudentId(),
                courseId,
                examScore10,
                false   // false = công bố điểm ngay; true = ẩn
        );

        viewModel.submitGrade(request);
    }

    // ── Observe ──────────────────────────────────────────────────

    private void observeGradeResult() {
        viewModel.gradeResult.observe(this, resource -> {
            switch (resource.status) {
                case LOADING:
                    btnSubmitGrade.setEnabled(false);
                    btnSubmitGrade.setText("Đang lưu...");
                    break;
                case SUCCESS:
                    Toast.makeText(this, "Đã ghi nhận điểm!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                    break;
                case ERROR:
                    btnSubmitGrade.setEnabled(true);
                    btnSubmitGrade.setText("Ghi nhận điểm");
                    Toast.makeText(this, "Lỗi: " + resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }
}