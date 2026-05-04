plugins {
    id("com.android.application")
    id("com.google.dagger.hilt.android") // Bắt buộc phải có dòng này
}

android {
    namespace = "com.example.myapplms"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.myapplms"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.viewbinding)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Thư viện Retrofit cốt lõi
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
// Thư viện giúp Retrofit tự động chuyển đổi JSON sang Object bằng Gson
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
// Thư viện Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // 1. Thư viện OkHttp Logging Interceptor
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

// 2. Thư viện Dagger Hilt (Dành cho dự án viết bằng Java)
    implementation("com.google.dagger:hilt-android:2.59.2")
    annotationProcessor("com.google.dagger:hilt-android-compiler:2.59.2")

    // Lifecycle ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")

}