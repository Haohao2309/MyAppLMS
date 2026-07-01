package com.example.myapplms.ui.student.course_detail;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplms.R;
import com.example.myapplms.LMSApplication;
import com.example.myapplms.data.local.entity.NotificationEntity;
import com.example.myapplms.utils.SessionManager;
import java.util.UUID;
import java.util.Date;

public class PaymentWebViewActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_webview);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thanh toán khóa học");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progress_bar);

        String url = getIntent().getStringExtra("PAYMENT_URL");
        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "URL không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String currentUrl = request.getUrl().toString();
                // Check if PayOS redirects back
                if (currentUrl.contains("cancel=false") || currentUrl.contains("success=true")) {
                    Toast.makeText(PaymentWebViewActivity.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                    
                    int courseId = getIntent().getIntExtra("COURSE_ID", -1);
                    if (courseId != -1) {
                        SessionManager sessionManager = new SessionManager(PaymentWebViewActivity.this);
                        int studentId = sessionManager.getStudentId() != null ? sessionManager.getStudentId() : -1;
                        
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault());
                        String nowStr = sdf.format(new Date());

                        NotificationEntity notif = new NotificationEntity(
                            UUID.randomUUID().toString(),
                            "Thanh toán thành công \uD83C\uDF89",
                            "Bạn đã mua thành công khóa học. Bấm vào đây để bắt đầu học ngay!",
                            "course://" + courseId,
                            "Payment",
                            false,
                            nowStr,
                            "Hôm nay",
                            new Date().getTime(),
                            false,
                            studentId
                        );
                        
                        new Thread(() -> {
                            ((LMSApplication) getApplication()).getNotificationDao().insertOne(notif);
                        }).start();
                    }
                    
                    finish();
                    return true;
                } else if (currentUrl.contains("cancel=true")) {
                    Toast.makeText(PaymentWebViewActivity.this, "Đã hủy thanh toán", Toast.LENGTH_SHORT).show();
                    finish();
                    return true;
                }
                return false;
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        webView.loadUrl(url);
    }
}
