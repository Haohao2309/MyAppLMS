package com.example.myapplms.ui.explore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.myapplms.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;

import java.sql.SQLOutput;

public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    // Tạo Interface để gửi kết quả lọc về lại ExploreListCourseFragment
    public interface OnFilterAppliedListener {
        void onFilterApplied(String level, String price, String rating);
    }

    private OnFilterAppliedListener listener;

    public void setFilterListener(OnFilterAppliedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_bottom_sheet_filter, container, false);

        RadioGroup rgLevel = view.findViewById(R.id.rgLevel);
        RadioGroup rgPrice = view.findViewById(R.id.rgPrice);
        ChipGroup cgRating = view.findViewById(R.id.cgRating);
        Button btnApply = view.findViewById(R.id.btnApplyFilter);
        TextView tvClear = view.findViewById(R.id.tvClearFilters);

        // Nút Apply: Lấy dữ liệu đang chọn thực tế và đóng Dialog
        btnApply.setOnClickListener(v -> {
            if (listener != null) {
                // 1. Lấy Level
                String selectedLevel = "All Levels"; // Giá trị mặc định
                int checkedLevelId = rgLevel.getCheckedRadioButtonId();
                if (checkedLevelId != -1) {
                    android.widget.RadioButton rbLevel = view.findViewById(checkedLevelId);
                    if (rbLevel != null) selectedLevel = rbLevel.getText().toString();
                }

                // 2. Lấy Price (Giá)
                String selectedPrice = "All"; // Giá trị mặc định
                int checkedPriceId = rgPrice.getCheckedRadioButtonId();
                if (checkedPriceId != -1) {
                    android.widget.RadioButton rbPrice = view.findViewById(checkedPriceId);
                    if (rbPrice != null) selectedPrice = rbPrice.getText().toString();
                }

                String selectedRating = "Any"; // Giá trị mặc định
                int checkedRatingId = cgRating.getCheckedChipId();
                if (checkedRatingId != -1) {
                    com.google.android.material.chip.Chip chipRating = view.findViewById(checkedRatingId);
                    if (chipRating != null) {
                        selectedRating = chipRating.getText().toString();
                    }
                    // In ra chữ sau khi đã lấy được Text
                    System.out.println("Rating đã chọn là: " + selectedRating);
                }


                // Truyền các giá trị ĐÃ CHỌN về lại Fragment chính
                listener.onFilterApplied(selectedLevel, selectedPrice, selectedRating);
            }
            dismiss(); // Đóng Bottom Sheet
        });

        return view;
    }
}