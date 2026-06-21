package com.example.myapplms.data.remote.dto.response;

import java.util.List;

public class DiscussionResponse {
    public String id;
    public String lessonId;
    public Integer authorId;
    public String authorName;
    public String authorRole;
    public String title;
    public String content;
    public int upvotes;
    public int replyCount;
    public String createdAt;
    public List<ReplyResponse> replies;

    public static class ReplyResponse {
        public Integer replyId;
        public String authorName;
        public String authorRole;
        public String content;
        public boolean isAccepted;
        public int upvotes;
        public String createdAt;
    }
}