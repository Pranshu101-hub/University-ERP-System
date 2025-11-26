package edu.univ.erp.domain;

public class GradebookRow {
    private final int enrollmentId;
    private final int studentId;
    private final String rollNo;
    private final String studentName;
    private Double quiz1;
    private Double quiz2;
    private Double project;
    private Double midsem;
    private Double endsem;
    private String finalGrade;
    private Double finalScore;

    public GradebookRow(int enrollmentId, int studentId, String rollNo, String studentName) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.rollNo = rollNo;
        this.studentName = studentName;
    }

    public int getEnrollmentId() { return enrollmentId; }
    public String getRollNo() { return rollNo; }
    public String getStudentName() { return studentName; }
    public String getFinalGrade() { return finalGrade; }
    public Double getQuiz1() { return quiz1; }
    public Double getQuiz2() { return quiz2; }
    public Double getProject() { return project; }
    public Double getMidsem() { return midsem; }
    public Double getEndsem() { return endsem; }
    public void setQuiz1(Double quiz1) { this.quiz1 = quiz1; }
    public void setQuiz2(Double quiz2) { this.quiz2 = quiz2; }
    public void setProject(Double project) { this.project = project; }
    public void setMidsem(Double midsem) { this.midsem = midsem; }
    public void setEndsem(Double endsem) { this.endsem = endsem; }
    public void setFinalGrade(String finalGrade) { this.finalGrade = finalGrade; }
}