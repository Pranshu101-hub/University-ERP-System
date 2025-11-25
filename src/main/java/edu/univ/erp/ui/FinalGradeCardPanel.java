package edu.univ.erp.ui;

import edu.univ.erp.domain.FinalGradeSummary;

import javax.swing.*;
import java.awt.*;

/**
 * A simple "card" panel to display a single final grade.
 */
public class FinalGradeCardPanel extends JPanel {

    public FinalGradeCardPanel(FinalGradeSummary grade) {
        setLayout(new GridBagLayout());

        // Add a simple border (we'll add padding in the main panel)
        setBorder(BorderFactory.createEtchedBorder());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 10, 5, 10); // Padding inside the card

        // --- Course Code (e.g., "CS101") ---
        JLabel codeLabel = new JLabel(grade.getCourseCode());
        codeLabel.setFont(codeLabel.getFont().deriveFont(Font.BOLD));
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.LINE_START;
        c.weightx = 0.0; // Don't let this column stretch
        add(codeLabel, c);

        // --- Course Title (e.g., "Intro to Programming") ---
        JLabel titleLabel = new JLabel(grade.getCourseTitle());
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.LINE_START;
        c.weightx = 1.0; // Let this column take up extra space
        add(titleLabel, c);

        // --- Final Grade (e.g., "A") ---
        JLabel gradeLabel = new JLabel(grade.getFinalGrade());
        gradeLabel.setFont(gradeLabel.getFont().deriveFont(Font.BOLD, 16f));
        c.gridx = 2;
        c.gridy = 0;
        c.anchor = GridBagConstraints.LINE_END;
        c.weightx = 0.0; // Don't let this column stretch
        add(gradeLabel, c);
    }
}