package com.example.myapplms.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageUtils {

    /**
     * Chuyển Uri (content://) thành File thực trên disk.
     * Cần thiết vì Retrofit Multipart cần File, không nhận Uri trực tiếp.
     */
    public static File uriToFile(Context context, Uri uri) {
        try {
            // Lấy tên file gốc
            String fileName = getFileName(context, uri);
            File tempFile = new File(context.getCacheDir(), fileName);

            // Copy nội dung vào file tạm
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return tempFile;
        } catch (Exception e) {
            return null;
        }
    }

    private static String getFileName(Context context, Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver()
                    .query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result != null ? result : "avatar_" + System.currentTimeMillis() + ".jpg";
    }
}
