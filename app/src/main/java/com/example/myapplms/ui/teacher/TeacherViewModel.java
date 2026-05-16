package com.example.myapplms.ui.teacher;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplms.data.remote.dto.response.TeacherResponse;
import com.example.myapplms.data.repository.TeacherRepository;
import com.example.myapplms.utils.Resource;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TeacherViewModel extends ViewModel {

    private final TeacherRepository teacherRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<Resource<List<TeacherResponse>>> _teachers = new MutableLiveData<>();
    public final LiveData<Resource<List<TeacherResponse>>> teachers = _teachers;
    private final MutableLiveData<Resource<TeacherResponse>> _teacher = new MutableLiveData<>();
    public final LiveData<Resource<TeacherResponse>> teacher = _teacher;


    public TeacherViewModel(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    public void fetchTeachers() {
        _teachers.setValue(Resource.loading());
        executor.execute(() -> {
            Resource<List<TeacherResponse>> result = teacherRepository.getTeacher();
            _teachers.postValue(result);
        });
    }
    public void getTeacherbyId(Integer id){
        _teacher.setValue(Resource.loading());
        executor.execute(() -> {
            Resource<TeacherResponse> result = teacherRepository.getTeacherbyId(id);
            _teacher.postValue(result);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
