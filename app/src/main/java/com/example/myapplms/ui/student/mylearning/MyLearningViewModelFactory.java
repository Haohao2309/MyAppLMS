package com.example.myapplms.ui.student.mylearning;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.data.repository.CourseRepository;

public class MyLearningViewModelFactory implements ViewModelProvider.Factory {
    private final CourseRepository repository;

    public MyLearningViewModelFactory(CourseRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MyLearningViewModel.class)) {
            return (T) new MyLearningViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
