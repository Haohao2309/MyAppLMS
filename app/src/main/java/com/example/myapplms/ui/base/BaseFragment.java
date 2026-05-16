package com.example.myapplms.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

/**
 * Lớp nền tảng cho tất cả các Fragment trong ứng dụng.
 * @param <VB> Kiểu ViewBinding của Fragment con.
 */
public abstract class BaseFragment<VB extends ViewBinding> extends Fragment {

    // Biến binding được quản lý ở Base. Các lớp con sẽ truy cập thông qua getBinding()
    private VB binding;

    /**
     * Phương thức trừu tượng: Các Fragment con bắt buộc phải ghi đè hàm này
     * để truyền cách inflate ViewBinding của riêng chúng.
     * Ví dụ: FragmentNotificationsBinding::inflate
     */
    @NonNull
    protected abstract VB inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Giao việc inflate layout cho lớp con thực hiện
        binding = inflateBinding(inflater, container);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Gọi hàm setup logic riêng của từng Fragment con
        setupViews();
        setupListeners();
        observeViewModel();
    }

    /**
     * Trả về đối tượng Binding để Fragment con sử dụng.
     * Ném lỗi nếu gọi hàm này sau khi Fragment đã bị hủy (sau onDestroyView).
     */
    @NonNull
    protected final VB getBinding() {
        if (binding == null) {
            throw new IllegalStateException("Không thể gọi getBinding() sau khi onDestroyView() đã được gọi.");
        }
        return binding;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // QUAN TRỌNG: Giải phóng bộ nhớ khi View bị hủy để tránh Memory Leak
        binding = null;
    }

    // --- CÁC PHƯƠNG THỨC TIỆN ÍCH DÙNG CHUNG ---

    /**
     * Hiển thị Toast ngắn gọn.
     */
    protected void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    // Các hàm này có thể để rỗng (không abstract) để lớp con nào cần thì Override
    protected void setupViews() {}
    protected void observeViewModel() {}
    protected void setupListeners() {}
}