package com.example.myapplms.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplms.data.remote.api.LmsApiService;
import com.example.myapplms.data.remote.dto.request.CreateReviewRequest;
import com.example.myapplms.data.remote.dto.request.PaymentCheckoutRequest;
import com.example.myapplms.data.remote.dto.request.VoteRequest;
import com.example.myapplms.data.remote.dto.response.CourseResponse;
import com.example.myapplms.data.remote.dto.response.EnrollmentStatusResponse;
import com.example.myapplms.data.remote.dto.response.PaymentCheckoutResponse;
import com.example.myapplms.data.remote.dto.response.ReviewResponse;
import com.example.myapplms.data.remote.dto.response.course_content.CourseContentResponse;
import com.example.myapplms.model.Course;
import com.example.myapplms.model.course_content.CourseContent;
import com.example.myapplms.utils.Resource;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseDetailRepository {

    private final LmsApiService apiService;

    public CourseDetailRepository(LmsApiService apiService) {

        this.apiService = apiService;
    }

    /**
     * Lấy thông tin cơ bản của khóa học từ PostgreSQL
     */
    public LiveData<Resource<Course>> getCourseDetail(int courseId) {
        MutableLiveData<Resource<Course>> result = new MutableLiveData<>();
        result.postValue(Resource.loading());

        apiService.getCourseById(courseId).enqueue(new Callback<CourseResponse>() {
            @Override
            public void onResponse(Call<CourseResponse> call, Response<CourseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Course domainCourse = Course.fromResponse(response.body());
                    result.postValue(Resource.success(domainCourse));
                } else {
                    result.postValue(Resource.error("Lỗi server: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<CourseResponse> call, Throwable t) {
                result.postValue(Resource.error("Không có kết nối mạng: " + t.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Lấy chi tiết nội dung phân cấp (Modules, Lessons) từ MongoDB
     */
    public LiveData<Resource<CourseContent>> getCourseContent(int courseId) {
        MutableLiveData<Resource<CourseContent>> result = new MutableLiveData<>();
        result.postValue(Resource.loading());

        apiService.getCourseContent(courseId).enqueue(new Callback<CourseContentResponse>() {
            @Override
            public void onResponse(Call<CourseContentResponse> call, Response<CourseContentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CourseContent domainContent = CourseContent.fromResponse(response.body());
                    result.postValue(Resource.success(domainContent));
                } else {
                    result.postValue(Resource.error("Lỗi server: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<CourseContentResponse> call, Throwable t) {
                result.postValue(Resource.error("Không có kết nối mạng: " + t.getMessage(), null));
            }
        });

        return result;
    }

    // API Lấy Reviews
    public LiveData<Resource<List<ReviewResponse>>> getCourseReviews(int courseId) {
        MutableLiveData<Resource<List<ReviewResponse>>> result = new MutableLiveData<>();
        result.postValue(Resource.loading());

        apiService.getCourseReviews(courseId).enqueue(new Callback<List<ReviewResponse>>() {
            @Override
            public void onResponse(Call<List<ReviewResponse>> call, Response<List<ReviewResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(Resource.success(response.body()));
                } else {
                    result.postValue(Resource.error("Lỗi server: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<List<ReviewResponse>> call, Throwable t) {
                result.postValue(Resource.error("Không có kết nối mạng: " + t.getMessage(), null));
            }
        });
        return result;
    }

    public LiveData<Resource<PaymentCheckoutResponse>> checkoutCourse(int courseId) {
        MutableLiveData<Resource<PaymentCheckoutResponse>> result = new MutableLiveData<>();
        result.postValue(Resource.loading());

        PaymentCheckoutRequest request = new PaymentCheckoutRequest(courseId);
        apiService.checkout(request).enqueue(new Callback<PaymentCheckoutResponse>() {
            @Override
            public void onResponse(Call<PaymentCheckoutResponse> call, Response<PaymentCheckoutResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(Resource.success(response.body()));
                } else {
                    result.postValue(Resource.error("Lỗi tạo hóa đơn: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<PaymentCheckoutResponse> call, Throwable t) {
                result.postValue(Resource.error("Lỗi mạng: " + t.getMessage(), null));
            }
        });
        return result;
    }

    /**
     * Check trạng thái mua khóa học — GET thuần, không side-effect, không
     * tạo Payment/PayOS như checkoutCourse(). Dùng để hiện/ẩn form viết
     * đánh giá và biết enrollmentId hợp lệ.
     */
    public LiveData<Resource<EnrollmentStatusResponse>> getEnrollmentStatus(int courseId) {
        MutableLiveData<Resource<EnrollmentStatusResponse>> result = new MutableLiveData<>();
        result.postValue(Resource.loading());

        apiService.getEnrollmentStatus(courseId).enqueue(new Callback<EnrollmentStatusResponse>() {
            @Override
            public void onResponse(Call<EnrollmentStatusResponse> call, Response<EnrollmentStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(Resource.success(response.body()));
                } else {
                    result.postValue(Resource.error("Lỗi kiểm tra trạng thái mua khóa học: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<EnrollmentStatusResponse> call, Throwable t) {
                result.postValue(Resource.error("Lỗi mạng: " + t.getMessage(), null));
            }
        });
        return result;
    }

    /**
     * Gửi đánh giá mới. BE tự lấy studentId từ JWT và validate enrollment
     * Active — không cần Android gửi enrollmentId/studentId.
     */
    public LiveData<Resource<ReviewResponse>> submitReview(int courseId, CreateReviewRequest request) {
        MutableLiveData<Resource<ReviewResponse>> result = new MutableLiveData<>();
        result.postValue(Resource.loading());

        apiService.createReview(courseId, request).enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(Resource.success(response.body()));
                } else {
                    result.postValue(Resource.error("Gửi đánh giá thất bại: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {
                result.postValue(Resource.error("Lỗi mạng: " + t.getMessage(), null));
            }
        });
        return result;
    }

    public LiveData<Resource<ReviewResponse>> voteReview(int courseId, String reviewId, VoteRequest request) {
        MutableLiveData<Resource<ReviewResponse>> result = new MutableLiveData<>();
        result.postValue(Resource.loading());

        apiService.voteReview(courseId, reviewId, request).enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(Resource.success(response.body()));
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorBody = "";
                    }
                    result.postValue(Resource.error("Bình chọn thất bại: " + response.code() + " — " + errorBody, null));
                }
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {
                result.postValue(Resource.error("Lỗi mạng: " + t.getMessage(), null));
            }
        });
        return result;
    }
}