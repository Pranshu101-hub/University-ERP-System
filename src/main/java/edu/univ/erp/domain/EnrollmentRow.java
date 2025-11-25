package edu.univ.erp.domain;


//data class to hold data for the My Registrations table
public class EnrollmentRow {
    private final int enrollmentId;
    private final String courseCode;
    private final String courseTitle;
    private final String instructorName;
    private final String dayTime;
    private final String room;
    private final int credits;
    private final int capacity;
    public EnrollmentRow(int enrollmentId, String courseCode, String courseTitle,
                         String instructorName, String dayTime, String room, int credits, int capacity) {
        this.enrollmentId = enrollmentId;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.instructorName = (instructorName != null) ? instructorName : "TBA";
        this.dayTime = dayTime;
        this.room = room;
        this.credits = credits;
        this.capacity = capacity;
    }

    public int getEnrollmentId() { return enrollmentId; }
    public String getCourseCode() { return courseCode; }
    public String getCourseTitle() { return courseTitle; }
    public String getInstructorName() { return instructorName; }
    public String getDayTime() { return dayTime; }
    public String getRoom() { return room; }
    public int getCredits() { return credits; }
    public int getCapacity() { return capacity; }
}