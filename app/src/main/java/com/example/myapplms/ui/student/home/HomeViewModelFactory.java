package com.example.myapplms.ui.student.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.data.repository.BannerRepository;
import com.example.myapplms.data.repository.CourseRepository;
import com.example.myapplms.data.repository.StudentRepository;

public class HomeViewModelFactory implements ViewModelProvider.Factory {
    private final CourseRepository repository;
    private final StudentRepository studentRepository;
    private final BannerRepository bannerRepository;

    public HomeViewModelFactory(CourseRepository repository,
                                StudentRepository studentRepository,
                                BannerRepository bannerRepository) {
        this.repository = repository;
        this.studentRepository = studentRepository;
        this.bannerRepository = bannerRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(repository, studentRepository, bannerRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}