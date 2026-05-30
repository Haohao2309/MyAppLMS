package com.example.myapplms.utils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    // Ép chuỗi "2026-05-13T10:30:00" từ Spring Boot ra giờ (VD: 10:30 AM)
    public static String formatTime(String isoDateString) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = isoFormat.parse(isoDateString);
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            return timeFormat.format(date);
        } catch (ParseException e) {
            return "Vừa xong";
        }
    }

    // Ép chuỗi thành Nhóm ngày (Today, Yesterday, Earlier)
    public static String getDateGroup(String isoDateString) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = isoFormat.parse(isoDateString);

            Calendar now = Calendar.getInstance();
            Calendar notifDate = Calendar.getInstance();
            notifDate.setTime(date);

            if (now.get(Calendar.YEAR) == notifDate.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == notifDate.get(Calendar.DAY_OF_YEAR)) {
                return "Today";
            } else if (now.get(Calendar.YEAR) == notifDate.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) - notifDate.get(Calendar.DAY_OF_YEAR) == 1) {
                return "Yesterday";
            } else {
                return "Earlier";
            }
        } catch (ParseException e) {
            return "Earlier";
        }
    }
}