package com.example.myapplms.data.remote.dto.request;

import java.util.List;

public class CreateDiscussionRequest {
    public String title;
    public String content;
    public String codeSnippet;
    public List<String> tags;

    public CreateDiscussionRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
}