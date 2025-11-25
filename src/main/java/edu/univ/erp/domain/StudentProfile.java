package edu.univ.erp.domain;

//data class to hold student profile 
public class StudentProfile {
    private final int userId;
    private final String rollNo;
    private final String program;
    private final int year;

    public StudentProfile(int userId, String rollNo, String program, int year) {
        this.userId=userId;
        this.rollNo=rollNo;
        this.program=program;
        this.year=year;
    }

    public int getUserId() { return userId; }
    public String getRollNo() { return rollNo; }
    public String getProgram() { return program; }
    public int getYear() { return year; }
}