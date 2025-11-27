package edu.univ.erp.service;

import edu.univ.erp.access.AccessChecker;
import edu.univ.erp.data.ErpDataStore;
import edu.univ.erp.domain.GradebookRow;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public class InstructorService {
    private final ErpDataStore erpDataStore;
    private final AccessChecker accessChecker;

    public InstructorService() {
        this.erpDataStore = new ErpDataStore();
        this.accessChecker = new AccessChecker();
    }


    // if inst assigned to sec
    private boolean isInstructorForSection(int instructorId, int sectionId) {
        return erpDataStore.isInstructorForSection(instructorId, sectionId);
    }


    // Q1(10%), Q2(10%), Project(20%), Midsem(25%), Endsem(35%) hardcoded
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
                Double q1 = row.getQuiz1();
                Double q2 = row.getQuiz2();
                Double proj = row.getProject();
                Double mid = row.getMidsem();
                Double end = row.getEndsem();

                // Skip if any are missing
                if (q1 == null || q2 == null || proj == null || mid == null || end == null) {
                    continue;
                }

                // Weighted Formula
                double finalScore = (q1 * 0.10) + (q2 * 0.10) + (proj * 0.20) + (mid * 0.25) + (end * 0.35);
                String finalGrade = convertScoreToLetterGrade(finalScore);

                erpDataStore.saveFinalGrade(row.getEnrollmentId(), finalGrade, finalScore);
            }
            return "Final grades calculated and saved!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error calculating grades.";
        }
    }

    // saves all 5 components for list of students
    public String saveAllGrades(int instructorId, int sectionId, List<GradebookRow> gradebook) {
        if (accessChecker.isMaintenanceOn()) {
            return "Maintenance Mode is ON. Cannot save grades.";
        }
        if (!isInstructorForSection(instructorId, sectionId)) {
            return "Error: You are not the instructor for this section.";
        }

        try {
            for (GradebookRow row : gradebook) {
                int eid = row.getEnrollmentId();
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

    // Allows an instructor to drop a student from their section.
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

    // Generates a text report with Mean, Min, Max, and Median for all components.
    public String getSectionStatistics(List<GradebookRow> gradebook) {
        StringBuilder report = new StringBuilder();

        // 1. Collect data into lists
        List<Double> q1 = new ArrayList<>();
        List<Double> q2 = new ArrayList<>();
        List<Double> proj = new ArrayList<>();
        List<Double> mid = new ArrayList<>();
        List<Double> end = new ArrayList<>();
        List<Double> finals = new ArrayList<>();

        for (GradebookRow row : gradebook) {
            if (row.getQuiz1() != null) q1.add(row.getQuiz1());
            if (row.getQuiz2() != null) q2.add(row.getQuiz2());
            if (row.getProject() != null) proj.add(row.getProject());
            if (row.getMidsem() != null) mid.add(row.getMidsem());
            if (row.getEndsem() != null) end.add(row.getEndsem());

            // Re-calculate Final Score for stats (only if all components exist)
            if (row.getQuiz1() != null && row.getQuiz2() != null && row.getProject() != null &&
                    row.getMidsem() != null && row.getEndsem() != null) {
                double finalScore = (row.getQuiz1() * 0.10) + (row.getQuiz2() * 0.10) +
                        (row.getProject() * 0.20) + (row.getMidsem() * 0.25) +
                        (row.getEndsem() * 0.35);
                finals.add(finalScore);
            }
        }

        // 2. Format the report
        report.append(formatStats("Quiz-1", q1));
        report.append(formatStats("Quiz-2", q2));
        report.append(formatStats("Project", proj));
        report.append(formatStats("Midsem", mid));
        report.append(formatStats("Endsem", end));
        report.append("--------------------------------------------------------------------------------------\n");
        report.append(formatStats("FINAL SCORE", finals));

        return report.toString();
    }

    private String formatStats(String name, List<Double> scores) {
        if (scores.isEmpty()) return String.format("%-12s | No data available\n", name);

        Collections.sort(scores); // Sort for Min/Max/Median

        double min = scores.get(0);
        double max = scores.get(scores.size() - 1);

        double sum = 0;
        for (Double s : scores) sum += s;
        double mean = sum / scores.size();

        double median;
        int size = scores.size();
        if (size % 2 == 0) {
            median = (scores.get(size/2 - 1) + scores.get(size/2)) / 2.0;
        } else {
            median = scores.get(size/2);
        }

        return String.format("%-12s | Mean: %5.1f | Median: %5.1f | Min: %5.1f | Max: %5.1f\n",
                name, mean, median, min, max);
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