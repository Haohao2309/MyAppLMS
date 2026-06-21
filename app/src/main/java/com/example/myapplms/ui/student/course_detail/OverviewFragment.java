package com.example.myapplms.ui.student.course_detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplms.R;
import com.example.myapplms.data.remote.dto.response.PaymentCheckoutResponse;

public class OverviewFragment extends Fragment {

    private CourseDetailViewModel sharedViewModel;
    private TextView tvDescription, tvInstructor, tvCategory, tvPrice;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ các View
        tvDescription = view.findViewById(R.id.tv_course_description);
        tvInstructor  = view.findViewById(R.id.tv_instructor);
        tvCategory    = view.findViewById(R.id.tv_category);
        tvPrice       = view.findViewById(R.id.tv_price);
        Button btnBuyCourse = view.findViewById(R.id.btn_buy_course);
        btnBuyCourse.setOnClickListener(v -> handleCheckout());

        // Lấy Shared ViewModel từ Activity
        sharedViewModel = new ViewModelProvider(requireActivity()).get(CourseDetailViewModel.class);

        // Lấy ID thực tế từ Intent của Activity thay vì hardcode 1
        int courseId = requireActivity().getIntent().getIntExtra("COURSE_ID", -1);



        if (courseId != -1) {
            sharedViewModel.getCourseDetail(courseId).observe(getViewLifecycleOwner(), resource -> {
                if (resource == null) return;

                switch (resource.status) {
                    case SUCCESS:
                        if (resource.data != null) {
                            if (resource.data.description != null && !resource.data.description.isEmpty()) {
                                tvDescription.setText(resource.data.description);
                            } else {
                                tvDescription.setText("Chưa có mô tả cho khóa học này.");
                            }

                            String instructorName = resource.data.instructor != null ? resource.data.instructor : "Đang cập nhật";
                            tvInstructor.setText("Instructor: " + instructorName.replace("by ", ""));
                            tvCategory.setText("Category: " + (resource.data.category != null ? resource.data.category : "General"));
                            tvPrice.setText("Price: " + (resource.data.priceText != null ? resource.data.priceText : "FREE"));
                        }
                        break;

                    case ERROR:
                        tvDescription.setText("Lỗi tải dữ liệu: " + resource.message);
                        Toast.makeText(getContext(), "Lỗi tải chi tiết: " + resource.message, Toast.LENGTH_SHORT).show();
                        break;

                    case LOADING:
                        tvDescription.setText("Đang tải dữ liệu mô tả...");
                        break;
                }
            });
        }
    }

    private void handleCheckout() {
        int courseId = requireActivity().getIntent().getIntExtra("COURSE_ID", -1);
        if (courseId == -1) {
            Toast.makeText(getContext(), "Không tìm thấy ID khóa học", Toast.LENGTH_SHORT).show();
            return;
        }

        sharedViewModel.checkoutCourse(courseId).observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    Toast.makeText(getContext(), "Đang tạo hóa đơn...", Toast.LENGTH_SHORT).show();
                    break;
                case SUCCESS:
                    PaymentCheckoutResponse data = resource.data;
                    if (data != null) {
                        if ("Paid".equalsIgnoreCase(data.paymentStatus)) {
                            Toast.makeText(getContext(), "Bạn đã mua khóa học này rồi!", Toast.LENGTH_SHORT).show();
                        } else if (data.checkoutUrl != null && !data.checkoutUrl.isEmpty()) {
                            android.content.Intent browserIntent = new android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse(data.checkoutUrl)
                            );
                            startActivity(browserIntent);
                        } else {
                            Toast.makeText(getContext(), "Lỗi: Backend chưa trả về link thanh toán", Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                case ERROR:
                    // Hiển thị lỗi từ Server để biết nguyên nhân thực sự
                    Toast.makeText(getContext(), "Thanh toán thất bại: " + resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }


}