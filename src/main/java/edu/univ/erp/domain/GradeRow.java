package edu.univ.erp.domain;

//my grades table
public class GradeRow {
    private final String courseCode;
    private final String courseTitle;
    private final String component;
    private final Double score;
    private final String finalGrade;

    public GradeRow(String courseCode, String courseTitle, String component, Double score, String finalGrade) {
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.component = component;
        this.score = score;
        this.finalGrade = finalGrade;
    }

    public String getCourseCode() { return courseCode; }
    public String getCourseTitle() { return courseTitle; }
    public String getComponent() { return component; }

    public String getScore() {
        if (score == null) {
            return "-";
        } else {
            return String.format("%.2f", score);
        }
    }
    public String getFinalGrade() {
        if (finalGrade == null) {
            return "-";
        } else {
            return finalGrade;
        }
    }
}