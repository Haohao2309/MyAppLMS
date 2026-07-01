package com.example.myapplms.ui.explore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplms.data.remote.dto.request.CourseRequest;
import com.example.myapplms.data.remote.dto.response.CategoryResponse;
import com.example.myapplms.data.repository.CourseRepository;
import com.example.myapplms.data.repository.MediaRepository;
import com.example.myapplms.model.Course;
import com.example.myapplms.utils.Resource;

import java.io.File;
import java.util.List;

public class TeacherCourseViewModel extends ViewModel {

    private final CourseRepository courseRepository;

    private final MutableLiveData<Resource<List<Course>>> _myCourses = new MutableLiveData<>();
    public final LiveData<Resource<List<Course>>> myCourses = _myCourses;

    private final MutableLiveData<Resource<List<CategoryResponse>>> _categories = new MutableLiveData<>();
    public final LiveData<Resource<List<CategoryResponse>>> categories = _categories;

    private final MutableLiveData<Resource<Course>> _createResult = new MutableLiveData<>();
    public final LiveData<Resource<Course>> createResult = _createResult;

    private final MutableLiveData<Resource<Course>> _updateResult = new MutableLiveData<>();
    public final LiveData<Resource<Course>> updateResult = _updateResult;

    private final MutableLiveData<Resource<String>> _uploadResult = new MutableLiveData<>();
    public final LiveData<Resource<String>> uploadResult = _uploadResult;

    private final MediaRepository mediaRepository;

    public TeacherCourseViewModel(CourseRepository courseRepository, MediaRepository mediaRepository) {
        this.courseRepository = courseRepository;
        this.mediaRepository = mediaRepository;
    }

    // ── Load khóa học của giảng viên ─────────────────────────
    public void loadMyCourses(Integer teacherId) {
        courseRepository.getCoursesByTeacherId(teacherId)
                .observeForever(result -> _myCourses.setValue(result));
    }

    public void loadCategories() {
        _categories.postValue(Resource.loading());
        new Thread(() -> {
            Resource<List<CategoryResponse>> res = courseRepository.getCategories();
            _categories.postValue(res);
        }).start();
    }

    // ── Tạo khóa học mới ─────────────────────────────────────
    public void createCourse(Integer teacherId, Integer categoryId, String title,
                             String description, String imageUrl, Double price) {
        CourseRequest request = new CourseRequest(teacherId, categoryId, title,
                description, imageUrl, price);
        courseRepository.createCourse(request)
                .observeForever(result -> _createResult.setValue(result));
    }

    // ── Cập nhật khóa học ────────────────────────────────────
    public void updateCourse(Integer courseId, Integer teacherId, Integer categoryId,
                             String title, String description, String imageUrl, Double price) {
        CourseRequest request = new CourseRequest(teacherId, categoryId, title,
                description, imageUrl, price);
        courseRepository.updateCourse(courseId, request)
                .observeForever(result -> _updateResult.setValue(result));
    }

    private final MutableLiveData<Resource<String>> _deleteResult = new MutableLiveData<>();
    public final LiveData<Resource<String>> deleteResult = _deleteResult;

    private final MutableLiveData<Resource<String>> _restoreResult = new MutableLiveData<>();
    public final LiveData<Resource<String>> restoreResult = _restoreResult;

    public void uploadCourseImage(File imageFile) {
        mediaRepository.uploadCourseImage(imageFile)
                .observeForever(result -> _uploadResult.setValue(result));
    }

    // ── Xóa mềm khóa học ─────────────────────────────────────
    public void deleteCourse(Integer courseId, String deletedBy, String reason) {
        courseRepository.deleteCourse(courseId, deletedBy, reason)
                .observeForever(result -> _deleteResult.setValue(result));
    }

    // ── Khôi phục khóa học ───────────────────────────────────
    public void restoreCourse(Integer courseId, String restoredBy) {
        courseRepository.restoreCourse(courseId, restoredBy)
                .observeForever(result -> _restoreResult.setValue(result));
    }

    public void clearDeleteResult() {
        _deleteResult.postValue(null);
    }

    public void clearRestoreResult() {
        _restoreResult.postValue(null);
    }
}