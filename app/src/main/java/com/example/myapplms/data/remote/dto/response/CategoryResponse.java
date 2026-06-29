package com.example.myapplms.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class CategoryResponse {
    @SerializedName("categoryId")
    public int id;

    @SerializedName("categoryName")
    public String name;

    public CategoryResponse(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
