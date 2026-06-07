package com.example.myapplms.data.remote.dto.request;

public class SubmitAssignmentRequest {
    public String fileUrl;
    public String studentNotes;

    public SubmitAssignmentRequest(String fileUrl, String studentNotes) {
        this.fileUrl = fileUrl;
        this.studentNotes = studentNotes;
    }
}
