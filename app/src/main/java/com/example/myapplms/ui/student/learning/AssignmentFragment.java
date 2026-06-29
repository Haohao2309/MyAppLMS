package com.example.myapplms.ui.student.learning;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.request.SubmitAssignmentRequest;

public class AssignmentFragment extends Fragment {

    private int courseId;
    private String lessonId;
    private String contentJson;

    public static AssignmentFragment newInstance(int courseId, String lessonId, String contentJson) {
        AssignmentFragment fragment = new AssignmentFragment();
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
        return inflater.inflate(R.layout.fragment_assignment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText etDriveLink = view.findViewById(R.id.et_drive_link);
        EditText etNotes = view.findViewById(R.id.et_student_notes);
        Button btnSubmit = view.findViewById(R.id.btn_submit_assignment);
        TextView tvAttempts = view.findViewById(R.id.tv_assignment_attempts);
        TextView tvInstructions = view.findViewById(R.id.tv_assignment_instructions);

        if (tvInstructions != null && contentJson != null) {
            try {
                org.json.JSONObject obj = new org.json.JSONObject(contentJson);
                if (obj.has("instructions")) {
                    tvInstructions.setText(obj.getString("instructions"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        LearningActivity parentActivity = (LearningActivity) getActivity();
        if (parentActivity != null) {
            parentActivity.getViewModel().getProgressLiveData().observe(getViewLifecycleOwner(), resource -> {
                if (resource != null && resource.data != null) {
                    com.example.myapplms.data.remote.dto.response.ProgressResponse progress = resource.data;
                    int attempts = 0;
                    int maxAttempts = 1;

                    if (progress.lessonDetails != null) {
                        for (com.example.myapplms.data.remote.dto.response.ProgressResponse.LessonDetailProgress detail : progress.lessonDetails) {
                            if (lessonId.equals(detail.lessonId)) {
                                if (detail.attemptCount != null) attempts = detail.attemptCount;
                                if (detail.maxAttempts != null) maxAttempts = detail.maxAttempts;
                                break;
                            }
                        }
                    }

                    tvAttempts.setText("Lượt: " + attempts + " / " + maxAttempts);
                    if (attempts >= maxAttempts) {
                        btnSubmit.setEnabled(false);
                        btnSubmit.setText("Hết lượt");
                    } else {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText(R.string.action_submit);
                    }
                }
            });
        }

        btnSubmit.setOnClickListener(v -> {
            String link = etDriveLink.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();

            if (link.isEmpty()) {
                etDriveLink.setError("Không được để trống link!");
                return;
            }

            SubmitAssignmentRequest request = new SubmitAssignmentRequest(link, notes);

            // GỌI API NỘP BÀI TẬP:
            if (parentActivity != null) {
                if (parentActivity.isPreviewMode()) {
                    Toast.makeText(getContext(), "Tính năng nộp bài không hỗ trợ khi xem thử!", Toast.LENGTH_SHORT).show();
                    return;
                }
                parentActivity.getViewModel().submitAssignment(courseId, lessonId, request).observe(getViewLifecycleOwner(), resource -> {
                    switch (resource.status) {
                        case LOADING:
                            // Toast.makeText(getContext(), "Đang gửi bài...", Toast.LENGTH_SHORT).show();
                            break;
                        case SUCCESS:
                            // Toast.makeText(getContext(), "Đã nộp! Chờ giảng viên chấm.", Toast.LENGTH_LONG).show();
                            // BÁO ACTIVITY CHA TẢI LẠI TIẾN ĐỘ ĐỂ HIỆN %
                            parentActivity.loadProgress();
                            break;
                        case ERROR:
                            // Toast.makeText(getContext(), "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
                            break;
                    }
                });
            }
        });
    }
}