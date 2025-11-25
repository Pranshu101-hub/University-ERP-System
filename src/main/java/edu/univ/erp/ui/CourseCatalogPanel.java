package edu.univ.erp.ui;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.data.ErpDataStore;
import edu.univ.erp.domain.CourseSectionRow;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import edu.univ.erp.domain.SectionDetails;
import edu.univ.erp.ui.EditSectionDialog; // Or edu.univ.erp.ui.admin.EditSectionDialog
import java.awt.Frame;

// UI Panel for students to browse the course catalog.
public class CourseCatalogPanel extends JPanel {

    private final UserSession session;
    private StudentService studentService;
    private final ErpDataStore erpDataStore;

    private List<CourseSectionRow> catalogList; // To store data with hidden IDs
    private JTable catalogTable;
    private DefaultTableModel tableModel;

    public CourseCatalogPanel(UserSession session) {
        this.session = session;
        this.erpDataStore = new ErpDataStore();

        // Conditionally initialize studentService
        if (session.isStudent()) {
            this.studentService = new StudentService();
        }

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Panel
        JLabel titleLabel = new JLabel("Course Catalog (Monsoon 2025)");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        //center Panel
        JScrollPane scrollPane = createTablePanel();
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel
        if (session.isStudent() || session.isAdmin()) {
            JPanel actionPanel = createActionPanel();
            add(actionPanel, BorderLayout.SOUTH);
        }

        loadCatalogData();
    }

    private JScrollPane createTablePanel() {
        String[] columnNames = {"Code", "Title", "Credits", "Instructor", "Capacity","Day/Time"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        catalogTable = new JTable(tableModel);
        catalogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        catalogTable.setFillsViewportHeight(true);

        return new JScrollPane(catalogTable);
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        if (session.isStudent()) {
            JButton registerButton = new JButton("Register for Selected Section");
            registerButton.addActionListener(e -> onRegister());
            panel.add(registerButton);
        } else if (session.isAdmin()) {
            JButton editButton = new JButton("Edit Selected Section");
            editButton.addActionListener(e -> onEdit());
            panel.add(editButton);
        }

        return panel;
    }
    // data from the ErpDataStore and populates the JTable
    public void loadCatalogData() {
        tableModel.setRowCount(0); // Clear existing data

        // Fetch new data and store it in the class-level list
        catalogList = erpDataStore.getCourseCatalog("Monsoon", 2025);

        for (CourseSectionRow row : catalogList) {
            Object[] rowData = {
                    row.getCourseCode(),
                    row.getCourseTitle(),
                    row.getCredits(),
                    row.getInstructorName(),
                    row.getCapacity(),
                    row.getDayTime()
            };
            tableModel.addRow(rowData);
        }
    }

    private void onRegister() {
        int selectedRow = catalogTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a section from the table to register.",
                    "No Section Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        CourseSectionRow selectedSection = catalogList.get(selectedRow);
        int sectionId = selectedSection.getSectionId();
        int studentId = session.getUserId(); // Get studentId from the session

        // 2. Call the StudentService "brain"
        String message = studentService.registerForSection(studentId, sectionId);

        // 3. Show the result to the user
        if (message.startsWith("Success")) {
            JOptionPane.showMessageDialog(this,
                    message,
                    "Registration Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            // Optionally, refresh data
            loadCatalogData();
        } else {
            JOptionPane.showMessageDialog(this,
                    message,
                    "Registration Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

     // EditSectionDialog.
    private void onEdit() {
        int selectedRow = catalogTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a section to edit.",
                    "No Section Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        CourseSectionRow selectedSectionRow = catalogList.get(selectedRow);
        int sectionId = selectedSectionRow.getSectionId();

        SectionDetails sectionDetails = erpDataStore.getFullSectionDetails(sectionId);

        if (sectionDetails == null) {
            JOptionPane.showMessageDialog(this, "Error: Could not find section details.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);


        EditSectionDialog dialog = new EditSectionDialog(owner, sectionDetails, this::loadCatalogData);
        dialog.setVisible(true);
    }
}