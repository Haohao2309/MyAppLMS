package com.example.myapplms.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class BannerResponse {
    public Long id;
    public String title;
    public String subtitle;

    @SerializedName("action_text")
    public String actionText;

    @SerializedName("background_color")
    public String backgroundColor;

    @SerializedName("image_url")
    public String imageUrl;

    @SerializedName("target_type")
    public String targetType;

    @SerializedName("target_id")
    public String targetId;
}
