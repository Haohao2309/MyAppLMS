package com.example.myapplms.ui.community;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.data.repository.CommunityRepository;

public class CommunityViewModelFactory implements ViewModelProvider.Factory {
    private final CommunityRepository repository;

    public CommunityViewModelFactory(CommunityRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CommunityViewModel.class)) {
            return (T) new CommunityViewModel(repository);
        } else if (modelClass.isAssignableFrom(PostDetailViewModel.class)) {
            return (T) new PostDetailViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
