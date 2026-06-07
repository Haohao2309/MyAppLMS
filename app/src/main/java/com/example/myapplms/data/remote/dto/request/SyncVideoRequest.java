package com.example.myapplms.data.remote.dto.request;

public class SyncVideoRequest {
    public int currentSeconds;
    public int totalSeconds;

    public SyncVideoRequest(int currentSeconds, int totalSeconds) {
        this.currentSeconds = currentSeconds;
        this.totalSeconds = totalSeconds;
    }
}
