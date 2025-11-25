package edu.univ.erp.domain;

//inst id and dropdown names
public class InstructorInfo {
    private final int instructorId;
    private final String name;
    public InstructorInfo(int instructorId, String name) {
        this.instructorId=instructorId;
        this.name=name;
    }
    public int getInstructorId() { return instructorId; }
    public String getName() { return name; }
    @Override
    public String toString() {
        return name;
    }
}