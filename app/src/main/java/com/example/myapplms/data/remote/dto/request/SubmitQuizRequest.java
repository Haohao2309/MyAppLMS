package com.example.myapplms.data.remote.dto.request;

import java.util.Map;
public class SubmitQuizRequest {
    public Map<String, String> studentAnswers;

    public SubmitQuizRequest(Map<String, String> studentAnswers) {
        this.studentAnswers = studentAnswers;
    }
}