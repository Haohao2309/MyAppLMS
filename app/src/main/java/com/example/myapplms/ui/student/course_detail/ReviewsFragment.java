package com.example.myapplms.ui.student.course_detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplms.R;
import com.example.myapplms.utils.Resource;
import com.example.myapplms.data.remote.dto.request.CreateReviewRequest;
import com.example.myapplms.data.remote.dto.response.ReviewResponse;
import com.example.myapplms.ui.student.course_detail.adapter.ReviewAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewsFragment extends Fragment {

    private CourseDetailViewModel sharedViewModel;

    // Core views
    private RecyclerView rvReviews;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState;

    // Rating summary views
    private TextView tvAverageRating;
    private TextView tvAverageRatingStars;
    private TextView tvReviewCount;
    private ProgressBar bar5, bar4, bar3, bar2, bar1;
    private TextView pct5, pct4, pct3, pct2, pct1;

    // Write review views
    private Button btnWriteReview;
    private LinearLayout layoutWriteForm;
    private RatingBar ratingInput;
    private EditText etTitle, etContent, etPros, etCons;
    private Button btnCancelReview, btnSubmitReview;

    // Cache local và cơ chế chống double click
    private List<ReviewResponse> localReviewsList;
    private final List<String> pendingVoteReviewIds = new ArrayList<>();
    private final List<String> upvotedReviewIds = new ArrayList<>();
    private final List<String> downvotedReviewIds = new ArrayList<>();
    private final Map<String, Long> lastVoteTimeMap = new HashMap<>();
    private static final long VOTE_COOLDOWN_MS = 2000; // 2 giây chặn click
    private LiveData<Resource<List<ReviewResponse>>> activeReviewsLiveData;
    public static ReviewsFragment newInstance(int courseId) {
        ReviewsFragment f = new ReviewsFragment();
        Bundle args = new Bundle();
        args.putInt("COURSE_ID", courseId);
        f.setArguments(args);
        return f;
    }

    private final androidx.lifecycle.Observer<Resource<List<ReviewResponse>>> reviewsObserver = resource -> {
        if (resource == null) return;
        switch (resource.status) {
            case LOADING:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case SUCCESS:
                progressBar.setVisibility(View.GONE);
                if (resource.data != null && !resource.data.isEmpty()) {
                    layoutEmptyState.setVisibility(View.GONE);
                    rvReviews.setVisibility(View.VISIBLE);
                    this.localReviewsList = new ArrayList<>(resource.data);

                    // Đánh dấu trạng thái vote từ server (votedBy) + SharedPreferences
                    initVoteStateFromServer(resource.data);

                    setupAdapter();
                    updateRatingSummary(this.localReviewsList);
                } else {
                    rvReviews.setVisibility(View.GONE);
                    layoutEmptyState.setVisibility(View.VISIBLE);
                    updateRatingSummary(new ArrayList<>());
                }
                break;
            case ERROR:
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi tải đánh giá: " + resource.message, Toast.LENGTH_SHORT).show();
                break;
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reviews, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));

        // Hứng ViewModel dùng chung từ Activity cha
        sharedViewModel = new ViewModelProvider(requireActivity()).get(CourseDetailViewModel.class);

        int courseId = requireActivity().getIntent().getIntExtra("COURSE_ID", -1);
        if (courseId != -1) {
            loadReviews(courseId);
            checkEnrollment(courseId);
            setupWriteReview(courseId);
        }
    }

    private void initViews(View view) {
        rvReviews = view.findViewById(R.id.rv_reviews);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);

        // Rating summary
        tvAverageRating = view.findViewById(R.id.tv_average_rating);
        tvAverageRatingStars = view.findViewById(R.id.tv_average_rating_stars);
        tvReviewCount = view.findViewById(R.id.tv_review_count);

        bar5 = view.findViewById(R.id.bar_5_star);
        bar4 = view.findViewById(R.id.bar_4_star);
        bar3 = view.findViewById(R.id.bar_3_star);
        bar2 = view.findViewById(R.id.bar_2_star);
        bar1 = view.findViewById(R.id.bar_1_star);

        pct5 = view.findViewById(R.id.tv_5_star_percent);
        pct4 = view.findViewById(R.id.tv_4_star_percent);
        pct3 = view.findViewById(R.id.tv_3_star_percent);
        pct2 = view.findViewById(R.id.tv_2_star_percent);
        pct1 = view.findViewById(R.id.tv_1_star_percent);

        // Write review form
        btnWriteReview = view.findViewById(R.id.btn_write_review);
        layoutWriteForm = view.findViewById(R.id.layout_write_review_form);
        ratingInput = view.findViewById(R.id.rating_bar_input);
        etTitle = view.findViewById(R.id.et_review_title);
        etContent = view.findViewById(R.id.et_review_content);
        etPros = view.findViewById(R.id.et_review_pros);
        etCons = view.findViewById(R.id.et_review_cons);
        btnCancelReview = view.findViewById(R.id.btn_cancel_review);
        btnSubmitReview = view.findViewById(R.id.btn_submit_review);
    }

    private void loadReviews(int courseId) {
        if (activeReviewsLiveData != null) {
            activeReviewsLiveData.removeObserver(reviewsObserver);
        }
        activeReviewsLiveData = sharedViewModel.getCourseReviews(courseId);
        activeReviewsLiveData.observe(getViewLifecycleOwner(), reviewsObserver);
    }

    private void setupAdapter() {
        int courseId = requireActivity().getIntent().getIntExtra("COURSE_ID", -1);
        ReviewAdapter adapter = new ReviewAdapter(localReviewsList, upvotedReviewIds, downvotedReviewIds,
            new ReviewAdapter.OnReviewVoteListener() {
                @Override
                public void onUpvoteClick(ReviewResponse review) {
                    handleVote(courseId, review, true);
                }
                @Override
                public void onDownvoteClick(ReviewResponse review) {
                    handleVote(courseId, review, false);
                }
            });
        rvReviews.setAdapter(adapter);
    }

    private void checkEnrollment(int courseId) {
        sharedViewModel.getEnrollmentStatus(courseId).observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.status == com.example.myapplms.utils.Resource.Status.SUCCESS) {
                if (resource.data != null && resource.data.enrolled) {
                    btnWriteReview.setVisibility(View.VISIBLE);
                } else {
                    btnWriteReview.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupWriteReview(int courseId) {
        btnWriteReview.setOnClickListener(v -> {
            layoutWriteForm.setVisibility(View.VISIBLE);
            btnWriteReview.setVisibility(View.GONE);
        });

        btnCancelReview.setOnClickListener(v -> {
            clearForm();
            layoutWriteForm.setVisibility(View.GONE);
            btnWriteReview.setVisibility(View.VISIBLE);
        });

        btnSubmitReview.setOnClickListener(v -> {
            double rating = ratingInput.getRating();
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            String prosStr = etPros.getText().toString().trim();
            String consStr = etCons.getText().toString().trim();

            if (title.isEmpty()) {
                etTitle.setError("Vui lòng nhập tiêu đề");
                return;
            }
            if (content.isEmpty()) {
                etContent.setError("Vui lòng nhập nội dung đánh giá");
                return;
            }

            List<String> pros = new ArrayList<>();
            if (!prosStr.isEmpty()) {
                for (String p : prosStr.split(",")) {
                    if (!p.trim().isEmpty()) {
                        pros.add(p.trim());
                    }
                }
            }

            List<String> cons = new ArrayList<>();
            if (!consStr.isEmpty()) {
                for (String c : consStr.split(",")) {
                    if (!c.trim().isEmpty()) {
                        cons.add(c.trim());
                    }
                }
            }

            CreateReviewRequest request = new CreateReviewRequest(rating, title, content, pros, cons);
            progressBar.setVisibility(View.VISIBLE);
            btnSubmitReview.setEnabled(false);

            sharedViewModel.submitReview(courseId, request).observe(getViewLifecycleOwner(), resource -> {
                if (resource == null) return;
                if (resource.status == com.example.myapplms.utils.Resource.Status.SUCCESS) {
                    progressBar.setVisibility(View.GONE);
                    btnSubmitReview.setEnabled(true);
                    Toast.makeText(getContext(), "Cảm ơn bạn đã gửi đánh giá thành công!", Toast.LENGTH_SHORT).show();
                    clearForm();
                    layoutWriteForm.setVisibility(View.GONE);
                    btnWriteReview.setVisibility(View.VISIBLE);

                    // Tải lại danh sách review mới nhất
                    loadReviews(courseId);
                } else if (resource.status == com.example.myapplms.utils.Resource.Status.ERROR) {
                    progressBar.setVisibility(View.GONE);
                    btnSubmitReview.setEnabled(true);
                    Toast.makeText(getContext(), "Gửi thất bại: " + resource.message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void clearForm() {
        ratingInput.setRating(5.0f);
        etTitle.setText("");
        etContent.setText("");
        etPros.setText("");
        etCons.setText("");
    }

    private void updateRatingSummary(List<ReviewResponse> reviews) {
        int total = reviews.size();
        if (total == 0) {
            tvAverageRating.setText("0.0");
            tvAverageRatingStars.setText("☆☆☆☆☆");
            tvReviewCount.setText("0 đánh giá");

            bar5.setProgress(0); bar4.setProgress(0); bar3.setProgress(0); bar2.setProgress(0); bar1.setProgress(0);
            pct5.setText("0%"); pct4.setText("0%"); pct3.setText("0%"); pct2.setText("0%"); pct1.setText("0%");
            return;
        }

        double sum = 0;
        int count5 = 0, count4 = 0, count3 = 0, count2 = 0, count1 = 0;

        for (ReviewResponse r : reviews) {
            double rate = r.rating != null ? r.rating : 5.0;
            sum += rate;
            int rounded = (int) Math.round(rate);
            if (rounded >= 5) count5++;
            else if (rounded == 4) count4++;
            else if (rounded == 3) count3++;
            else if (rounded == 2) count2++;
            else count1++;
        }

        double average = sum / total;
        tvAverageRating.setText(String.format(java.util.Locale.US, "%.1f", average));

        int avgStars = (int) Math.round(average);
        StringBuilder starBuilder = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= avgStars) {
                starBuilder.append("★");
            } else {
                starBuilder.append("☆");
            }
        }
        tvAverageRatingStars.setText(starBuilder.toString());
        tvReviewCount.setText(total + " đánh giá");

        int pct5Val = (count5 * 100) / total;
        int pct4Val = (count4 * 100) / total;
        int pct3Val = (count3 * 100) / total;
        int pct2Val = (count2 * 100) / total;
        int pct1Val = (count1 * 100) / total;

        bar5.setProgress(pct5Val);
        bar4.setProgress(pct4Val);
        bar3.setProgress(pct3Val);
        bar2.setProgress(pct2Val);
        bar1.setProgress(pct1Val);

        pct5.setText(pct5Val + "%");
        pct4.setText(pct4Val + "%");
        pct3.setText(pct3Val + "%");
        pct2.setText(pct2Val + "%");
        pct1.setText(pct1Val + "%");
    }

    // ── Vote logic: xử lý cả upvote và downvote ──────────

    private void handleVote(int courseId, ReviewResponse review, boolean isUpvoteClick) {
        long currentTime = System.currentTimeMillis();
        Long lastVoteTime = lastVoteTimeMap.get(review.id);
        if (lastVoteTime != null && currentTime - lastVoteTime < VOTE_COOLDOWN_MS) {
            return; // Đang trong thời gian chờ (cooldown), bỏ qua click (chống spam)
        }

        if (pendingVoteReviewIds.contains(review.id)) {
            return; // Chặn click trùng lặp khi request cũ đang chạy
        }

        lastVoteTimeMap.put(review.id, currentTime);

        com.example.myapplms.utils.SessionManager sessionManager = new com.example.myapplms.utils.SessionManager(requireContext());
        Integer studentId = sessionManager.getStudentId();
        if (studentId == null) {
            Toast.makeText(getContext(), "Bạn cần đăng nhập để bình chọn!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean currentlyUpvoted = upvotedReviewIds.contains(review.id);
        boolean currentlyDownvoted = downvotedReviewIds.contains(review.id);

        pendingVoteReviewIds.add(review.id);

        if (isUpvoteClick) {
            if (currentlyDownvoted) {
                // Đang downvote mà bấm upvote -> Phải gỡ downvote trước, rồi mới upvote
                switchVote(courseId, review, studentId, false, true);
            } else {
                // Gửi bình thường (upvote mới hoặc hủy upvote)
                sendVoteRequest(courseId, review, studentId, true, false);
            }
        } else {
            if (currentlyUpvoted) {
                // Đang upvote mà bấm downvote -> Phải gỡ upvote trước, rồi mới downvote
                switchVote(courseId, review, studentId, true, false);
            } else {
                // Gửi bình thường (downvote mới hoặc hủy downvote)
                sendVoteRequest(courseId, review, studentId, false, false);
            }
        }
    }

    private void switchVote(int courseId, ReviewResponse review, int studentId, boolean currentVoteType, boolean newVoteType) {
        // Bước 1: Gửi request để gỡ vote cũ
        com.example.myapplms.data.remote.dto.request.VoteRequest cancelRequest =
            new com.example.myapplms.data.remote.dto.request.VoteRequest(studentId, currentVoteType);

        sharedViewModel.voteReview(courseId, review.id, cancelRequest).observe(getViewLifecycleOwner(), new androidx.lifecycle.Observer<Resource<ReviewResponse>>() {
            @Override
            public void onChanged(Resource<ReviewResponse> resource) {
                if (resource == null) return;
                if (resource.status == com.example.myapplms.utils.Resource.Status.SUCCESS) {
                    // Đã gỡ thành công -> Bước 2: Gửi request vote mới
                    sendVoteRequest(courseId, review, studentId, newVoteType, true);
                } else if (resource.status == com.example.myapplms.utils.Resource.Status.ERROR) {
                    pendingVoteReviewIds.remove(review.id);
                    Toast.makeText(getContext(), "Chuyển đổi bình chọn thất bại.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendVoteRequest(int courseId, ReviewResponse review, int studentId, boolean isUpvote, boolean isSwitching) {
        com.example.myapplms.data.remote.dto.request.VoteRequest request =
            new com.example.myapplms.data.remote.dto.request.VoteRequest(studentId, isUpvote);

        sharedViewModel.voteReview(courseId, review.id, request).observe(getViewLifecycleOwner(), new androidx.lifecycle.Observer<Resource<ReviewResponse>>() {
            @Override
            public void onChanged(Resource<ReviewResponse> resource) {
                if (resource == null) return;
                if (resource.status == com.example.myapplms.utils.Resource.Status.SUCCESS) {
                    pendingVoteReviewIds.remove(review.id);

                    if (resource.data != null) {
                        if (resource.data.id == null) {
                            resource.data.id = review.id;
                        }
                        updateVoteStateFromResponse(resource.data, studentId);
                        updateLocalReview(resource.data, review.id);
                    }

                } else if (resource.status == com.example.myapplms.utils.Resource.Status.ERROR) {
                    pendingVoteReviewIds.remove(review.id);
                    Toast.makeText(getContext(), "Bình chọn thất bại, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateVoteStateFromResponse(ReviewResponse updatedReview, int studentId) {
        upvotedReviewIds.remove(updatedReview.id);
        downvotedReviewIds.remove(updatedReview.id);

        if (updatedReview.votedUpBy != null && updatedReview.votedUpBy.contains(studentId)) {
            upvotedReviewIds.add(updatedReview.id);
        }
        if (updatedReview.votedDownBy != null && updatedReview.votedDownBy.contains(studentId)) {
            downvotedReviewIds.add(updatedReview.id);
        }
    }

    private void updateLocalReview(ReviewResponse updatedReview, String reviewId) {
        if (localReviewsList == null) return;
        for (int i = 0; i < localReviewsList.size(); i++) {
            if (localReviewsList.get(i).id.equals(reviewId)) {
                localReviewsList.set(i, updatedReview);
                if (rvReviews.getAdapter() != null) {
                    rvReviews.getAdapter().notifyItemChanged(i);
                }
                break;
            }
        }
    }

    // ── Khởi tạo trạng thái vote từ server ──

    private void initVoteStateFromServer(List<ReviewResponse> reviews) {
        com.example.myapplms.utils.SessionManager sm = new com.example.myapplms.utils.SessionManager(requireContext());
        Integer currentStudentId = sm.getStudentId();
        if (currentStudentId == null) return;

        upvotedReviewIds.clear();
        downvotedReviewIds.clear();

        for (ReviewResponse review : reviews) {
            if (review.votedUpBy != null && review.votedUpBy.contains(currentStudentId)) {
                upvotedReviewIds.add(review.id);
            }
            if (review.votedDownBy != null && review.votedDownBy.contains(currentStudentId)) {
                downvotedReviewIds.add(review.id);
            }
        }
    }
}