package com.example.myapplms.ui.student.learning;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplms.data.remote.dto.request.CreateDiscussionRequest;
import com.example.myapplms.data.remote.dto.request.SubmitAssignmentRequest;
import com.example.myapplms.data.remote.dto.request.SubmitQuizRequest;
import com.example.myapplms.data.remote.dto.request.SyncVideoRequest;
import com.example.myapplms.data.remote.dto.response.DiscussionResponse;
import com.example.myapplms.data.remote.dto.response.ProgressResponse;
import com.example.myapplms.data.repository.LearningRepository;
import com.example.myapplms.utils.Resource;

import java.util.List;

public class LearningViewModel extends ViewModel {

    private final LearningRepository repository;

    public LearningViewModel(LearningRepository repository) {
        this.repository = repository;
    }

    // 1. Lấy tiến độ
    public LiveData<Resource<ProgressResponse>> getProgressLiveData() {
        return repository.getProgressLiveData();
    }

    public void fetchProgress(int courseId) {
        repository.fetchProgress(courseId);
    }

    // 2. Đồng bộ Video ngầm
    public LiveData<Resource<Void>> syncVideoProgress(int courseId, String lessonId, SyncVideoRequest request) {
        return repository.syncVideoProgress(courseId, lessonId, request);
    }

    // 3. Nộp Quiz
    public LiveData<Resource<ProgressResponse>> submitQuiz(int courseId, String lessonId, SubmitQuizRequest request) {
        return repository.submitQuiz(courseId, lessonId, request);
    }

    // 4. Nộp Assignment
    public LiveData<Resource<ProgressResponse>> submitAssignment(int courseId, String lessonId, SubmitAssignmentRequest request) {
        return repository.submitAssignment(courseId, lessonId, request);
    }
    public LiveData<Resource<List<DiscussionResponse>>> getDiscussions(int courseId, String lessonId) {
        return repository.getDiscussions(courseId, lessonId);
    }

    public LiveData<Resource<DiscussionResponse>> createDiscussion(int courseId, String lessonId, CreateDiscussionRequest request) {
        return repository.createDiscussion(courseId, lessonId, request);
    }
}