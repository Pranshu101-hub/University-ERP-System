package edu.univ.erp.ui;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.data.ErpDataStore;
import edu.univ.erp.domain.CourseSectionRow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MySectionsPanel extends JPanel {

    private final UserSession session;
    private final ErpDataStore erpDataStore;
    private JTable sectionsTable;
    private DefaultTableModel tableModel;
    private List<CourseSectionRow> sectionList;

    public MySectionsPanel(UserSession session) {
        this.session = session;
        this.erpDataStore = new ErpDataStore();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("My Assigned Sections");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // Table
        JScrollPane scrollPane = createTablePanel();
        add(scrollPane, BorderLayout.CENTER);

        // Action Panel
        JPanel actionPanel = createActionPanel();
        add(actionPanel, BorderLayout.SOUTH);

        loadSectionsData();
    }

    private JScrollPane createTablePanel() {
        // Show all relevant info for the instructor
        String[] columnNames = {"Code", "Title", "Credits", "Capacity", "Day/Time", "Room"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        sectionsTable = new JTable(tableModel);
        sectionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sectionsTable.setFillsViewportHeight(true);

        return new JScrollPane(sectionsTable);
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton gradebookButton = new JButton("View Gradebook for Selected Section");
        gradebookButton.addActionListener(e -> onViewGradebook());

        panel.add(gradebookButton);
        return panel;
    }

    /**
     * Make this PUBLIC so the dashboard can call it to refresh.
     */
    public void loadSectionsData() {
        tableModel.setRowCount(0); // Clear table

        // Get this instructor's sections
        sectionList = erpDataStore.getInstructorSections(session.getUserId());

        for (CourseSectionRow row : sectionList) {
            tableModel.addRow(new Object[]{
                    row.getCourseCode(),
                    row.getCourseTitle(),
                    row.getCredits(),
                    row.getCapacity(),
                    row.getDayTime(),
                    row.getRoom()
            });
        }
    }

    private void onViewGradebook() {
        int selectedRow = sectionsTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a section to view its gradebook.",
                    "No Section Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the selected section
        CourseSectionRow selectedSection = sectionList.get(selectedRow);

        // We will build this 'GradebookPanel' in the *next* step.
        // For now, just show a message.

        GradebookPanel gradebookPanel = new GradebookPanel(session, selectedSection);

        // We'll show this in a new dialog window
        JDialog gradebookDialog = new JDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), // Parent window
                "Gradebook - " + selectedSection.getCourseCode(), // Title
                true // Modal (blocks other windows)
        );
        gradebookDialog.setSize(1200, 600);
        gradebookDialog.setLocationRelativeTo(null);
        gradebookDialog.add(gradebookPanel);
        gradebookDialog.setVisible(true);
    }
}