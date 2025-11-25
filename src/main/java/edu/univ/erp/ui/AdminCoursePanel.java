package edu.univ.erp.ui;
import java.util.ArrayList;
import java.util.List;
import edu.univ.erp.auth.UserSession;
import edu.univ.erp.data.ErpDataStore;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.CourseSectionRow;
import edu.univ.erp.domain.InstructorInfo;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AdminCoursePanel extends JPanel {
    private final AdminService adminService;
    private final ErpDataStore erpDataStore;
    private JTextField codeField;
    private JTextField titleField;
    private JTextField creditsField;
    private JComboBox<Integer> classCountBox;
    JPanel dynamicSlotsPanel;
    private List<JComboBox<String>> dayBoxes;
    private List<JTextField> startTimeFields;
    private List<JTextField> endTimeFields;
    private JComboBox<String> semesterBox;
    private JTable manageCoursesTable;
    private DefaultTableModel manageCoursesModel;
    private List<Course> allCoursesList;

    private JComboBox<Course> courseComboBox;
    private JComboBox<InstructorInfo> instructorComboBox;
    private JTextField roomField;
    private JTextField capacityField;
    private JTextField yearField;
    private JTable manageSectionsTable;
    private DefaultTableModel manageSectionsModel;
    private List<CourseSectionRow> allSectionsList;
    public AdminCoursePanel(UserSession session) {
        this.adminService = new AdminService();
        this.erpDataStore = new ErpDataStore(); // For loading lists

        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Create Course", buildCreateCoursePanel());
        tabbedPane.addTab("Create Section", buildCreateSectionPanel());
        tabbedPane.addTab("Manage Sections", buildManageSectionsPanel());
        tabbedPane.addTab("Manage Courses", buildManageCoursesPanel());

        add(tabbedPane, BorderLayout.CENTER);

        refreshAllData();
    }

    private JPanel buildCreateCoursePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; panel.add(new JLabel("Course Code:"), c);
        c.gridx = 1; c.gridy = 0; codeField = new JTextField(20); panel.add(codeField, c);

        c.gridx = 0; c.gridy = 1; panel.add(new JLabel("Course Title:"), c);
        c.gridx = 1; c.gridy = 1; titleField = new JTextField(20); panel.add(titleField, c);

        c.gridx = 0; c.gridy = 2; panel.add(new JLabel("Credits:"), c);
        c.gridx = 1; c.gridy = 2; creditsField = new JTextField(20); panel.add(creditsField, c);

        c.gridx = 1; c.gridy = 3; c.anchor = GridBagConstraints.LINE_END; c.fill = GridBagConstraints.NONE;
        JButton saveButton = new JButton("Save New Course");
        saveButton.addActionListener(e -> onSaveCourse());
        panel.add(saveButton, c);
        return panel;
    }

    private JPanel buildCreateSectionPanel() {
        dayBoxes = new ArrayList<>();
        startTimeFields = new ArrayList<>();
        endTimeFields = new ArrayList<>();

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        int gridY = 0;

        //  Select Course
        c.gridx = 0; c.gridy = gridY; c.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel("Select Course:"), c);
        c.gridx = 1; c.gridy = gridY++; c.anchor = GridBagConstraints.LINE_START;
        courseComboBox = new JComboBox<>(); panel.add(courseComboBox, c);

        //  Assign Instructor
        c.gridx = 0; c.gridy = gridY; c.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel("Assign Instructor:"), c);
        c.gridx = 1; c.gridy = gridY++; c.anchor = GridBagConstraints.LINE_START;
        instructorComboBox = new JComboBox<>(); panel.add(instructorComboBox, c);

        // No of Classes per Week
        c.gridx = 0; c.gridy = gridY; c.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel("Classes per week:"), c);
        c.gridx = 1; c.gridy = gridY++; c.anchor = GridBagConstraints.LINE_START;
        Integer[] classCounts = {1, 2, 3};
        classCountBox = new JComboBox<>(classCounts);
        panel.add(classCountBox, c);

        // Dynamic Panel for Day/Time Slots
        c.gridx = 0; c.gridy = gridY; c.gridwidth = 2; c.fill = GridBagConstraints.BOTH;
        dynamicSlotsPanel = new JPanel();
        dynamicSlotsPanel.setLayout(new BoxLayout(dynamicSlotsPanel, BoxLayout.Y_AXIS));
        panel.add(dynamicSlotsPanel, c);
        gridY++; // Move to the next row

        // listener to update the slots
        classCountBox.addItemListener(e -> updateClassSlots());
        updateClassSlots();

        c.gridwidth = 1; c.fill = GridBagConstraints.HORIZONTAL; // Reset constraints

        c.gridx = 0; c.gridy = gridY; c.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel("Room:"), c);
        c.gridx = 1; c.gridy = gridY++; c.anchor = GridBagConstraints.LINE_START;
        roomField = new JTextField(20); panel.add(roomField, c);

        c.gridx = 0; c.gridy = gridY; c.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel("Capacity:"), c);
        c.gridx = 1; c.gridy = gridY++; c.anchor = GridBagConstraints.LINE_START;
        capacityField = new JTextField(20); panel.add(capacityField, c);

        c.gridx = 0; c.gridy = gridY; c.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel("Semester:"), c);
        c.gridx = 1; c.gridy = gridY++; c.anchor = GridBagConstraints.LINE_START;
        String[] semesters = {"Monsoon", "Winter", "Summer"};
        semesterBox = new JComboBox<>(semesters);
        panel.add(semesterBox, c);

        c.gridx = 0; c.gridy = gridY; c.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel("Year (e.g., 2025):"), c);
        c.gridx = 1; c.gridy = gridY++; c.anchor = GridBagConstraints.LINE_START;
        yearField = new JTextField(20); panel.add(yearField, c);

        //  Save Button
        c.gridx = 1; c.gridy = gridY++;
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.NONE;
        JButton saveButton = new JButton("Save New Section");
        saveButton.addActionListener(e -> onSaveSection());
        panel.add(saveButton, c);

        return panel;
    }
    private JPanel buildManageCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columnNames = {"Course Code", "Title", "Credits", "Instructors"};
        manageCoursesModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        manageCoursesTable = new JTable(manageCoursesModel);
        manageCoursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(manageCoursesTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton deleteButton = new JButton("Delete Selected Course");
        deleteButton.setBackground(new Color(220, 53, 69)); // Red
        deleteButton.setForeground(Color.WHITE);
        deleteButton.addActionListener(e -> onDeleteCourse());
        bottomPanel.add(deleteButton);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }
    private JPanel buildManageSectionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columnNames = {"Code", "Title", "Day/Time", "Room", "Instructor", "Capacity"};
        manageSectionsModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        manageSectionsTable = new JTable(manageSectionsModel);
        manageSectionsTable.setFillsViewportHeight(true);

        panel.add(new JScrollPane(manageSectionsTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton deleteButton = new JButton("Delete Selected Section");
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.addActionListener(e -> onDeleteSection());
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }
    public void refreshAllData() {
        System.out.println("DEBUG: refreshAllData() has started running..."); // <-- Debug 1

        List<Course> courses = erpDataStore.getAllCourses();
        courseComboBox.removeAllItems();
        for (Course course : courses) {
            courseComboBox.addItem(course);
        }

        List<InstructorInfo> instructors = erpDataStore.getAllInstructors();


        instructorComboBox.removeAllItems();
        instructorComboBox.addItem(new InstructorInfo(0, "TBA (Unassigned)"));

        for (InstructorInfo inst : instructors) {
            System.out.println("DEBUG: Adding to dropdown -> " + inst.getName()); // <-- Debug 3
            instructorComboBox.addItem(inst);
        }

        loadManageCoursesTable();
        loadManageSectionsTable();
    }
    private void loadManageCoursesTable() {
        if (manageCoursesModel == null) return;

        manageCoursesModel.setRowCount(0);
        allCoursesList = erpDataStore.getAllCourses(); // Get fresh list

        for (Course course : allCoursesList) {
            manageCoursesModel.addRow(new Object[]{
                    course.getCode(),
                    course.getTitle(),
                    course.getCredits(),
                    course.getInstructorNames()
            });
        }
    }
    private void loadManageSectionsTable() {
        if (manageSectionsModel == null) return;
        manageSectionsModel.setRowCount(0);

        allSectionsList = erpDataStore.getAllSections();

        for (CourseSectionRow row : allSectionsList) {
            manageSectionsModel.addRow(new Object[]{
                    row.getCourseCode(),
                    row.getCourseTitle(),
                    row.getDayTime(),
                    row.getRoom(),
                    row.getInstructorName(),
                    row.getCapacity()
            });
        }
    }
    private void onSaveCourse() {
        String message = adminService.createCourse(
                codeField.getText(),
                titleField.getText(),
                creditsField.getText()
        );
        JOptionPane.showMessageDialog(this, message, "Save Status", JOptionPane.INFORMATION_MESSAGE);
        if (message.startsWith("Success")) {
            refreshAllData();
            codeField.setText("");
            titleField.setText("");
            creditsField.setText("");
        }
    }

    private void onSaveSection() {
        Course selectedCourse = (Course) courseComboBox.getSelectedItem();
        InstructorInfo selectedInstructor = (InstructorInfo) instructorComboBox.getSelectedItem();

        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Error: You must select a course.");
            return;
        }

        List<String> combinedSlots = new ArrayList<>();

        for (int i = 0; i < dayBoxes.size(); i++) {
            String day = (String) dayBoxes.get(i).getSelectedItem();

            String start = startTimeFields.get(i).getText();
            String end = endTimeFields.get(i).getText();

            if (start.isEmpty() || end.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Error: Time for Class " + (i+1) + " cannot be empty.");
                return;
            }
            combinedSlots.add(day + " " + start + "-" + end);
        }

        String dayTimeString = String.join(" / ", combinedSlots); // joins them with " / "
        String semesterString = (String) semesterBox.getSelectedItem();
        Integer instructorId;
        if (selectedInstructor == null || selectedInstructor.getInstructorId() == 0) {
            instructorId = null;
        } else {
            instructorId = selectedInstructor.getInstructorId();
        }
        String message = adminService.createSection(
                selectedCourse.getCourseId(),
                instructorId,
                dayTimeString, // Pass the new string
                roomField.getText(),
                capacityField.getText(),
                semesterString,
                yearField.getText()
        );
        JOptionPane.showMessageDialog(this, message);
    }

    private void updateClassSlots() {
        // Clear all old components and lists
        dynamicSlotsPanel.removeAll();
        dayBoxes.clear();
        startTimeFields.clear();
        endTimeFields.clear();

        int numClasses = (Integer) classCountBox.getSelectedItem();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

        for (int i = 1; i <= numClasses; i++) {
            // Create a new sub-panel for this row
            JPanel slotPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

            JLabel title = new JLabel("Class " + i + ":");

            // Day Dropdown
            JLabel dayLabel = new JLabel("Day:");
            JComboBox<String> dayBox = new JComboBox<>(days);
            dayBoxes.add(dayBox); // Add to our list

            // Start Time
            JLabel startLabel = new JLabel("Start:");
            JTextField startField = new JTextField(5);
            startTimeFields.add(startField);

            // End Time
            JLabel endLabel = new JLabel("End:");
            JTextField endField = new JTextField(5);
            endTimeFields.add(endField);

            slotPanel.add(title);
            slotPanel.add(dayLabel);
            slotPanel.add(dayBox);
            slotPanel.add(startLabel);
            slotPanel.add(startField);
            slotPanel.add(endLabel);
            slotPanel.add(endField);

            dynamicSlotsPanel.add(slotPanel);
        }

        // Tell Swing to re-draw the panel
        dynamicSlotsPanel.revalidate();
        dynamicSlotsPanel.repaint();
    }
    private void onDeleteCourse() {
        int selectedRow = manageCoursesTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete.", "No Course Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the full Course object
        Course selectedCourse = allCoursesList.get(selectedRow);

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete '" + selectedCourse.getTitle() + "'?\n" +
                        "This cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            // Call the service
            String message = adminService.deleteCourse(selectedCourse.getCourseId());

            // Show the result
            JOptionPane.showMessageDialog(this, message);

            // Refresh all data (to update dropdowns and this table)
            refreshAllData();
        }
    }
    private void onDeleteSection() {
        int selectedRow = manageSectionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section.");
            return;
        }

        CourseSectionRow section = allSectionsList.get(selectedRow);

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete section for " + section.getCourseCode() + "?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            String msg = adminService.deleteSection(section.getSectionId());
            JOptionPane.showMessageDialog(this, msg);
            if (msg.startsWith("Success")) refreshAllData();
        }
    }
}