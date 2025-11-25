package edu.univ.erp.domain;

//data class for course
public class Course {
    private final int courseId;
    private final String code;
    private final String title;
    private final int credits;
    private final String instructorNames;

    public Course(int courseId, String code, String title, int credits, String instructorNames){
        this.courseId = courseId;
        this.code = code;
        this.title = title;
        this.credits = credits;
        if (instructorNames == null) {
            this.instructorNames = "None";
        } else {
            this.instructorNames = instructorNames;
        }


    }

    public int getCourseId(){
        return courseId;
    }
    public String getCode(){ return code; }
    public String getTitle(){ return title; }
    public int getCredits(){ return credits; }
    public String getInstructorNames() { return instructorNames; }
    @Override
    public String toString(){
        return code + " - " + title;
    }
}