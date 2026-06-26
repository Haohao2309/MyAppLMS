package com.example.myapplms.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GradingListResponse {

    @SerializedName("stats")
    private GradingStats stats;

    @SerializedName("students")
    private List<StudentGradingItem> students;

    public GradingStats getStats()                      { return stats; }
    public List<StudentGradingItem> getStudents()       { return students; }

    // ── GradingStats ────────────────────────────────────────────
    public static class GradingStats {
        @SerializedName("totalStudents")  private int totalStudents;
        @SerializedName("gradedCount")    private int gradedCount;
        @SerializedName("ungradedCount")  private int ungradedCount;
        @SerializedName("maskedCount")    private int maskedCount;
        @SerializedName("avgFinalScore")  private Double avgFinalScore;

        public int getTotalStudents()   { return totalStudents; }
        public int getGradedCount()     { return gradedCount; }
        public int getUngradedCount()   { return ungradedCount; }
        public int getMaskedCount()     { return maskedCount; }
        public Double getAvgFinalScore(){ return avgFinalScore; }
    }

    // ── StudentGradingItem ───────────────────────────────────────
    public static class StudentGradingItem {
        @SerializedName("studentId")    private Integer studentId;
        @SerializedName("fullName")     private String  fullName;
        @SerializedName("graded")       private boolean graded;
        @SerializedName("isMasked")     private Boolean isMasked;
        @SerializedName("submissionId") private String  submissionId;
        @SerializedName("fileUrl")      private String  fileUrl;
        @SerializedName("submittedAt")  private String  submittedAt;
        @SerializedName("courseGradeId")private Integer courseGradeId;
        @SerializedName("examScore")    private Double  examScore;
        @SerializedName("quizAvgScore") private Double  quizAvgScore;
        @SerializedName("finalScore")   private Double  finalScore;
        @SerializedName("gradeLevel")   private String  gradeLevel;
        @SerializedName("gradedAt")     private String  gradedAt;

        public Integer getStudentId()   { return studentId; }
        public String  getFullName()    { return fullName; }
        public boolean isGraded()       { return graded; }
        public Boolean getIsMasked()    { return isMasked; }
        public String  getSubmissionId(){ return submissionId; }
        public String  getFileUrl()     { return fileUrl; }
        public String  getSubmittedAt() { return submittedAt; }
        public Integer getCourseGradeId(){ return courseGradeId; }
        public Double  getExamScore()   { return examScore; }
        public Double  getQuizAvgScore(){ return quizAvgScore; }
        public Double  getFinalScore()  { return finalScore; }
        public String  getGradeLevel()  { return gradeLevel; }
        public String  getGradedAt()    { return gradedAt; }
    }
}