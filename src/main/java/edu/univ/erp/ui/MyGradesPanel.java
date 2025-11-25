package edu.univ.erp.ui;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.data.ErpDataStore;
import edu.univ.erp.domain.GradeRow;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import edu.univ.erp.service.TranscriptService;
import javax.swing.JFileChooser;
import java.io.File;
import java.util.stream.Collectors;
import edu.univ.erp.ui.SingleCourseGradePanel;
public class MyGradesPanel extends JPanel {

    private final UserSession session;
    private final ErpDataStore erpDataStore;
    private final TranscriptService transcriptService;
    // This is the main panel that will hold all the course "cards"
    private JPanel contentPanel;

    public MyGradesPanel(UserSession session) {
        this.session = session;
        this.erpDataStore = new ErpDataStore();
        this.transcriptService = new TranscriptService();
        // Main panel uses BorderLayout
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("My Grades");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // 1. Create the content panel with a vertical BoxLayout
        // This panel will stack all the cards
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // 2. Create a "wrapper" panel that uses BorderLayout.
        // This is the trick to stop the stretching.
        JPanel wrapperPanel = new JPanel(new BorderLayout());

        // 3. Add the contentPanel to the NORTH of the wrapper.
        // This forces it to be its preferred size and align top-left.
        wrapperPanel.add(contentPanel, BorderLayout.NORTH);

        // 4. Put the *wrapperPanel* inside the JScrollPane
        JScrollPane scrollPane = new JScrollPane(wrapperPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // No ugly border
        add(scrollPane, BorderLayout.CENTER);
        // 5. Add the Download Button to the bottom
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton downloadButton = new JButton("Download Transcript (CSV)");
        downloadButton.addActionListener(e -> onDownload());
        bottomPanel.add(downloadButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadGradesData();
    }
    /**
     * This method is public so the MainDashboard can call it on tab-click.
     */
    public void loadGradesData() {
        // Clear all old "cards"
        contentPanel.removeAll();

        // 1. Fetch the flat list of all grades
        List<GradeRow> allGrades = erpDataStore.getStudentGrades(session.getUserId());

        // 2. Group the grades by Course Title
        Map<String, List<GradeRow>> gradesByCourse = allGrades.stream()
                .collect(Collectors.groupingBy(GradeRow::getCourseTitle));

        // 3. Create a new "card" for each course
        if (gradesByCourse.isEmpty()) {
            contentPanel.add(new JLabel("No grades have been entered yet."));
        } else {
            for (String courseTitle : gradesByCourse.keySet()) {
                List<GradeRow> courseGrades = gradesByCourse.get(courseTitle);

                JLabel titleLabel = new JLabel(courseTitle);
                titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
                // Add padding to the left and top
                titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 0));
                contentPanel.add(titleLabel);

                SingleCourseGradePanel courseCard = new SingleCourseGradePanel(courseGrades);
                // Align the card to the left
                courseCard.setAlignmentX(Component.LEFT_ALIGNMENT);

                // Add padding to the left
                courseCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(0, 5, 5, 5), // Outer padding
                        courseCard.getBorder() // Inner EtchedBorder
                ));
                contentPanel.add(courseCard);
            }
        }

        // Refresh the panel
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    /**
     * Handles the "Download" button click.
     */
    private void onDownload() {
        // 1. Open a "Save" dialog
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Transcript");
        fileChooser.setSelectedFile(new File("transcript_" + session.getUserId() + ".csv"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            // 2. Use a SwingWorker to save the file in the background
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    // This runs on a background thread
                    return transcriptService.writeTranscriptToFile(session.getUserId(), fileToSave);
                }

                @Override
                protected void done() {
                    // This runs back on the UI thread when finished
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(MyGradesPanel.this,
                                    "Transcript saved successfully to:\n" + fileToSave.getAbsolutePath(),
                                    "Export Successful",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            throw new Exception("Write failed");
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(MyGradesPanel.this,
                                "Error: Could not save transcript.",
                                "Export Failed",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            worker.execute(); // Start the worker
        }
        }
}