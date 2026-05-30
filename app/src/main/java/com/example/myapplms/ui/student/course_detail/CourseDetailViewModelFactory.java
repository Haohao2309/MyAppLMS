package com.example.myapplms.ui.student.course_detail;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.data.repository.CourseDetailRepository;

public class CourseDetailViewModelFactory implements ViewModelProvider.Factory {

    private final CourseDetailRepository repository;

    public CourseDetailViewModelFactory(CourseDetailRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CourseDetailViewModel.class)) {
            return (T) new CourseDetailViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}