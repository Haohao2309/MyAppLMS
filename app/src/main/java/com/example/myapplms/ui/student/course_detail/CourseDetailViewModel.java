package com.example.myapplms.ui.student.course_detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplms.data.repository.CourseDetailRepository;
import com.example.myapplms.model.Course;
import com.example.myapplms.model.course_content.CourseContent;
import com.example.myapplms.utils.Resource;

public class CourseDetailViewModel extends ViewModel {

    private final CourseDetailRepository repository;

    private LiveData<Resource<Course>> courseDetailLiveData;
    private LiveData<Resource<CourseContent>> courseContentLiveData;

    public CourseDetailViewModel(CourseDetailRepository repository) {
        this.repository = repository;
    }

    /**
     * Cung cấp LiveData thông tin khóa học cho View (Overview Tab / Header)
     */
    public LiveData<Resource<Course>> getCourseDetail(int courseId) {
        if (courseDetailLiveData == null) {
            courseDetailLiveData = repository.getCourseDetail(courseId);
        }
        return courseDetailLiveData;
    }

    /**
     * Cung cấp LiveData nội dung chương trình học cho View (Curriculum Tab)
     */
    public LiveData<Resource<CourseContent>> getCourseContent(int courseId) {
        if (courseContentLiveData == null) {
            courseContentLiveData = repository.getCourseContent(courseId);
        }
        return courseContentLiveData;
    }

    /**
     * Làm mới dữ liệu khi người dùng kéo để refresh (SwipeRefreshLayout)
     */
    public void refreshData(int courseId) {
        courseDetailLiveData = repository.getCourseDetail(courseId);
        courseContentLiveData = repository.getCourseContent(courseId);
    }
}