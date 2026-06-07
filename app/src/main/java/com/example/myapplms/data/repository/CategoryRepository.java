package com.example.myapplms.data.repository;


import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.response.CategoryResponse;
import com.example.myapplms.utils.Resource;
import java.io.IOException;
import java.util.List;
import retrofit2.Response;

public class CategoryRepository {

    private final LmsApiService apiService;

    public CategoryRepository(LmsApiService apiService) {
        this.apiService = apiService;
    }

    public Resource<List<CategoryResponse>> getCategories() {
        try {
            Response<List<CategoryResponse>> response = apiService.getCategories().execute();

            if (response.isSuccessful() && response.body() != null) {
                return Resource.success(response.body());
            }
            return Resource.error("Lỗi: " + response.code(), null);

        } catch (IOException e) {
            return Resource.error("Không có kết nối mạng", null);
        }
    }
}