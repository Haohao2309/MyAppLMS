package com.example.myapplms.ui.teacher;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.data.repository.TeacherRepository;

public class TeacherHomeViewModelFactory implements ViewModelProvider.Factory {

    private final TeacherRepository repository;

    public TeacherHomeViewModelFactory(TeacherRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TeacherHomeViewModel.class)) {
            return (T) new TeacherHomeViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
