//package com.example.myapplms.ui.profile;
//
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModel;
//import com.example.myapplms.model.User;
//
//public class ProfileViewModel extends ViewModel {
//
//    // Lưu thông tin User
//    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
//    // Lưu trạng thái công tắc thông báo
//    private final MutableLiveData<Boolean> isPushNotificationEnabled = new MutableLiveData<>(true);
//
//    public ProfileViewModel() {
//        // Giả lập lấy dữ liệu từ Data/API
//        loadUserData();
//    }
//
//    public LiveData<User> getCurrentUser() { return currentUser; }
//    public LiveData<Boolean> getPushNotificationStatus() { return isPushNotificationEnabled; }
//
//    private void loadUserData() {
//        // Tạm thời tạo User giả. Sau này sẽ gọi từ Repository/Room Database
//        User mockUser = new User("1", "Trịnh Trường Giang", "IT Student at HUCE", "System Ops Lead");
//        currentUser.setValue(mockUser);
//    }
//
//    public void togglePushNotification(boolean isChecked) {
//        isPushNotificationEnabled.setValue(isChecked);
//        // Sau này sẽ gọi API để lưu cài đặt này lên server Spring Boot
//    }
//}