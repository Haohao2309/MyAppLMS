package com.example.myapplms.ui.explore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplms.data.remote.dto.request.CourseRequest;
import com.example.myapplms.data.repository.CourseRepository;
import com.example.myapplms.model.Course;
import com.example.myapplms.utils.Resource;

import java.util.List;

public class TeacherCourseViewModel extends ViewModel {

    private final CourseRepository courseRepository;

    private final MutableLiveData<Resource<List<Course>>> _myCourses = new MutableLiveData<>();
    public final LiveData<Resource<List<Course>>> myCourses = _myCourses;

    private final MutableLiveData<Resource<Course>> _createResult = new MutableLiveData<>();
    public final LiveData<Resource<Course>> createResult = _createResult;

    private final MutableLiveData<Resource<Course>> _updateResult = new MutableLiveData<>();
    public final LiveData<Resource<Course>> updateResult = _updateResult;

    public TeacherCourseViewModel(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    // ── Load khóa học của giảng viên ─────────────────────────
    public void loadMyCourses(Integer teacherId) {
        courseRepository.getCoursesByTeacherId(teacherId)
                .observeForever(result -> _myCourses.setValue(result));
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
}