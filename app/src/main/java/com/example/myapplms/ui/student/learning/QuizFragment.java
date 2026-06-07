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

    // Các thành phần UI
    private LinearLayout layoutQuizForm;
    private LinearLayout layoutQuizResult;
    private LinearLayout container;
    private TextView tvResultScore;

    private Map<String, RadioGroup> questionGroups = new HashMap<>();

    public static QuizFragment newInstance(int courseId, String lessonId, String contentJson) {
        QuizFragment fragment = new QuizFragment();
        Bundle args = new Bundle();
        args.putInt("COURSE_ID", courseId);
        args.putString("LESSON_ID", lessonId);
        args.putString("CONTENT_JSON", contentJson);
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

        // Ánh xạ View
        layoutQuizForm = view.findViewById(R.id.layout_quiz_form);
        layoutQuizResult = view.findViewById(R.id.layout_quiz_result);
        container = view.findViewById(R.id.layout_questions_container);
        tvResultScore = view.findViewById(R.id.tv_result_score);

        Button btnSubmit = view.findViewById(R.id.btn_submit_quiz);
        Button btnRetake = view.findViewById(R.id.btn_retake_quiz);

        // Chuẩn bị form làm bài
        renderQuestions();

        // Nút nộp bài
        btnSubmit.setOnClickListener(v -> submitQuiz());

        // Nút làm lại bài (Reset Form)
        btnRetake.setOnClickListener(v -> {
            layoutQuizResult.setVisibility(View.GONE);
            layoutQuizForm.setVisibility(View.VISIBLE);
            for (RadioGroup group : questionGroups.values()) {
                group.clearCheck();
            }
        });

        // 🟢 KIỂM TRA TRẠNG THÁI: Đã làm bài chưa?
        LearningActivity parentActivity = (LearningActivity) getActivity();
        if (parentActivity != null) {
            parentActivity.getViewModel().getProgress(courseId).observe(getViewLifecycleOwner(), resource -> {
                if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                    ProgressResponse progress = resource.data;

                    // Nếu bài thi nằm trong danh sách đã hoàn thành
                    if (progress.completedLessons != null && progress.completedLessons.contains(lessonId)) {
                        int score = 0;
                        if (progress.lessonDetails != null) {
                            for (ProgressResponse.LessonDetailProgress detail : progress.lessonDetails) {
                                if (lessonId.equals(detail.lessonId)) {
                                    score = detail.score != null ? detail.score : 0;
                                    break;
                                }
                            }
                        }
                        showResultUI(score);
                    } else {
                        // Nếu chưa làm -> Hiện Form
                        layoutQuizForm.setVisibility(View.VISIBLE);
                        layoutQuizResult.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void showResultUI(int score) {
        layoutQuizForm.setVisibility(View.GONE);
        layoutQuizResult.setVisibility(View.VISIBLE);

        int passScore = 10; // Giả sử điểm tối đa là 10
        try {
            JSONObject mainObj = new JSONObject(contentJson);
            if (mainObj.has("passScore")) {
                passScore = mainObj.getInt("passScore"); // Hoặc lấy MaxScore nếu DB bạn cấu hình
            }
        } catch (Exception e) { e.printStackTrace(); }

        tvResultScore.setText(score + " / " + passScore);
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
                    // 🔥 FIX LỖI 0 ĐIỂM: Tạo ID chống trùng lặp và giấu index vào Tag
                    rb.setId(View.generateViewId());
                    rb.setTag(j);

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
            Toast.makeText(getContext(), "Lỗi tải câu hỏi!", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitQuiz() {
        Map<String, String> studentAnswers = new HashMap<>();

        for (Map.Entry<String, RadioGroup> entry : questionGroups.entrySet()) {
            String qId = entry.getKey();
            RadioGroup group = entry.getValue();

            int selectedViewId = group.getCheckedRadioButtonId();
            if (selectedViewId != -1) {
                // 🔥 FIX LỖI 0 ĐIỂM: Rút chính xác Index đáp án ra từ Tag
                RadioButton selectedRb = group.findViewById(selectedViewId);
                if (selectedRb != null && selectedRb.getTag() != null) {
                    int answerIndex = (int) selectedRb.getTag();
                    studentAnswers.put(qId, String.valueOf(answerIndex));
                }
            }
        }

        if (studentAnswers.size() < questionGroups.size()) {
            Toast.makeText(getContext(), "Vui lòng làm hết tất cả các câu hỏi!", Toast.LENGTH_SHORT).show();
            return;
        }

        SubmitQuizRequest request = new SubmitQuizRequest(studentAnswers);

        LearningActivity parentActivity = (LearningActivity) getActivity();
        if (parentActivity != null) {
            parentActivity.getViewModel().submitQuiz(courseId, lessonId, request).observe(getViewLifecycleOwner(), resource -> {
                switch (resource.status) {
                    case LOADING:
                        Toast.makeText(getContext(), "Hệ thống đang chấm điểm...", Toast.LENGTH_SHORT).show();
                        break;
                    case SUCCESS:
                        Toast.makeText(getContext(), "Nộp bài thành công!", Toast.LENGTH_SHORT).show();
                        // Hàm này gọi lên cha, cha sẽ gọi API lấy Progress mới nhất
                        // Progress thay đổi -> Fragment sẽ tự động nhảy vào hàm showResultUI() ở trên!
                        parentActivity.loadProgress();
                        break;
                    case ERROR:
                        Toast.makeText(getContext(), "Lỗi nộp bài: " + resource.message, Toast.LENGTH_SHORT).show();
                        break;
                }
            });
        }
    }
}