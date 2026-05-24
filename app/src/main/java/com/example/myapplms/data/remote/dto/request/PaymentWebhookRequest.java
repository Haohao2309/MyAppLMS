package com.example.myapplms.data.remote.dto.request;

public class PaymentWebhookRequest {
    public Integer paymentId;
    public String paymentStatus;

    public PaymentWebhookRequest(Integer paymentId, String paymentStatus) {
        this.paymentId = paymentId;
        this.paymentStatus = paymentStatus;
    }
}
