package com.example.myapplms.ui.teacher;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.data.repository.TeacherRepository;

public class TeacherViewModelFactory implements ViewModelProvider.Factory {
    private final TeacherRepository repository;

    public TeacherViewModelFactory(TeacherRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TeacherViewModel.class)) {
            return (T) new TeacherViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
