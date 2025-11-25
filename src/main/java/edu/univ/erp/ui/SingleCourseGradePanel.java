package edu.univ.erp.ui;

import edu.univ.erp.domain.GradeRow;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * A panel that displays the grade breakdown for a single course,
 * in the 2-row format you requested.
 */
public class SingleCourseGradePanel extends JPanel {
    // It no longer takes a 'courseTitle'
    public SingleCourseGradePanel(List<GradeRow> gradeRows) {
        // Use GridBagLayout for precise control
        super(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 8, 5, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Set a simple border, not a titled one
        setBorder(BorderFactory.createEtchedBorder());

        // 1. Separate the "Final" grade from the other components
        GradeRow finalGradeRow = null;
        List<GradeRow> componentRows = new java.util.ArrayList<>();

        for (GradeRow row : gradeRows) {
            if ("Final".equalsIgnoreCase(row.getComponent())) {
                finalGradeRow = row;
            } else {
                componentRows.add(row);
            }
        }

        // 2. Build the component rows
        int col = 0;
        for (GradeRow component : componentRows) {
            // Row 0: Header (e.g., "Quiz")
            c.gridx = col;
            c.gridy = 0;
            c.anchor = GridBagConstraints.CENTER;
            JLabel headerLabel = new JLabel(component.getComponent());
            headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD));
            add(headerLabel, c);

            // Row 1: Score (e.g., "80.00")
            c.gridx = col;
            c.gridy = 1;
            JLabel scoreLabel = new JLabel(component.getScore());
            scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(scoreLabel, c);

            col++; // Move to the next column
        }

        // 3. Add the Final Grade at the end
        if (finalGradeRow != null) {
            // Add a separator
            c.gridx = col;
            c.gridy = 0;
            c.gridheight = 2; // Span both rows
            add(new JSeparator(SwingConstants.VERTICAL), c);
            col++;

            // Row 0: Header ("Final Grade")
            c.gridx = col;
            c.gridy = 0;
            c.gridheight = 1; // Reset grid height
            c.anchor = GridBagConstraints.CENTER;
            JLabel finalHeader = new JLabel("Final Grade");
            finalHeader.setFont(finalHeader.getFont().deriveFont(Font.BOLD));
            add(finalHeader, c);

            // Row 1: The Grade ("B" and the score)
            c.gridx = col;
            c.gridy = 1;
            String finalGradeText = String.format("%s (%s)",
                    finalGradeRow.getFinalGrade(), finalGradeRow.getScore());
            JLabel finalGradeLabel = new JLabel(finalGradeText);
            finalGradeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(finalGradeLabel, c);
        }
    }
}