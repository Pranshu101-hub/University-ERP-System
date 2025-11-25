package edu.univ.erp.ui; // Your flat UI package

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.UserSession;
import edu.univ.erp.data.ErpDataStore;
import edu.univ.erp.domain.FinalGradeSummary;
import edu.univ.erp.domain.StudentProfile;
import edu.univ.erp.ui.FinalGradeCardPanel;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

public class MyProfilePanel extends JPanel {

    private final UserSession session;
    private final AuthService authService;
    private final ErpDataStore erpDataStore;

    // --- Password Change Components ---
    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel statusLabel;

    public MyProfilePanel(UserSession session) {
        this.session=session;
        this.authService=new AuthService();
        this.erpDataStore=new ErpDataStore();

        setLayout(new BorderLayout()); // Main layout
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // Create a wrapper that stacks things vertically
        JPanel wrapperPanel=new JPanel();
        wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.Y_AXIS));

        // Only build and add the info panel if the user is a Student
        if ("Student".equals(session.getRole())) {
            JPanel profileInfoPanel=buildProfileInfoPanel();
            profileInfoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            wrapperPanel.add(profileInfoPanel);
            wrapperPanel.add(Box.createVerticalStrut(20)); // Spacing
        }
        // The password panel is always added for everyone
        JPanel passwordPanel=buildPasswordPanel();
        passwordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrapperPanel.add(passwordPanel);

        // Add the wrapper to the NORTH to keep it from stretching
        add(wrapperPanel, BorderLayout.NORTH);
    }


    private JPanel buildProfileInfoPanel() {
        // main panel now uses GridBagLayout
        JPanel panel=new JPanel(new GridBagLayout());
        GridBagConstraints c=new GridBagConstraints();
        c.insets=new Insets(10, 20, 10, 20); // Add some padding
        c.anchor=GridBagConstraints.NORTH; // Stick to the top

        // --- 2. Column 0: PROFILE PHOTO ---
        c.gridx=0;
        c.gridy=0;
        c.weightx=0.1; // Give some space
        c.fill=GridBagConstraints.HORIZONTAL;
        JLabel photoLabel=new JLabel("PROFILE PHOTO");
        photoLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        photoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(photoLabel, c);

        // --- 3. Column 1: Center Info (Info + Grades) ---
        c.gridx=1;
        c.gridy=0;
        c.weightx=0.8; // Give this column the most space
        c.fill=GridBagConstraints.HORIZONTAL;

        // We'll create a sub-panel to stack the info and grades
        JPanel centerInfoPanel=new JPanel();
        centerInfoPanel.setLayout(new BoxLayout(centerInfoPanel, BoxLayout.Y_AXIS));

        // Fetch Data
        StudentProfile profile=erpDataStore.getStudentProfile(session.getUserId());
        List<FinalGradeSummary> grades=erpDataStore.getFinalGrades(session.getUserId());

        // -- Student Info (Center Aligned) --
        JLabel infoTitle=new JLabel("My Information");
        infoTitle.setFont(infoTitle.getFont().deriveFont(Font.BOLD, 16f));
        infoTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerInfoPanel.add(infoTitle);

        if (profile != null) {
            JLabel rollLabel=new JLabel("Roll Number: " + profile.getRollNo());
            JLabel programLabel=new JLabel("Program: " + profile.getProgram());
            JLabel yearLabel=new JLabel("Year: " + profile.getYear());

            rollLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            programLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            yearLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            centerInfoPanel.add(rollLabel);
            centerInfoPanel.add(programLabel);
            centerInfoPanel.add(yearLabel);
        }

        // -- Grade Summary (Center Aligned) --
        centerInfoPanel.add(Box.createVerticalStrut(15)); // Add spacing
        JLabel gradesTitle=new JLabel("Final Grade Summary");
        gradesTitle.setFont(gradesTitle.getFont().deriveFont(Font.BOLD, 16f));
        gradesTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerInfoPanel.add(gradesTitle);

        if (grades.isEmpty()) {
            JLabel noGradesLabel=new JLabel("No final grades have been calculated yet.");
            noGradesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerInfoPanel.add(noGradesLabel);
        } else {
            // Loop through grades and create cards
            for (FinalGradeSummary grade : grades) {
                FinalGradeCardPanel card=new FinalGradeCardPanel(grade);
                card.setAlignmentX(Component.CENTER_ALIGNMENT);
                Dimension cardSize=card.getPreferredSize();
                card.setMaximumSize(new Dimension(500, cardSize.height));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(2, 0, 2, 0),
                        card.getBorder()
                ));
                centerInfoPanel.add(card);
            }
        }

        // Add the sub-panel to the main panel
        panel.add(centerInfoPanel, c);

        // --- 4. Column 2: LOGO ---
        c.gridx=2;
        c.gridy=0;
        c.weightx=0.1; // Give some space
        c.fill=GridBagConstraints.HORIZONTAL;
        JLabel logoLabel=new JLabel("LOGO");
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(logoLabel, c);

        return panel;
    }

    /**
     * Builds the center panel for changing password.
     */
    private JPanel buildPasswordPanel() {
        JPanel panel=new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Change Password",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 14)
        ));

        GridBagConstraints c=new GridBagConstraints();
        c.insets=new Insets(5, 10, 5, 10);
        c.fill=GridBagConstraints.HORIZONTAL;

        // --- Column 0: Labels ---
        c.gridx=0;
        c.anchor=GridBagConstraints.LINE_END; // Right-align labels
        c.weightx=0.0; // Don't let this column grow

        c.gridy=1; panel.add(new JLabel("Current Password:"), c);
        c.gridy=2; panel.add(new JLabel("New Password (min 8 chars):"), c);
        c.gridy=3; panel.add(new JLabel("Confirm New Password:"), c);

        // --- Column 1: Text Fields ---
        c.gridx=1;
        c.anchor=GridBagConstraints.LINE_START; // Left-align fields
        c.weightx=0.1; // Give this column a tiny bit of weight
        c.fill=GridBagConstraints.HORIZONTAL; // Make fields fill their space

        c.gridy=1; oldPasswordField=new JPasswordField(20); panel.add(oldPasswordField, c);
        c.gridy=2; newPasswordField=new JPasswordField(20); panel.add(newPasswordField, c);
        c.gridy=3; confirmPasswordField=new JPasswordField(20); panel.add(confirmPasswordField, c);

        // --- Row 4: Save Button ---
        c.gridy=4;
        c.anchor=GridBagConstraints.LINE_START;
        c.fill=GridBagConstraints.NONE; // Don't stretch button
        JButton saveButton=new JButton("Save New Password");
        saveButton.addActionListener(e -> onChangePassword());
        panel.add(saveButton, c);

        // --- Row 5: Status Label ---
        c.gridx=0; c.gridy=5;
        c.gridwidth=2; // Span both columns
        c.anchor=GridBagConstraints.CENTER;
        statusLabel=new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        panel.add(statusLabel, c);

        // --- Alignment Fix 2: Glue Column ---
        // This "glue" column (column 2) absorbs all extra horizontal space
        c.gridx=2; c.gridy=0;
        c.weightx=1.0; // Give it all the growth weight
        c.fill=GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(""), c); // Add an empty, invisible component

        return panel;
    }

    private void onChangePassword() {
        String oldPass=new String(oldPasswordField.getPassword());
        String newPass=new String(newPasswordField.getPassword());
        String confirmPass=new String(confirmPasswordField.getPassword());

        String message=authService.changePassword(
                session.getUserId(),
                oldPass,
                newPass,
                confirmPass
        );

        if (message.startsWith("Success")) {
            statusLabel.setForeground(new Color(25, 135, 84)); // Green
            statusLabel.setText(message);
            oldPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
        } else {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText(message);
        }
    }
}