package com.example.myapplms.ui.course;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.myapplms.data.remote.dto.response.CategoryResponse;
import com.example.myapplms.data.repository.CategoryRepository;
import com.example.myapplms.utils.Resource;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryViewModel extends ViewModel {

    private final CategoryRepository categoryRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<Resource<List<CategoryResponse>>> _categories = new MutableLiveData<>();
    public final LiveData<Resource<List<CategoryResponse>>> categories = _categories;

    public CategoryViewModel(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void loadCategories() {
        _categories.setValue(Resource.loading());
        executor.execute(() -> {
            Resource<List<CategoryResponse>> result = categoryRepository.getCategories();
            _categories.postValue(result);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}