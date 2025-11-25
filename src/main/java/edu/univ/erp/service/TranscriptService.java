package edu.univ.erp.service;

import com.opencsv.CSVWriter;
import edu.univ.erp.data.ErpDataStore;
import edu.univ.erp.domain.GradeRow;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TranscriptService {

    private final ErpDataStore erpDataStore;

    public TranscriptService() {
        this.erpDataStore = new ErpDataStore();
    }

    public boolean writeTranscriptToFile(int studentId, File file) {
        List<GradeRow> allGrades = erpDataStore.getStudentGrades(studentId);
        Map<String, List<GradeRow>> gradesByCourse = allGrades.stream()
                .collect(Collectors.groupingBy(GradeRow::getCourseCode));

        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            String[] header = {"Course", "Title", "Q1", "Q2", "Project", "Midsem", "Endsem", "Final Score", "Grade"};
            writer.writeNext(header);

            for (String courseCode : gradesByCourse.keySet()) {
                List<GradeRow> courseGrades = gradesByCourse.get(courseCode);

                String title = findData(courseGrades, "Title");
                String q1 = findData(courseGrades, "Quiz-1");
                String q2 = findData(courseGrades, "Quiz-2");
                String proj = findData(courseGrades, "Project");
                String mid = findData(courseGrades, "Midsem");
                String end = findData(courseGrades, "Endsem");
                String score = findData(courseGrades, "FinalScore");
                String grade = findData(courseGrades, "FinalGrade");

                String[] line = {courseCode, title, q1, q2, proj, mid, end, score, grade};
                writer.writeNext(line);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false; // Failure
        }
    }

    private String findData(List<GradeRow> grades, String type) {
        if (type.equals("Title")) {
            return grades.get(0).getCourseTitle();
        }
        if (type.equals("FinalGrade")) {
            return grades.stream()
                    .filter(g -> "Final".equalsIgnoreCase(g.getComponent()))
                    .map(GradeRow::getFinalGrade)
                    .findFirst()
                    .orElse("N/A");
        }
        if (type.equals("FinalScore")) {
            return grades.stream()
                    .filter(g -> "Final".equalsIgnoreCase(g.getComponent()))
                    .map(GradeRow::getScore)
                    .findFirst()
                    .orElse("N/A");
        }
        return grades.stream()
                .filter(g -> type.equalsIgnoreCase(g.getComponent()))
                .map(GradeRow::getScore)
                .findFirst()
                .orElse("N/A");
    }
}