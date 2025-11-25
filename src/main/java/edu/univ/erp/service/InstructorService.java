package edu.univ.erp.service;

import edu.univ.erp.access.AccessChecker;
import edu.univ.erp.data.ErpDataStore;
import edu.univ.erp.domain.GradebookRow;
import java.util.List;

public class InstructorService {
    private final ErpDataStore erpDataStore;
    private final AccessChecker accessChecker;

    public InstructorService() {
        this.erpDataStore=new ErpDataStore();
        this.accessChecker=new AccessChecker();
    }

     // if inst  assigned to sec
    private boolean isInstructorForSection(int instructorId, int sectionId) {
        return erpDataStore.isInstructorForSection(instructorId, sectionId);
    }

    //Q1(10%), Q2(10%), Project(20%), Midsem(25%), Endsem(35%) hardcoded
    public String calculateFinalGrades(int instructorId, int sectionId, List<GradebookRow> gradebook) {
        if (accessChecker.isMaintenanceOn()) {
            return "Maintenance Mode is ON. Cannot calculate grades.";
        }
        if (!isInstructorForSection(instructorId, sectionId)) {
            return "Error: You are not the instructor for this section.";
        }
        try {
            for (GradebookRow row : gradebook) {
                // Get all scores
                Double q1=row.getQuiz1();
                Double q2=row.getQuiz2();
                Double proj=row.getProject();
                Double mid=row.getMidsem();
                Double end=row.getEndsem();

                // Skip if any are missing
                if (q1 == null || q2 == null || proj == null || mid == null || end == null) {
                    continue;
                }

                // Weighted Formula
                double finalScore=(q1 * 0.10) + (q2 * 0.10) + (proj * 0.20) + (mid * 0.25) + (end * 0.35);
                String finalGrade=convertScoreToLetterGrade(finalScore);

                erpDataStore.saveFinalGrade(row.getEnrollmentId(), finalGrade, finalScore);
            }
            return "Final grades calculated and saved!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error calculating grades.";
        }
    }


    //saves all 5 components for list of students
    public String saveAllGrades(int instructorId, int sectionId, List<GradebookRow> gradebook) {
        if (accessChecker.isMaintenanceOn()) {
            return "Maintenance Mode is ON. Cannot save grades.";
        }
        if (!isInstructorForSection(instructorId, sectionId)) {
            return "Error: You are not the instructor for this section.";
        }

        try {
            for (GradebookRow row : gradebook) {
                int eid=row.getEnrollmentId();
                erpDataStore.saveGrade(eid, "Quiz-1", row.getQuiz1());
                erpDataStore.saveGrade(eid, "Quiz-2", row.getQuiz2());
                erpDataStore.saveGrade(eid, "Project", row.getProject());
                erpDataStore.saveGrade(eid, "Midsem", row.getMidsem());
                erpDataStore.saveGrade(eid, "Endsem", row.getEndsem());
            }
            return "Grades saved successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error saving grades.";
        }
    }
     //Allows an instructor to drop a student from their section.
    public String dropStudent(int instructorId, int sectionId, int enrollmentId) {
        if (accessChecker.isMaintenanceOn()) {
            return "Maintenance Mode is ON. Cannot drop students.";
        }

        if (!isInstructorForSection(instructorId, sectionId)) {
            return "Error: You are not the instructor for this section.";
        }

        erpDataStore.deleteGrades(enrollmentId);

        boolean success = erpDataStore.deleteEnrollment(enrollmentId);

        if (success) {
            return "Success! Student and their grades have been dropped.";
        } else {
            return "Error: Could not drop student.";
        }
    }
    private String convertScoreToLetterGrade(double score) {
        if (score >= 90) return "A+";
        if (score >= 80) return "A-";
        if (score >= 65) return "B";
        if (score >= 50) return "B-";
        if (score >= 40) return "C";
        if (score >= 30) return "D";
        return "F";
    }
}