package com.example.myapplms.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class CategoryResponse {
    @SerializedName("categoryId")
    public int id;

    @SerializedName("categoryName")
    public String name;

}
