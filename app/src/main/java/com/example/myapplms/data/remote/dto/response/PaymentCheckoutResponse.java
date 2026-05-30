package com.example.myapplms.data.remote.dto.response;
import com.google.gson.annotations.SerializedName;

public class PaymentCheckoutResponse {
    @SerializedName("paymentId")
    public Integer paymentId;

    @SerializedName("amount")
    public Double amount;

    @SerializedName("paymentStatus")
    public String paymentStatus;

    @SerializedName("qrText")
    public String qrText;

    // THÊM TRƯỜNG NÀY ĐỂ HỨNG LINK TỪ BACKEND
    @SerializedName("checkoutUrl")
    public String checkoutUrl;
}