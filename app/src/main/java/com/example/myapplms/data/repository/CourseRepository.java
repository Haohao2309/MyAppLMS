package com.example.myapplms.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.request.CourseRequest;
import com.example.myapplms.data.remote.dto.response.CategoryResponse;
import com.example.myapplms.data.remote.dto.response.CourseResponse;
import com.example.myapplms.data.remote.dto.response.PagedResponse;
import com.example.myapplms.model.Course;
import com.example.myapplms.utils.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseRepository {

    private final LmsApiService apiService;

    public CourseRepository(LmsApiService apiService) {
        this.apiService = apiService;
    }

    public LiveData<Resource<List<Course>>> getCourses() {
        MutableLiveData<Resource<List<Course>>> result = new MutableLiveData<>();
        // Báo LOADING ngay lập tức
        result.postValue(Resource.loading());

        apiService.getCourses().enqueue(new Callback<List<CourseResponse>>() {
            @Override
            public void onResponse(Call<List<CourseResponse>> call,
                                   Response<List<CourseResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Course> domainList = new ArrayList<>();
                    for (CourseResponse dto : response.body()) {
                        domainList.add(Course.fromResponse(dto));
                    }
                    result.postValue(Resource.success(domainList));
                } else {
                    result.postValue(Resource.error("Lỗi server: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<List<CourseResponse>> call, Throwable t) {
                result.postValue(Resource.error("Không có kết nối mạng: " + t.getMessage(), null));
            }
        });

        return result;
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
    public LiveData<Resource<List<Course>>> getCoursesByTeacherId(Integer teacherId) {
        MutableLiveData<Resource<List<Course>>> result = new MutableLiveData<>();
        // Báo LOADING ngay lập tức
        result.postValue(Resource.loading());

        apiService.getExploreCoursesTea().enqueue(new Callback<List<CourseResponse>>() {
            @Override
            public void onResponse(Call<List<CourseResponse>> call,
                                   Response<List<CourseResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Course> domainList = new ArrayList<>();
                    for (CourseResponse dto : response.body()) {
                        domainList.add(Course.fromResponse(dto));
                    }
                    result.postValue(Resource.success(domainList));
                } else {
                    result.postValue(Resource.error("Lỗi server: " + response.code(), null));
                }
            }

            // ĐÃ THÊM: onFailure bị thiếu
            @Override
            public void onFailure(Call<List<CourseResponse>> call, Throwable t) {
                result.postValue(Resource.error("Không có kết nối mạng: " + t.getMessage(), null));
            }
        }); // ĐÃ THÊM: Đóng hàm enqueue đúng chỗ

        return result;
    }

    // ── Tạo khóa học mới ─────────────────────────────────────
    public LiveData<Resource<Course>> createCourse(CourseRequest request) {
        MutableLiveData<Resource<Course>> result = new MutableLiveData<>();
        result.postValue(Resource.loading());

        apiService.createCourse(request).enqueue(new Callback<CourseResponse>() {
            @Override
            public void onResponse(Call<CourseResponse> call, Response<CourseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(Resource.success(Course.fromResponse(response.body())));
                } else {
                    result.postValue(Resource.error("Lỗi server: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<CourseResponse> call, Throwable t) {
                result.postValue(Resource.error("Không có kết nối mạng", null));
            }
        });
        return result;
    }

    // ── Cập nhật khóa học ────────────────────────────────────
    public LiveData<Resource<Course>> updateCourse(Integer courseId, CourseRequest request) {
        MutableLiveData<Resource<Course>> result = new MutableLiveData<>();
        result.postValue(Resource.loading());

        apiService.updateCourse(courseId, request).enqueue(new Callback<CourseResponse>() {
            @Override
            public void onResponse(Call<CourseResponse> call, Response<CourseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(Resource.success(Course.fromResponse(response.body())));
                } else {
                    result.postValue(Resource.error("Lỗi server: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<CourseResponse> call, Throwable t) {
                result.postValue(Resource.error("Không có kết nối mạng", null));
            }
        });

        return result;
    }

    // ── Phân trang (server-side) ─────────────────────────────────────────────

    /**
     * Lấy phân trang khóa học explore cho student (8 bản ghi/trang).
     * Trả về Resource đồng bộ để dùng trong ExecutorService.
     */
    public com.example.myapplms.utils.Resource<PagedResponse<CourseResponse>> getExploreCoursesPagedStudent(int page, int size) {
        try {
            retrofit2.Response<PagedResponse<CourseResponse>> response =
                    apiService.getExploreCoursesPagedStudent(page, size).execute();
            if (response.isSuccessful() && response.body() != null) {
                return com.example.myapplms.utils.Resource.success(response.body());
            }
            return com.example.myapplms.utils.Resource.error("Lỗi: " + response.code(), null);
        } catch (java.io.IOException e) {
            return com.example.myapplms.utils.Resource.error("Không có kết nối mạng", null);
        }
    }

    /**
     * Lấy phân trang khóa học explore cho teacher (8 bản ghi/trang).
     */
    public com.example.myapplms.utils.Resource<PagedResponse<CourseResponse>> getExploreCoursesPagedTeacher(int page, int size) {
        try {
            retrofit2.Response<PagedResponse<CourseResponse>> response =
                    apiService.getExploreCoursesPagedTeacher(page, size).execute();
            if (response.isSuccessful() && response.body() != null) {
                return com.example.myapplms.utils.Resource.success(response.body());
            }
            return com.example.myapplms.utils.Resource.error("Lỗi: " + response.code(), null);
        } catch (java.io.IOException e) {
            return com.example.myapplms.utils.Resource.error("Không có kết nối mạng", null);
        }
    }
}