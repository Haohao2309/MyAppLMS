package com.example.myapplms.ui.student.learning;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.request.SubmitQuizRequest;
import com.example.myapplms.data.remote.dto.response.ProgressResponse;
import com.example.myapplms.utils.Resource;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class QuizFragment extends Fragment {

    private int courseId;
    private String lessonId;
    private String contentJson;

    private LinearLayout layoutQuizForm;
    private LinearLayout layoutQuizResult;
    private LinearLayout container;
    private TextView tvResultScore;

    private Map<String, RadioGroup> questionGroups = new HashMap<>();

    private int durationSeconds;
    private TextView tvQuizTimer;
    private TextView tvQuizAttempts;
    private android.os.CountDownTimer countDownTimer;

    public static QuizFragment newInstance(int courseId, String lessonId, String contentJson, int duration) {
        QuizFragment fragment = new QuizFragment();
        Bundle args = new Bundle();
        args.putInt("COURSE_ID", courseId);
        args.putString("LESSON_ID", lessonId);
        args.putString("CONTENT_JSON", contentJson);
        args.putInt("DURATION", duration);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            courseId = getArguments().getInt("COURSE_ID");
            lessonId = getArguments().getString("LESSON_ID");
            contentJson = getArguments().getString("CONTENT_JSON");
            durationSeconds = getArguments().getInt("DURATION", 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutQuizForm = view.findViewById(R.id.layout_quiz_form);
        layoutQuizResult = view.findViewById(R.id.layout_quiz_result);
        container = view.findViewById(R.id.layout_questions_container);
        tvResultScore = view.findViewById(R.id.tv_result_score);
        tvQuizTimer = view.findViewById(R.id.tv_quiz_timer);
        tvQuizAttempts = view.findViewById(R.id.tv_quiz_attempts);

        Button btnSubmit = view.findViewById(R.id.btn_submit_quiz);
        Button btnRetake = view.findViewById(R.id.btn_retake_quiz);

        renderQuestions();

        btnSubmit.setOnClickListener(v -> submitQuiz());

        btnRetake.setOnClickListener(v -> {
            layoutQuizResult.setVisibility(View.GONE);
            layoutQuizForm.setVisibility(View.VISIBLE);
            for (RadioGroup group : questionGroups.values()) {
                group.clearCheck();
            }
            startTimer();
        });

        // 🟢 BƯỚC KIỂM TRA ĐẦU VÀO: Truy vấn dữ liệu tiến độ thực tế từ Sổ điểm (Gradebook)
        LearningActivity parentActivity = (LearningActivity) getActivity();
        if (parentActivity != null) {
            parentActivity.getViewModel().getProgressLiveData().observe(getViewLifecycleOwner(), resource -> {
                if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                    ProgressResponse progress = resource.data;

                    boolean identityFound = false;
                    int finalScoreValue = 0;
                    int attempts = 0;
                    int maxAttempts = 5; // Mặc định

                    if (progress.lessonDetails != null) {
                        for (ProgressResponse.LessonDetailProgress detail : progress.lessonDetails) {
                            if (lessonId.equals(detail.lessonId)) {
                                if (detail.attemptCount != null) attempts = detail.attemptCount;
                                if (detail.maxAttempts != null) maxAttempts = detail.maxAttempts;
                                // HIỂN THỊ ĐIỂM NẾU ĐÃ LÀM BÀI (CÓ LƯỢT LÀM > 0 VÀ CÓ ĐIỂM)
                                if (detail.score != null && attempts > 0) {
                                    finalScoreValue = detail.score;
                                    identityFound = true;
                                }
                                break;
                            }
                        }
                    }

                    if (identityFound) {
                        showResultUI(finalScoreValue);
                    } else {
                        layoutQuizForm.setVisibility(View.VISIBLE);
                        layoutQuizResult.setVisibility(View.GONE);
                    }
                    
                    // LUÔN CẬP NHẬT GIAO DIỆN SỐ LƯỢT LÀM BÀI ĐỂ KHI BẤM 'LÀM LẠI' NÓ KHÔNG BỊ HIỆN 0/5
                    tvQuizAttempts.setText("Lượt: " + attempts + " / " + maxAttempts);
                    if (attempts >= maxAttempts) {
                        Toast.makeText(getContext(), "Bạn đã hết lượt làm bài!", Toast.LENGTH_LONG).show();
                        btnSubmit.setEnabled(false);
                        btnSubmit.setText("Hết lượt");
                    } else {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("Nộp bài");
                        if (!identityFound) {
                            startTimer();
                        }
                    }
                }
            });
        }
    }

    private void showResultUI(int score) {
        layoutQuizForm.setVisibility(View.GONE);
        layoutQuizResult.setVisibility(View.VISIBLE);
        tvResultScore.setText(score + " / 100");
        stopTimer();
    }

    private void startTimer() {
        if (durationSeconds <= 0) {
            tvQuizTimer.setText("Không giới hạn");
            return;
        }

        stopTimer();
        countDownTimer = new android.os.CountDownTimer(durationSeconds * 1000L, 1000) {
            public void onTick(long millisUntilFinished) {
                long totalSecs = millisUntilFinished / 1000;
                long minutes = totalSecs / 60;
                long seconds = totalSecs % 60;
                tvQuizTimer.setText(String.format("%02d:%02d", minutes, seconds));
                
                // Cảnh báo khi còn dưới 1 phút
                if (totalSecs <= 60 && isAdded()) {
                    tvQuizTimer.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
                }
            }

            public void onFinish() {
                tvQuizTimer.setText("00:00");
                Toast.makeText(getContext(), "Hết giờ làm bài! Hệ thống tự động nộp bài.", Toast.LENGTH_LONG).show();
                submitQuiz();
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopTimer();
    }

    private void renderQuestions() {
        try {
            JSONObject mainObj = new JSONObject(contentJson);
            JSONArray questions = mainObj.getJSONArray("questions");

            container.removeAllViews();
            questionGroups.clear();

            int textColor = ContextCompat.getColor(requireContext(), R.color.text_primary);
            int primaryColor = ContextCompat.getColor(requireContext(), R.color.primary);

            for (int i = 0; i < questions.length(); i++) {
                JSONObject qObj = questions.getJSONObject(i);
                String qId = qObj.getString("id");
                String questionText = qObj.getString("question");
                JSONArray options = qObj.getJSONArray("options");

                TextView tvQuestion = new TextView(getContext());
                tvQuestion.setText((i + 1) + ". " + questionText);
                tvQuestion.setTextSize(16f);
                tvQuestion.setTextColor(textColor);
                tvQuestion.setPadding(0, 24, 0, 12);
                container.addView(tvQuestion);

                RadioGroup radioGroup = new RadioGroup(getContext());
                radioGroup.setOrientation(RadioGroup.VERTICAL);

                for (int j = 0; j < options.length(); j++) {
                    RadioButton rb = new RadioButton(getContext());
                    rb.setId(View.generateViewId());
                    rb.setTag(j); // Gán index thực tế vào Tag để Backend đối chiếu
                    rb.setText(options.getString(j));
                    rb.setTextColor(textColor);
                    rb.setTextSize(15f);
                    rb.setPadding(16, 20, 16, 20);
                    rb.setButtonTintList(ColorStateList.valueOf(primaryColor));
                    radioGroup.addView(rb);
                }

                container.addView(radioGroup);
                questionGroups.put(qId, radioGroup);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi cấu trúc câu hỏi!", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitQuiz() {
        Map<String, String> studentAnswers = new HashMap<>();

        for (Map.Entry<String, RadioGroup> entry : questionGroups.entrySet()) {
            String qId = entry.getKey();
            RadioGroup group = entry.getValue();

            int selectedViewId = group.getCheckedRadioButtonId();
            if (selectedViewId != -1) {
                RadioButton selectedRb = group.findViewById(selectedViewId);
                if (selectedRb != null && selectedRb.getTag() != null) {
                    int answerIndex = (int) selectedRb.getTag();
                    studentAnswers.put(qId, String.valueOf(answerIndex));
                }
            }
        }

        if (studentAnswers.size() < questionGroups.size()) {
            Toast.makeText(getContext(), "Vui lòng hoàn thành tất cả câu hỏi!", Toast.LENGTH_SHORT).show();
            return;
        }

        SubmitQuizRequest request = new SubmitQuizRequest(studentAnswers);

        LearningActivity parentActivity = (LearningActivity) getActivity();
        if (parentActivity != null) {
            if (parentActivity.isPreviewMode()) {
                Toast.makeText(getContext(), "Tính năng nộp bài không hỗ trợ khi xem thử!", Toast.LENGTH_SHORT).show();
                return;
            }
            parentActivity.getViewModel().submitQuiz(courseId, lessonId, request).observe(getViewLifecycleOwner(), resource -> {
                switch (resource.status) {
                    case LOADING:
                        // Toast.makeText(getContext(), "Đang gửi bài và chấm điểm...", Toast.LENGTH_SHORT).show();
                        break;
                    case SUCCESS:
                        // Toast.makeText(getContext(), "Nộp bài thành công!", Toast.LENGTH_SHORT).show();

                        // 1. Cập nhật lại thanh tiến độ và dấu tích xanh ở thanh Menu bên ngoài
                        parentActivity.loadProgress();

                        // 2. TỰ ĐỘNG LẬT SANG TRANG KẾT QUẢ VÀ HIỆN ĐIỂM NGAY LẬP TỨC
                        if (resource.data != null) {
                            int finalScore = 0;
                            // Tìm điểm số của bài học hiện tại trong cục Data trả về
                            if (resource.data.lessonDetails != null) {
                                for (ProgressResponse.LessonDetailProgress detail : resource.data.lessonDetails) {
                                    if (lessonId.equals(detail.lessonId) && detail.score != null) {
                                        finalScore = detail.score;
                                        break;
                                    }
                                }
                            }
                            // Gọi hàm ẩn Form thi và show Bảng điểm
                            showResultUI(finalScore);
                        }
                        break;
                    case ERROR:
                        // Toast.makeText(getContext(), "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
                        break;
                }
            });
        }
    }
}