package com.example.myapplms.ui.course;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplms.data.repository.CategoryRepository;

public class CategoryViewModelFactory implements ViewModelProvider.Factory {
    private final CategoryRepository repository;

    public CategoryViewModelFactory(CategoryRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new CategoryViewModel(repository);
    }
}