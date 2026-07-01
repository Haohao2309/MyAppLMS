package com.example.myapplms.utils;

import org.json.JSONObject;

public class ErrorUtil {
    public static String parseError(retrofit2.Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorBodyStr = response.errorBody().string();
                JSONObject jsonObject = new JSONObject(errorBodyStr);
                if (jsonObject.has("error")) {
                    return jsonObject.getString("error");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Lỗi server: " + response.code();
    }
}
