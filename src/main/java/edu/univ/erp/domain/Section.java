package edu.univ.erp.domain;

//hold sec details
public class Section {
    private final int sectionId;
    private final int capacity;
    private final int currentEnrollment;

    public Section(int sectionId, int capacity, int currentEnrollment) {
        this.sectionId = sectionId;
        this.capacity = capacity;
        this.currentEnrollment = currentEnrollment;
    }

    public int getSectionId() { return sectionId; }
    public int getCapacity() { return capacity; }
    public int getCurrentEnrollment() { return currentEnrollment; }

    //if sec is full
    public boolean isFull() {
        return currentEnrollment >= capacity;
    }
}