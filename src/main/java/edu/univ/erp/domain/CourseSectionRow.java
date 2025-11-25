package edu.univ.erp.domain;


 //simple data class to hold the combined data for the course catalog

public class CourseSectionRow {

    private final int sectionId;
    private final String courseCode;
    private final String courseTitle;
    private final int credits;
    private final int capacity;
    private final String instructorName;
    private final String dayTime;
    private final String room;

    public CourseSectionRow(int sectionId, String courseCode, String courseTitle, int credits, int capacity, String instructorName, String dayTime, String room) {
        this.sectionId = sectionId;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.credits = credits;
        this.capacity = capacity;
        this.instructorName = instructorName != null ? instructorName : "TBA"; // Handle unassigned instructors
        this.dayTime = dayTime;
        this.room = room;
    }

    public int getSectionId() { return sectionId; }
    public String getCourseCode() { return courseCode; }
    public String getCourseTitle() { return courseTitle; }
    public int getCredits() { return credits; }
    public int getCapacity() { return capacity; }
    public String getInstructorName() { return instructorName; }
    public String getDayTime() { return dayTime; }
    public String getRoom() { return room; }
}