package com.example.myapplms.ui.student.learning;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.data.repository.LearningRepository;

public class LearningViewModelFactory implements ViewModelProvider.Factory {
    private final LearningRepository repository;

    public LearningViewModelFactory(LearningRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new LearningViewModel(repository);
    }
}