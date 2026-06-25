package com.example.myapplms.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeeklyActivityResponse {

    @SerializedName("dailySubmissions")
    public List<DailyCount> dailySubmissions;

    @SerializedName("totalThisWeek")
    public long totalThisWeek;

    @SerializedName("totalLastWeek")
    public long totalLastWeek;

    @SerializedName("weeklyChangePercent")
    public double weeklyChangePercent;

    public static class DailyCount {
        @SerializedName("dayLabel")
        public String dayLabel;

        @SerializedName("dayOfWeek")
        public int dayOfWeek;

        @SerializedName("count")
        public long count;
    }
}
