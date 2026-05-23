package com.example.myapplms.ui.explore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplms.data.repository.CourseRepository;
import com.example.myapplms.model.Course;
import com.example.myapplms.utils.Resource;

import java.util.List;

public class ExploreViewModel extends ViewModel {

    private final CourseRepository courseRepository;

    // ── QUAN TRỌNG: dùng MutableLiveData cố định, không gán lại ──
    private final MutableLiveData<Resource<List<Course>>> _courses = new MutableLiveData<>();
    public final LiveData<Resource<List<Course>>> courses = _courses;

    public ExploreViewModel(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }


    public void loadCourses() {
        // Observe LiveData từ Repository rồi forward vào _courses
        // Không gán lại _courses = ... vì Fragment đang observe đối tượng này rồi
        courseRepository.getCourses().observeForever(result -> _courses.setValue(result));
    }
    public void loadCourses(Integer teacherId) {
        // Observe LiveData từ Repository rồi forward vào _courses
        // Không gán lại _courses = ... vì Fragment đang observe đối tượng này rồi
        courseRepository.getCoursesByTeacherId(teacherId).observeForever(result -> _courses.setValue(result));
    }

    // Giữ lại getter cũ để không phải sửa Fragment
    public LiveData<Resource<List<Course>>> getCourses() {
        return _courses;
    }
    public LiveData<Resource<List<Course>>> getCoursesByTeacherId(Integer teacherId) {
        return _courses;
    }


}