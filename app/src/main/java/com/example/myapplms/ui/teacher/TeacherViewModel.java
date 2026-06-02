package com.example.myapplms.ui.teacher;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.data.mapper.TeacherMapper;
import com.example.myapplms.data.remote.dto.request.TeacherRequest;
import com.example.myapplms.data.remote.dto.response.TeacherResponse;
import com.example.myapplms.data.repository.TeacherRepository;
import com.example.myapplms.model.Teacher;
import com.example.myapplms.utils.Resource;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TeacherViewModel extends ViewModel {

    private final TeacherRepository teacherRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // ── LiveData ──────────────────────────────────────────────
    private final MutableLiveData<Resource<List<TeacherResponse>>> _teachers = new MutableLiveData<>();
    public final LiveData<Resource<List<TeacherResponse>>> teachers = _teachers;

    // Đổi từ TeacherResponse → Teacher (domain model)
    private final MutableLiveData<Resource<Teacher>> _teacher = new MutableLiveData<>();
    public final LiveData<Resource<Teacher>> teacher = _teacher;

    private final MutableLiveData<Resource<Teacher>> _updateResult = new MutableLiveData<>();
    public final LiveData<Resource<Teacher>> updateResult = _updateResult;

    public TeacherViewModel(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    // ── Fetch list ────────────────────────────────────────────
    public void fetchTeachers() {
        _teachers.setValue(Resource.loading());
        executor.execute(() -> {
            Resource<List<TeacherResponse>> result = teacherRepository.getTeacher();
            _teachers.postValue(result);
        });
    }

    // ── Get by ID ─────────────────────────────────────────────
    public void getTeacherbyId(Integer id) {
        _teacher.setValue(Resource.loading());
        executor.execute(() -> {
            Resource<Teacher> result = teacherRepository.getTeacherById(id);
            _teacher.postValue(result);
        });
    }

    // ── Update ────────────────────────────────────────────────
    public void updateTeacher(Integer teacherId, TeacherRequest request) {
        _updateResult.setValue(Resource.loading());
        executor.execute(() -> {
            Resource<Teacher> result = teacherRepository.updateTeacher(teacherId, request);
            _updateResult.postValue(result);
            // Đồng bộ lại _teacher nếu update thành công
            if (result.status == Resource.Status.SUCCESS) {
                _teacher.postValue(result);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

    // ── Factory ───────────────────────────────────────────────
    public static class Factory implements ViewModelProvider.Factory {
        private final TeacherRepository repository;

        public Factory(TeacherRepository repository) {
            this.repository = repository;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new TeacherViewModel(repository);
        }
    }
}