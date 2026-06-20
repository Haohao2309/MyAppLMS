package com.example.myapplms.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class UriToFileUtil {

    // Thêm vào UriToFileUtil.java — thay hàm from() hiện tại
    public static File from(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            // Decode ảnh gốc
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) return null;

            // Nén xuống còn tối đa 1280px chiều rộng
            bitmap = resizeBitmap(bitmap, 1280);

            // Lưu file nén vào cache
            File tempFile = File.createTempFile("upload_", ".jpg", context.getCacheDir());
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out); // 85% quality
            }
            bitmap.recycle();
            return tempFile;

        } catch (Exception e) {
            return null;
        }
    }

    private static Bitmap resizeBitmap(Bitmap bitmap, int maxWidth) {
        if (bitmap.getWidth() <= maxWidth) return bitmap;
        float ratio = (float) maxWidth / bitmap.getWidth();
        int newHeight = Math.round(bitmap.getHeight() * ratio);
        return Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true);
    }
}