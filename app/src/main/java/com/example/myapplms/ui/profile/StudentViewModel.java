package com.example.myapplms.ui.profile;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.data.mapper.StudentMapper;
import com.example.myapplms.data.remote.dto.request.StudentRequest;
import com.example.myapplms.data.repository.StudentRepository;
import com.example.myapplms.model.Student;
import com.example.myapplms.utils.Resource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * StudentViewModel — quản lý trạng thái UI cho màn Profile (role STUDENT).
 *
 * Exposed LiveData:
 *   • student      — kết quả GET profile
 *   • updateResult — kết quả PUT (update profile)
 */
public class StudentViewModel extends ViewModel {

    private final StudentRepository repository;
    private final ExecutorService   executor = Executors.newSingleThreadExecutor();

    // ── LiveData ──────────────────────────────────────────────────────────────

    private final MutableLiveData<Resource<Student>> _student = new MutableLiveData<>();
    /** Quan sát để hiển thị thông tin sinh viên. */
    public final LiveData<Resource<Student>> student = _student;

    private final MutableLiveData<Resource<Student>> _updateResult = new MutableLiveData<>();
    /** Quan sát kết quả cập nhật (dùng ở màn Edit Profile). */
    public final LiveData<Resource<Student>> updateResult = _updateResult;

    public StudentViewModel(StudentRepository repository) {
        this.repository = repository;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    /**
     * Tải thông tin sinh viên theo userId.
     * Kết quả đẩy vào LiveData {@code student}.
     */
    public void getStudentByUserId(Integer userId) {
        _student.setValue(Resource.loading());
        executor.execute(() -> {
            Resource<Student> result = repository.getStudentByUserId(userId);
            _student.postValue(result);
        });
    }

    /**
     * Cập nhật thông tin sinh viên.
     *
     * Cách dùng từ Fragment:
     *   Student current = studentViewModel.student.getValue().data;
     *   StudentRequest req = StudentMapper.toRequest(current);
     *   req.setPhone("0912...");
     *   studentViewModel.updateStudent(current.getStudentId(), req);
     */
    public void updateStudent(Integer studentId, StudentRequest request) {
        _updateResult.setValue(Resource.loading());
        executor.execute(() -> {
            Resource<Student> result = repository.updateStudent(studentId, request);
            _updateResult.postValue(result);
            // Nếu update thành công, đồng bộ lại LiveData student
            if (result.status == Resource.Status.SUCCESS) {
                _student.postValue(result);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    public static class Factory implements ViewModelProvider.Factory {

        private final StudentRepository repository;

        public Factory(StudentRepository repository) {
            this.repository = repository;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new StudentViewModel(repository);
        }
    }
}