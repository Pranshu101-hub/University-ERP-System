package edu.univ.erp.ui;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.data.ErpDataStore;
import edu.univ.erp.domain.EnrollmentRow;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class MyRegistrationsPanel extends JPanel {

    private final UserSession session;
    private final StudentService studentService;
    private final ErpDataStore erpDataStore;

    private List<EnrollmentRow> registrationList;
    private JTable registrationTable;
    private DefaultTableModel tableModel;

    public MyRegistrationsPanel(UserSession session) {
        this.session = session;
        this.studentService = new StudentService();
        this.erpDataStore = new ErpDataStore();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("My Registered Sections");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        JScrollPane scrollPane = createTablePanel();
        add(scrollPane, BorderLayout.CENTER);

        JPanel actionPanel = createActionPanel();
        add(actionPanel, BorderLayout.SOUTH);

        loadRegistrationData();
    }

    private JScrollPane createTablePanel() {
        String[] columnNames = {"Code", "Title", "Credits", "Instructor", "Capacity", "Days", "Time"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        registrationTable = new JTable(tableModel);
        registrationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        registrationTable.setFillsViewportHeight(true);

        // Set column widths
        registrationTable.getColumnModel().getColumn(5).setPreferredWidth(80); // Days
        registrationTable.getColumnModel().getColumn(6).setPreferredWidth(120); // Time

        return new JScrollPane(registrationTable);
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton dropButton = new JButton("Drop Selected Section");
        dropButton.setBackground(new Color(220, 53, 69)); // Red
        dropButton.setForeground(Color.WHITE);
        dropButton.addActionListener(e -> onDrop());

        panel.add(dropButton);
        return panel;
    }

    public void loadRegistrationData() {
        tableModel.setRowCount(0);

        registrationList = erpDataStore.getStudentRegistrations(session.getUserId());

        for (EnrollmentRow row : registrationList) {
            String rawDayTime = row.getDayTime();
            StringBuilder daysBuilder = new StringBuilder();
            StringBuilder timeBuilder = new StringBuilder();

            // --- PARSING LOGIC ---
            // Raw format: "Mon 10:00-11:30 / Wed 14:00-15:30"
            if (rawDayTime != null && !rawDayTime.isEmpty()) {
                String[] slots = rawDayTime.split(" / ");

                for (String slot : slots) {
                    String[] parts = slot.split(" ", 2);
                    if (parts.length == 2) {
                        if (daysBuilder.length() > 0) daysBuilder.append(", ");
                        daysBuilder.append(parts[0]); // "Mon" or "Mon/Wed"

                        if (timeBuilder.length() > 0) timeBuilder.append(", ");
                        timeBuilder.append(parts[1]); // "10:00-11:30"
                    } else {
                        // Fallback for messy data
                        daysBuilder.append(slot);
                    }
                }
            }
            // ---------------------

            tableModel.addRow(new Object[]{
                    row.getCourseCode(),
                    row.getCourseTitle(),
                    row.getCredits(),
                    row.getInstructorName(),
                    row.getCapacity(),
                    daysBuilder.toString(), // e.g., "Mon, Wed"
                    timeBuilder.toString()  // e.g., "10:00-11:30, 14:00-15:30"
            });
        }
    }

    private void onDrop() {
        int selectedRow = registrationTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section to drop.");
            return;
        }

        EnrollmentRow selectedEnrollment = registrationList.get(selectedRow);
        int enrollmentId = selectedEnrollment.getEnrollmentId();

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to drop " + selectedEnrollment.getCourseCode() + "?",
                "Confirm Drop", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            String message = studentService.dropSection(enrollmentId);
            if (message.startsWith("Success")) {
                JOptionPane.showMessageDialog(this, message);
                loadRegistrationData();
            } else {
                JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}