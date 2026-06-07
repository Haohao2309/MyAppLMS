package com.example.myapplms.ui.student.learning;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.request.SubmitAssignmentRequest;

public class AssignmentFragment extends Fragment {

    private int courseId;
    private String lessonId;

    public static AssignmentFragment newInstance(int courseId, String lessonId) {
        AssignmentFragment fragment = new AssignmentFragment();
        Bundle args = new Bundle();
        args.putInt("COURSE_ID", courseId);
        args.putString("LESSON_ID", lessonId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            courseId = getArguments().getInt("COURSE_ID");
            lessonId = getArguments().getString("LESSON_ID");
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

        btnSubmit.setOnClickListener(v -> {
            String link = etDriveLink.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();

            if (link.isEmpty()) {
                etDriveLink.setError("Không được để trống link!");
                return;
            }

            SubmitAssignmentRequest request = new SubmitAssignmentRequest(link, notes);

            // GỌI API NỘP BÀI TẬP:
            LearningActivity parentActivity = (LearningActivity) getActivity();
            if (parentActivity != null) {
                parentActivity.getViewModel().submitAssignment(courseId, lessonId, request).observe(getViewLifecycleOwner(), resource -> {
                    switch (resource.status) {
                        case LOADING:
                            Toast.makeText(getContext(), "Đang gửi bài...", Toast.LENGTH_SHORT).show();
                            break;
                        case SUCCESS:
                            Toast.makeText(getContext(), "Đã nộp! Chờ giảng viên chấm.", Toast.LENGTH_LONG).show();
                            // 👉 BẢO ACTIVITY CHA TẢI LẠI TIẾN ĐỘ ĐỂ HIỆN ✅
                            parentActivity.loadProgress();
                            break;
                        case ERROR:
                            Toast.makeText(getContext(), "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
                            break;
                    }
                });
            }
        });
    }
}