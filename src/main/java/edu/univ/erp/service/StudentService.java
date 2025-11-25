package edu.univ.erp.service;

import edu.univ.erp.access.AccessChecker;
import edu.univ.erp.data.ErpDataStore;
import edu.univ.erp.domain.Section;

public class StudentService {

    private final ErpDataStore erpDataStore;
    private final AccessChecker accessChecker;

    public StudentService() {
        this.erpDataStore = new ErpDataStore();
        this.accessChecker = new AccessChecker();
    }

     //attempts to register a student for section
    public String registerForSection(int studentId, int sectionId) {
        if (accessChecker.isMaintenanceOn()) {
            return "Maintenance Mode is ON. Registration is disabled.";
        }
        if (erpDataStore.isStudentAlreadyEnrolled(studentId, sectionId)) {
            return "Error: You are already registered for this section.";
        }

        // if section is full
        Section section = erpDataStore.getSectionDetails(sectionId);
        if (section == null) {
            return "Error: Section not found.";
        }

        if (section.isFull()) {
            return "Error: Section full.";
        }
        boolean success = erpDataStore.createEnrollment(studentId, sectionId);
        if (success) {
            return "Success! You are now registered.";
        } else {
            return "Error: Registration failed for an unknown reason.";
        }
    }
    // drop a section (only if grades not entered)
    public String dropSection(int enrollmentId) {
        if (accessChecker.isMaintenanceOn()) {
            return "Maintenance Mode is ON. Dropping sections is disabled.";
        }
        if (erpDataStore.hasGrades(enrollmentId)) {
            return "Error: Cannot drop a course after grades have been entered.";
        }
        boolean success = erpDataStore.deleteEnrollment(enrollmentId);
        if (success) {
            return "Success! Section has been dropped.";
        } else {
            return "Error: Dropping the section failed.";
        }
    }
}