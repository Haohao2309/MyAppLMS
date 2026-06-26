package com.example.myapplms.ui.teacher.grading;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplms.data.remote.dto.request.SubmitGradeRequest;
import com.example.myapplms.data.remote.dto.response.GradingListResponse;
import com.example.myapplms.data.repository.GradingRepository;
import com.example.myapplms.utils.Resource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GradingViewModel extends ViewModel {

    private final GradingRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<Resource<GradingListResponse>> _gradingList = new MutableLiveData<>();
    public final LiveData<Resource<GradingListResponse>> gradingList = _gradingList;

    private final MutableLiveData<Resource<GradingListResponse.StudentGradingItem>> _gradeResult = new MutableLiveData<>();
    public final LiveData<Resource<GradingListResponse.StudentGradingItem>> gradeResult = _gradeResult;

    public GradingViewModel(GradingRepository repository) {
        this.repository = repository;
    }

    public void loadGradingList(int courseId) {
        _gradingList.setValue(Resource.loading());
        executor.execute(() -> _gradingList.postValue(repository.getGradingList(courseId)));
    }

    public void submitGrade(SubmitGradeRequest request) {
        _gradeResult.setValue(Resource.loading());
        executor.execute(() -> _gradeResult.postValue(repository.submitGrade(request)));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}