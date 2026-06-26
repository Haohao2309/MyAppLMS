package com.example.myapplms.ui.student.course_detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplms.data.remote.dto.request.CreateReviewRequest;
import com.example.myapplms.data.remote.dto.request.VoteRequest;
import com.example.myapplms.data.remote.dto.response.EnrollmentStatusResponse;
import com.example.myapplms.data.remote.dto.response.PaymentCheckoutResponse;
import com.example.myapplms.data.remote.dto.response.ReviewResponse;
import com.example.myapplms.data.repository.CourseDetailRepository;
import com.example.myapplms.model.Course;
import com.example.myapplms.model.course_content.CourseContent;
import com.example.myapplms.utils.Resource;

import java.util.List;

public class CourseDetailViewModel extends ViewModel {

    private final CourseDetailRepository repository;

    private LiveData<Resource<Course>> courseDetailLiveData;
    private LiveData<Resource<CourseContent>> courseContentLiveData;

    public CourseDetailViewModel(CourseDetailRepository repository) {
        this.repository = repository;
    }

    /**
     * Cung cấp LiveData thông tin khóa học cho View (Overview Tab / Header)
     */
    public LiveData<Resource<Course>> getCourseDetail(int courseId) {
        if (courseDetailLiveData == null) {
            courseDetailLiveData = repository.getCourseDetail(courseId);
        }
        return courseDetailLiveData;
    }

    /**
     * Cung cấp LiveData nội dung chương trình học cho View (Curriculum Tab)
     */
    public LiveData<Resource<CourseContent>> getCourseContent(int courseId) {
        if (courseContentLiveData == null) {
            courseContentLiveData = repository.getCourseContent(courseId);
        }
        return courseContentLiveData;
    }

    /**
     * Làm mới dữ liệu khi người dùng kéo để refresh (SwipeRefreshLayout)
     */
    public void refreshData(int courseId) {
        courseDetailLiveData = repository.getCourseDetail(courseId);
        courseContentLiveData = repository.getCourseContent(courseId);
    }

    // Bổ sung hàm lấy Reviews
    public LiveData<Resource<List<ReviewResponse>>> getCourseReviews(int courseId) {
        return repository.getCourseReviews(courseId);
    }

    public LiveData<Resource<PaymentCheckoutResponse>> checkoutCourse(int courseId) {
        return repository.checkoutCourse(courseId);
    }

    // Bổ sung hàm kiểm tra trạng thái mua khóa học
    public LiveData<Resource<EnrollmentStatusResponse>> getEnrollmentStatus(int courseId) {
        return repository.getEnrollmentStatus(courseId);
    }

    // Bổ sung hàm gửi đánh giá mới
    public LiveData<Resource<ReviewResponse>> submitReview(int courseId, CreateReviewRequest request) {
        return repository.submitReview(courseId, request);
    }

    // Bổ sung hàm bình chọn đánh giá
    public LiveData<Resource<ReviewResponse>> voteReview(int courseId, String reviewId, VoteRequest request) {
        return repository.voteReview(courseId, reviewId, request);
    }
}