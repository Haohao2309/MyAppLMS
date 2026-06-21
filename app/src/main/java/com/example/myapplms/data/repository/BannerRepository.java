package com.example.myapplms.data.repository;

import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.response.ApiResponse;
import com.example.myapplms.data.remote.dto.response.BannerResponse;
import com.example.myapplms.utils.Resource;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BannerRepository {

    public interface BannerCallback {
        void onResult(Resource<List<BannerResponse>> result);
    }

    private final LmsApiService apiService;

    public BannerRepository(LmsApiService apiService) {
        this.apiService = apiService;
    }

    /**
     * Gọi API lấy danh sách banner active và trả về qua callback.
     * Dùng trực tiếp trong ViewModel thay vì qua observeForever.
     */
    public void fetchActiveBanners(BannerCallback callback) {
        callback.onResult(Resource.loading());

        apiService.getActiveBanners().enqueue(new Callback<ApiResponse<List<BannerResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BannerResponse>>> call,
                                   Response<ApiResponse<List<BannerResponse>>> response) {
                android.util.Log.d("BANNER_DEBUG", "HTTP code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    List<BannerResponse> banners = response.body().getData();
                    android.util.Log.d("BANNER_DEBUG", "body OK, data: " + banners);
                    if (banners != null && !banners.isEmpty()) {
                        android.util.Log.d("BANNER_DEBUG", "Banner count: " + banners.size());
                        callback.onResult(Resource.success(banners));
                    } else {
                        android.util.Log.w("BANNER_DEBUG", "Banner list null or empty!");
                        callback.onResult(Resource.error("Banner trống", null));
                    }
                } else {
                    android.util.Log.e("BANNER_DEBUG", "Response not successful or body null");
                    callback.onResult(Resource.error("Lỗi server: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<BannerResponse>>> call, Throwable t) {
                android.util.Log.e("BANNER_DEBUG", "Network error: " + t.getMessage());
                callback.onResult(Resource.error("Mạng lỗi: " + t.getMessage(), null));
            }
        });
    }
}
