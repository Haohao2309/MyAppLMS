package com.example.myapplms.ui.student.learning;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplms.data.remote.dto.request.SubmitAssignmentRequest;
import com.example.myapplms.data.remote.dto.request.SubmitQuizRequest;
import com.example.myapplms.data.remote.dto.request.SyncVideoRequest;
import com.example.myapplms.data.remote.dto.response.ProgressResponse;
import com.example.myapplms.data.repository.LearningRepository;
import com.example.myapplms.utils.Resource;

public class LearningViewModel extends ViewModel {

    private final LearningRepository repository;

    public LearningViewModel(LearningRepository repository) {
        this.repository = repository;
    }

    // 1. Lấy tiến độ
    public LiveData<Resource<ProgressResponse>> getProgress(int courseId) {
        return repository.getProgress(courseId);
    }

    // 2. Đồng bộ Video ngầm
    public void syncVideoProgress(int courseId, String lessonId, SyncVideoRequest request) {
        repository.syncVideoProgress(courseId, lessonId, request);
    }

    // 3. Nộp Quiz
    public LiveData<Resource<ProgressResponse>> submitQuiz(int courseId, String lessonId, SubmitQuizRequest request) {
        return repository.submitQuiz(courseId, lessonId, request);
    }

    // 4. Nộp Assignment
    public LiveData<Resource<ProgressResponse>> submitAssignment(int courseId, String lessonId, SubmitAssignmentRequest request) {
        return repository.submitAssignment(courseId, lessonId, request);
    }
}