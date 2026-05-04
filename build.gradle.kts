plugins {
    alias(libs.plugins.android.application) apply false
    // Đổi từ 2.58 thành 2.59.2
    id("com.google.dagger.hilt.android") version "2.59.2" apply false
}