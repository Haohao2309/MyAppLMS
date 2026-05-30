// ui/explore/ExploreViewModelFactory.java
package com.example.myapplms.ui.explore;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.data.repository.CourseRepository;

public class ExploreViewModelFactory implements ViewModelProvider.Factory {
    private final CourseRepository repository;

    public ExploreViewModelFactory(CourseRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ExploreViewModel(repository);
    }
}