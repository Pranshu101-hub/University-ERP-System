package edu.univ.erp.domain;

// data class for the profile's grade smmuary

public class FinalGradeSummary {
    private final String courseCode;
    private final String courseTitle;
    private final String finalGrade;

    public FinalGradeSummary(String courseCode, String courseTitle, String finalGrade) {
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        if (finalGrade == null) {
            this.finalGrade = "-";
        } else {
            this.finalGrade = finalGrade;
        }
    }
    public String getCourseCode() { return courseCode; }
    public String getCourseTitle() { return courseTitle; }
    public String getFinalGrade() { return finalGrade; }
}