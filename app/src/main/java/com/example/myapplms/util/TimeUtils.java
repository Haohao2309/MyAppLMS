package com.example.myapplms.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {

    public static String formatToRelativeTime(String isoString) {
        if (isoString == null || isoString.isEmpty()) return "Vừa xong";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date date = sdf.parse(isoString);
            long now = System.currentTimeMillis();
            long time = date.getTime();
            long diff = now - time;

            if (diff < 60000) {
                return "Vừa xong";
            } else if (diff < 3600000) {
                return (diff / 60000) + " phút trước";
            } else if (diff < 86400000) {
                return (diff / 3600000) + " giờ trước";
            } else if (diff < 604800000) {
                return (diff / 86400000) + " ngày trước";
            } else {
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return isoString;
        }
    }
}
