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
        // 1. ÁNH XẠ NÚT MUA KHÓA HỌC
        Button btnBuyCourse = view.findViewById(R.id.btn_buy_course);

        // 2. GẮN SỰ KIỆN CLICK VÀO NÚT
        btnBuyCourse.setOnClickListener(v -> handleCheckout());
        // Lấy Shared ViewModel từ Activity
        sharedViewModel = new ViewModelProvider(requireActivity()).get(CourseDetailViewModel.class);

        // Lắng nghe dữ liệu PostgreSQL từ Shared ViewModel
        // Không cần bọc trong if (courseId != -1) ở đây nữa vì LiveData trong ViewModel đã được Activity kích hoạt chạy rồi
        sharedViewModel.getCourseDetail(1).observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case SUCCESS:
                    if (resource.data != null) {
                        // 1. Hiển thị mô tả khóa học
                        if (resource.data.description != null && !resource.data.description.isEmpty()) {
                            tvDescription.setText(resource.data.description);
                        } else {
                            tvDescription.setText("Chưa có mô tả cho khóa học này.");
                        }

                        // 2. Hiển thị tên giảng viên
                        String instructorName = resource.data.instructor != null ? resource.data.instructor : "Đang cập nhật";
                        tvInstructor.setText("Instructor: " + instructorName.replace("by ", ""));

                        // 3. Hiển thị danh mục
                        tvCategory.setText("Category: " + (resource.data.category != null ? resource.data.category : "General"));

                        // 4. Hiển thị giá tiền
                        tvPrice.setText("Price: " + (resource.data.priceText != null ? resource.data.priceText : "FREE"));
                    }
                    break;

                case ERROR:
                    tvDescription.setText("Lỗi tải dữ liệu: " + resource.message);
                    break;

                case LOADING:
                    // Giữ nguyên trạng thái hiển thị đang tải dữ liệu
                    tvDescription.setText("Đang tải dữ liệu mô tả...");
                    break;
            }
        });
    }
    // Giả sử bạn có nút này trong layout fragment_overview.xml
    // getBinding().btnBuyCourse.setOnClickListener(v -> handleCheckout());

    private void handleCheckout() {
        int courseId = requireActivity().getIntent().getIntExtra("COURSE_ID", -1);
        if (courseId == -1) return;

        sharedViewModel.checkoutCourse(courseId).observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    // Hiện loading...
                    break;
                case SUCCESS:
                    PaymentCheckoutResponse data = resource.data;
                    if (data != null) {
                        if ("Paid".equalsIgnoreCase(data.paymentStatus)) {
                            Toast.makeText(getContext(), "Bạn đã mua khóa học này rồi!", Toast.LENGTH_SHORT).show();
                        } else {
                            // SỬA Ở ĐÂY: Xóa (hoặc comment) dòng gọi Dialog cũ
                            // showQrDialog(data.amount, courseId, data.paymentId);

                            // THÊM MỚI: Mở trình duyệt với link thanh toán PayOS
                            if (data.checkoutUrl != null && !data.checkoutUrl.isEmpty()) {
                                android.content.Intent browserIntent = new android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse(data.checkoutUrl)
                                );
                                startActivity(browserIntent);
                            } else {
                                Toast.makeText(getContext(), "Lỗi: Backend chưa trả về checkoutUrl", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    break;
            }
        });
    }

}