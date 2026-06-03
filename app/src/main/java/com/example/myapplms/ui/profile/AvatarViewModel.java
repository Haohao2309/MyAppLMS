package com.example.myapplms.ui.profile;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplms.data.repository.AvatarRepository;
import com.example.myapplms.utils.Resource;

import java.io.File;

public class AvatarViewModel extends ViewModel {

    private final AvatarRepository repository;

    private final MutableLiveData<Resource<String>> _uploadResult = new MutableLiveData<>();
    public final LiveData<Resource<String>> uploadResult = _uploadResult;

    public AvatarViewModel(AvatarRepository repository) {
        this.repository = repository;
    }

    public void uploadAvatar(File imageFile) {
        repository.uploadAvatar(imageFile)
                .observeForever(result -> _uploadResult.setValue(result));
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final AvatarRepository repository;

        public Factory(AvatarRepository repository) {
            this.repository = repository;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new AvatarViewModel(repository);
        }
    }
}
