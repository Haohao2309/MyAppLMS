package com.example.myapplms.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Generic wrapper cho phản hồi phân trang từ server.
 * Tương ứng với PagedResponse<T> phía Spring Boot.
 */
public class PagedResponse<T> {

    @SerializedName("content")
    public List<T> content;

    @SerializedName("page")
    public int page;

    @SerializedName("size")
    public int size;

    @SerializedName("totalElements")
    public long totalElements;

    @SerializedName("totalPages")
    public int totalPages;

    @SerializedName("hasNext")
    public boolean hasNext;

    @SerializedName("hasPrevious")
    public boolean hasPrevious;
}
