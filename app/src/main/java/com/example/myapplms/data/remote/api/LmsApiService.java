package com.example.myapplms.data.remote.api;

import com.example.myapplms.data.remote.dto.request.CourseRequest;
import com.example.myapplms.data.remote.dto.request.CreateCommentRequest;
import com.example.myapplms.data.remote.dto.request.CreatePostRequest;
import com.example.myapplms.data.remote.dto.request.CreateReviewRequest;
import com.example.myapplms.data.remote.dto.request.GradeSubmissionRequest;
import com.example.myapplms.data.remote.dto.request.LoginRequest;
import com.example.myapplms.data.remote.dto.request.PaymentCheckoutRequest;
import com.example.myapplms.data.remote.dto.request.PaymentWebhookRequest;
import com.example.myapplms.data.remote.dto.request.RefreshTokenRequest;
import com.example.myapplms.data.remote.dto.request.SubmitAssignmentRequest;
import com.example.myapplms.data.remote.dto.request.SubmitGradeRequest;
import com.example.myapplms.data.remote.dto.request.SubmitQuizRequest;
import com.example.myapplms.data.remote.dto.request.SyncVideoRequest;
import com.example.myapplms.data.remote.dto.request.VoteRequest;
import com.example.myapplms.data.remote.dto.request.RegisterRequest;
import com.example.myapplms.data.remote.dto.request.StudentRequest;
import com.example.myapplms.data.remote.dto.request.TeacherRequest;
import com.example.myapplms.data.remote.dto.response.ApiResponse;
import com.example.myapplms.data.remote.dto.response.AuthResponse;
import com.example.myapplms.data.remote.dto.response.CategoryResponse;
import com.example.myapplms.data.remote.dto.response.DiscussionResponse;
import com.example.myapplms.data.remote.dto.response.EnrollmentStatusResponse;
import com.example.myapplms.data.remote.dto.response.GradingListResponse;
import com.example.myapplms.data.remote.dto.response.NotificationResponse;
import com.example.myapplms.data.remote.dto.response.PaymentCheckoutResponse;
import com.example.myapplms.data.remote.dto.response.PaymentWebhookResponse;
import com.example.myapplms.data.remote.dto.response.SubmissionResponse;
import com.example.myapplms.data.remote.dto.response.TeacherStatsResponse;
import com.example.myapplms.data.remote.dto.response.DashboardOverviewResponse;
import com.example.myapplms.data.remote.dto.response.PagedResponse;
import com.example.myapplms.data.remote.dto.response.RecentActivityResponse;
import com.example.myapplms.data.remote.dto.response.WeeklyActivityResponse;
import com.example.myapplms.data.remote.dto.response.TaskItemResponse;
import com.example.myapplms.data.remote.dto.response.TeacherTaskResponse;
import com.example.myapplms.data.remote.dto.response.UserResponse;
import com.example.myapplms.data.remote.dto.response.ProgressResponse;
import com.example.myapplms.data.remote.dto.response.community_response.CommentResponse;
import com.example.myapplms.data.remote.dto.response.community_response.CommunityActionResponse;
import com.example.myapplms.data.remote.dto.response.community_response.CommunityStatsResponse;
import com.example.myapplms.data.remote.dto.response.community_response.PostDetailResponse;
import com.example.myapplms.data.remote.dto.response.community_response.PostResponse;

import java.util.List;
import com.example.myapplms.data.remote.dto.response.CourseResponse;
import com.example.myapplms.data.remote.dto.response.ReviewResponse;
import com.example.myapplms.data.remote.dto.response.StudentResponse;
import com.example.myapplms.data.remote.dto.response.TeacherResponse;
import com.example.myapplms.data.remote.dto.response.course_content.CourseContentResponse;
import com.example.myapplms.model.Course;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LmsApiService {

    // Trong file LmsApiService.java sửa lại như thế này:
    @POST("auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);
    @POST("auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequest request);

    @POST("auth/refresh")
    Call<ApiResponse<AuthResponse>> refreshToken(@Body RefreshTokenRequest request);

    @POST("auth/logout")
    Call<Void> logout(@Body RefreshTokenRequest request);

    // ── Community API ───────────────────────────────────────

    @GET("community/posts")
    Call<List<PostResponse>> getPosts(
            @Query("category") String category,
            @Query("q") String query,
            @Query("sortBy") String sortBy,
            @Query("page") Integer page,
            @Query("size") Integer size
    );

    @GET("community/posts/{id}")
    Call<PostDetailResponse> getPostDetail(@Path("id") String id);

    @POST("community/posts")
    Call<PostResponse> createPost(
            @Query("category") String category,
            @Body CreatePostRequest request
    );

    @DELETE("community/posts/{id}")
    Call<CommunityActionResponse> deletePost(@Path("id") String postId);

    @POST("community/posts/{id}/like")
    Call<CommunityActionResponse> toggleLike(@Path("id") String postId);

    @POST("community/posts/{id}/comments")
    Call<CommentResponse> addComment(
            @Path("id") String postId,
            @Body CreateCommentRequest request
    );

    @DELETE("community/posts/{postId}/comments/{commentId}")
    Call<CommunityActionResponse> deleteComment(
            @Path("postId") String postId,
            @Path("commentId") String commentId
    );

    @PUT("community/posts/{postId}/comments/{commentId}")
    Call<CommentResponse> updateComment(
            @Path("postId") String postId,
            @Path("commentId") String commentId,
            @Body CreateCommentRequest request
    );

    @PUT("community/posts/{id}")
    Call<PostResponse> updatePost(
            @Path("id") String postId,
            @Body CreatePostRequest request
    );

    @GET("community/posts/stats")
    Call<CommunityStatsResponse> getCommunityStats();

    // 🌟 Tìm đến hàm togglePin trong file LmsApiService.java và sửa lại thành:
    @POST("api/community/posts/{postId}/pin")
    Call<PostResponse> togglePin(
            @Path("postId") String postId
    );
    @GET("teachers")
    Call<List<TeacherResponse>> getTeachers();
    @GET("teachers/{id}")
    Call<TeacherResponse> getTeacherbyId(@Path("id") Integer id); // Bắt buộc phải có @Path("id")

    @PUT("teachers/{id}")
    Call<TeacherResponse> updateTeacher(@Path("id") Integer id, @Body TeacherRequest request);

    // Payment
    @POST("payments/checkout")
    Call<PaymentCheckoutResponse> checkout(@Body PaymentCheckoutRequest request);

    @POST("payments/webhook")
    Call<PaymentWebhookResponse> paymentWebhook(@Body PaymentWebhookRequest request);

    @GET("notifications/my-notifications")
    Call<List<NotificationResponse>> getMyNotifications();

    @PATCH("notifications/{id}/read")
    Call<Void> markAsRead(@Path("id") String id);

    @GET("students/{id}")
    Call<StudentResponse> getStudentById(@Path("id") Integer userId);

    /** PUT cập nhật thông tin sinh viên theo studentId */
    @PUT("students/{id}")
    Call<StudentResponse> updateStudent(@Path("id") Integer studentId,
                                        @Body StudentRequest request);

    @GET("v1/courses/explore")
    Call<List<CourseResponse>> getCourses();

    @GET("v1/courses/teacher/{id}")
    Call<List<CourseResponse>> getCoursesByTeacherId(@Path("id") Integer id);

    @GET("v1/courses/{id}")
    Call<CourseResponse> getCourseById(@Path("id") int id);

    @GET("v1/courses/explore/me")
    Call<List<CourseResponse>> getExploreCoursesTea();

    // ── Endpoints phân trang (server-side) ─────────────────────────────────

    /** Phân trang khóa học explore (student) */
    @GET("v1/courses/explore/paged")
    Call<PagedResponse<CourseResponse>> getExploreCoursesPagedStudent(
            @Query("page") int page,
            @Query("size") int size,
            @Query("search") String search,
            @Query("category") String category,
            @Query("price") String price,
            @Query("rating") String rating
    );

    /** Phân trang khóa học explore (teacher — dùng JWT để lấy teacherId) */
    @GET("v1/courses/explore/me/paged")
    Call<PagedResponse<CourseResponse>> getExploreCoursesPagedTeacher(
            @Query("page") int page,
            @Query("size") int size,
            @Query("search") String search,
            @Query("category") String category,
            @Query("price") String price,
            @Query("rating") String rating
    );

    @GET("v1/courses/{id}/content")
    Call<CourseContentResponse> getCourseContent(@Path("id") int id);

    // Lấy danh sách review
    @GET("courses/{courseId}/reviews")
    Call<List<ReviewResponse>> getCourseReviews(@Path("courseId") int courseId);

    // Vote review (Để sẵn cho tính năng vote sau này)
    @PUT("courses/{courseId}/reviews/{reviewId}/vote")
    Call<ReviewResponse> voteReview(@Path("courseId") int courseId, @Path("reviewId") String reviewId, @Body VoteRequest request);

    // MỚI: Gửi đánh giá mới — BE tự lấy studentId từ JWT, không gửi từ client
    @POST("courses/{courseId}/reviews")
    Call<ReviewResponse> createReview(@Path("courseId") int courseId, @Body CreateReviewRequest request);

    // MỚI: Check trạng thái mua khóa học (enrolled + enrollmentId) — GET thuần,
    // không tạo Payment/PayOS như checkout(). Dùng để hiện/ẩn form review,
    // và để biết enrollmentId trước khi cho phép review.
    @GET("courses/{courseId}/enrollment-status")
    Call<EnrollmentStatusResponse> getEnrollmentStatus(@Path("courseId") int courseId);
    @POST("v1/courses")
    Call<CourseResponse> createCourse(@Body CourseRequest request);

    @PUT("v1/courses/{id}")
    Call<CourseResponse> updateCourse(@Path("id") Integer id, @Body CourseRequest request);

    @DELETE("v1/courses/{id}")
    Call<String> deleteCourse(@Path("id") Integer id,
                              @Query("deletedBy") String deletedBy,
                              @Query("reason") String reason);
    // data/remote/LmsApiService.java
    @GET("v1/categories")
    Call<List<CategoryResponse>> getCategories();
    // --- LEARNING WORKSPACE ---
    @GET("v1/learn/courses/{courseId}/progress")
    Call<ProgressResponse> getProgress(@Path("courseId") int courseId);

    @PUT("v1/learn/courses/{courseId}/lessons/{lessonId}/sync")
    Call<Void> syncVideoProgress(@Path("courseId") int courseId, @Path("lessonId") String lessonId, @Body SyncVideoRequest request);

    // Upload ảnh — multipart
    @Multipart
    @POST("upload/avatar")
    Call<UserResponse> uploadAvatar(@Part MultipartBody.Part file);

    @Multipart
    @POST("upload/course-thumbnail")   // chỉnh lại đúng endpoint của bạn
    Call<CourseResponse> uploadCourseImage(@Part MultipartBody.Part file);

    // API Lấy dữ liệu Dashboard cho Giáo viên (legacy)
    @GET("teachers/{id}/dashboard")
    Call<TeacherStatsResponse> getTeacherDashboardStats(@Path("id") Integer teacherId);

    // ── Teacher Dashboard v2 ──────────────────────────────────
    @GET("teacher-dashboard/overview")
    Call<DashboardOverviewResponse> getTeacherOverview();

    @GET("teacher-dashboard/recent-activities")
    Call<List<RecentActivityResponse>> getRecentActivities();

    /** Phân trang hoạt động gần đây (server-side) */
    @GET("teacher-dashboard/recent-activities/paged")
    Call<PagedResponse<RecentActivityResponse>> getRecentActivitiesPaged(
            @Query("page") int page,
            @Query("size") int size
    );

    /** Phân trang khoá học của teacher (server-side) */
    @GET("teacher-dashboard/my-courses/paged")
    Call<PagedResponse<CourseResponse>> getMyCoursesPagedTeacher(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("teacher-dashboard/weekly-activity")
    Call<WeeklyActivityResponse> getWeeklyActivity();

    @GET("teacher-dashboard/tasks")
    Call<TeacherTaskResponse> getTasks();


    @POST("v1/learn/courses/{courseId}/lessons/{lessonId}/submit-quiz")
    Call<ProgressResponse> submitQuiz(@Path("courseId") int courseId, @Path("lessonId") String lessonId, @Body SubmitQuizRequest request);

    @POST("v1/learn/courses/{courseId}/lessons/{lessonId}/submit-assignment")
    Call<ProgressResponse> submitAssignment(@Path("courseId") int courseId, @Path("lessonId") String lessonId, @Body SubmitAssignmentRequest request);

    @GET("v1/learn/courses/{courseId}/lessons/{lessonId}/discussions")
    Call<List<com.example.myapplms.data.remote.dto.response.DiscussionResponse>> getDiscussions(
            @Path("courseId") int courseId,
            @Path("lessonId") String lessonId
    );

    @POST("v1/learn/courses/{courseId}/lessons/{lessonId}/discussions")
    Call<DiscussionResponse> createDiscussion(
            @Path("courseId") int courseId,
            @Path("lessonId") String lessonId,
            @Body com.example.myapplms.data.remote.dto.request.CreateDiscussionRequest request
    );
// Thêm vào cuối interface LmsApiService.java (trước dấu } cuối cùng)

// ── Grading (Teacher) ──────────────────────────────────────────────────────

    /** Danh sách sinh viên + trạng thái chấm điểm theo khóa học */
    @GET("v1/course-grades/grading/{courseId}")
    Call<GradingListResponse> getGradingList(@Path("courseId") int courseId);

    /** Giáo viên nhập examScore → BE tính quizAvg, finalScore, gradeLevel → ghi CourseGrade */
    @POST("v1/course-grades/grading/submit")
    Call<GradingListResponse.StudentGradingItem> submitGrade(@Body SubmitGradeRequest request);

}


