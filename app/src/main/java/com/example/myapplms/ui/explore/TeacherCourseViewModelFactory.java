package com.example.myapplms.ui.explore;


import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.data.repository.CourseRepository;
import com.example.myapplms.data.repository.MediaRepository;

public class TeacherCourseViewModelFactory implements ViewModelProvider.Factory {

    private final CourseRepository courseRepository;
    private final MediaRepository mediaRepository;  // thêm

    public TeacherCourseViewModelFactory(CourseRepository courseRepository,
                                         MediaRepository mediaRepository) {
        this.courseRepository = courseRepository;
        this.mediaRepository  = mediaRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TeacherCourseViewModel.class)) {
            return (T) new TeacherCourseViewModel(courseRepository, mediaRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel: " + modelClass.getName());
    }
}