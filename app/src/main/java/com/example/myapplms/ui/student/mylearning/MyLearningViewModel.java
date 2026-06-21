package com.example.myapplms.ui.student.mylearning;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplms.data.remote.dto.response.DashboardResponse;
import com.example.myapplms.data.repository.CourseRepository;
import com.example.myapplms.model.Course;
import com.example.myapplms.utils.Resource;

import java.util.ArrayList;
import java.util.List;

public class MyLearningViewModel extends ViewModel {

    private final CourseRepository courseRepository;
    private final MutableLiveData<Resource<List<Course>>> _myCourses = new MutableLiveData<>();
    public final LiveData<Resource<List<Course>>> myCourses = _myCourses;

    public MyLearningViewModel(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public void loadMyCourses() {
        courseRepository.getStudentDashboard().observeForever(result -> {
            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                DashboardResponse data = result.data;
                List<com.example.myapplms.data.remote.dto.response.CourseResponse> continueCourses = 
                    data.continueLearningCourses != null ? data.continueLearningCourses : data.continueLearning;
                
                List<Course> domainList = new ArrayList<>();
                if (continueCourses != null) {
                    for (com.example.myapplms.data.remote.dto.response.CourseResponse res : continueCourses) {
                        domainList.add(Course.fromResponse(res));
                    }
                }
                _myCourses.setValue(Resource.success(domainList));
            } else if (result.status == Resource.Status.ERROR) {
                _myCourses.setValue(Resource.error(result.message, null));
            } else if (result.status == Resource.Status.LOADING) {
                _myCourses.setValue(Resource.loading());
            }
        });
    }
}
