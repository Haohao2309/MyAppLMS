package com.example.myapplms.ui.explore;


import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.data.repository.CourseRepository;

public class TeacherCourseViewModelFactory implements ViewModelProvider.Factory {

    private final CourseRepository repository;

    public TeacherCourseViewModelFactory(CourseRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TeacherCourseViewModel.class)) {
            return (T) new TeacherCourseViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel: " + modelClass.getName());
    }
}