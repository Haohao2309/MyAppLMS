package com.example.myapplms.ui.teacher.grading;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.data.repository.GradingRepository;

public class GradingViewModelFactory implements ViewModelProvider.Factory {

    private final GradingRepository repository;

    public GradingViewModelFactory(GradingRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(GradingViewModel.class)) {
            return (T) new GradingViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel: " + modelClass.getName());
    }
}