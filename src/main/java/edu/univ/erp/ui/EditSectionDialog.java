package edu.univ.erp.ui;

import edu.univ.erp.data.ErpDataStore;
import edu.univ.erp.domain.InstructorInfo;
import edu.univ.erp.domain.SectionDetails;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditSectionDialog extends JDialog {

    private final AdminService adminService;
    private final ErpDataStore erpDataStore;
    private final SectionDetails section;
    private final Runnable onSaveCallback;

    private JComboBox<InstructorInfo> instructorComboBox;
    private JComboBox<Integer> classCountBox; // For "Number of classes"
    private JPanel dynamicSlotsPanel; // Panel to hold the dynamic rows
    private List<JComboBox<String>> dayBoxes; // List to store Day dropdowns
    private List<JTextField> timeFields; // List to store Time fields
    private JComboBox<String> semesterBox;
    private JTextField roomField;
    private JTextField capacityField;
    private JTextField yearField;

    // Day mapping for parsing
    private static final Map<String, String> DAY_MAP = Map.of(
            "Mon", "Monday", "Tue", "Tuesday", "Wed", "Wednesday",
            "Thu", "Thursday", "Fri", "Friday", "Sat", "Saturday", "Sun", "Sunday"
    );
    // Reverse map for finding the key
    private static final Map<String, String> REVERSE_DAY_MAP = Map.of(
            "Monday", "Mon", "Tuesday", "Tue", "Wednesday", "Wed",
            "Thursday", "Thu", "Friday", "Fri", "Saturday", "Sat", "Sunday", "Sun"
    );

    public EditSectionDialog(Frame owner, SectionDetails section, Runnable onSaveCallback) {
        super(owner, "Edit Section", true); // true = modal dialog
        this.section = section;
        this.onSaveCallback = onSaveCallback;
        this.adminService = new AdminService();
        this.erpDataStore = new ErpDataStore();

        // Initialize lists
        dayBoxes = new ArrayList<>();
        timeFields = new ArrayList<>();

        setSize(550, 450); // Made it a bit wider
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        int gridY = 0;

        c.gridx = 0; c.gridy = gridY; c.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel("Assign Instructor:"), c);
        c.gridx = 1; c.gridy = gridY++; c.anchor = GridBagConstraints.LINE_START;
        instructorComboBox = new JComboBox<>(); panel.add(instructorComboBox, c);

        c.gridx = 0; c.gridy = gridY; c.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel("Classes per week:"), c);
        c.gridx = 1; c.gridy = gridY++; c.anchor = GridBagConstraints.LINE_START;
        Integer[] classCounts = {1, 2, 3};
        classCountBox = new JComboBox<>(classCounts);
        panel.add(classCountBox, c);

        c.gridx = 0; c.gridy = gridY; c.gridwidth = 2; c.fill = GridBagConstraints.BOTH;
        dynamicSlotsPanel = new JPanel();
        dynamicSlotsPanel.setLayout(new BoxLayout(dynamicSlotsPanel, BoxLayout.Y_AXIS));
        panel.add(dynamicSlotsPanel, c);
        gridY++; // Move to the next row

        classCountBox.addItemListener(e -> updateClassSlots());

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

        add(panel, BorderLayout.CENTER);

        //  Save/Cancel Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> onSave());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load data into the form
        populateForm();
    }


     //build the dynamic Day/Time rows
    private void updateClassSlots() {
        dynamicSlotsPanel.removeAll();
        dayBoxes.clear();
        timeFields.clear();

        int numClasses = (Integer) classCountBox.getSelectedItem();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        for (int i = 1; i <= numClasses; i++) {
            JPanel slotPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

            JLabel title = new JLabel("Class " + i + ":");
            JLabel dayLabel = new JLabel("Day:");
            JComboBox<String> dayBox = new JComboBox<>(days);
            dayBoxes.add(dayBox);

            JLabel timeLabel = new JLabel("Time (HH:MM):");
            JTextField timeField = new JTextField(10);
            timeFields.add(timeField);

            slotPanel.add(title);
            slotPanel.add(dayLabel);
            slotPanel.add(dayBox);
            slotPanel.add(timeLabel);
            slotPanel.add(timeField);

            dynamicSlotsPanel.add(slotPanel);
        }
        dynamicSlotsPanel.revalidate();
        dynamicSlotsPanel.repaint();
    }

     //Fills the form with the section's existing data,
    private void populateForm() {
        // simple fields
        roomField.setText(section.getRoom());
        capacityField.setText(String.valueOf(section.getCapacity()));
        semesterBox.setSelectedItem(section.getSemester());
        yearField.setText(String.valueOf(section.getYear()));

        //  Populate dynamic Day/Time fields
        String dayTimeString = section.getDayTime();
        if (dayTimeString != null && !dayTimeString.isEmpty()) {
            String[] slots = dayTimeString.split(" / "); // "Mon 10:00 / Wed 10:00"
            int classCount = slots.length;

            // Set the dropdown to the correct number of classes
            classCountBox.setSelectedItem(classCount);

            // Manually call updateClassSlots() in case the listener didn't fire
            updateClassSlots();
            for (int i = 0; i < classCount; i++) {
                String[] parts = slots[i].split(" ", 2);
                if (parts.length == 2) {
                    String[] days = parts[0].split("/");
                    String dayName = DAY_MAP.getOrDefault(days[0], "Monday");

                    dayBoxes.get(i).setSelectedItem(dayName);
                    timeFields.get(i).setText(parts[1]);
                }
            }
        } else {
            classCountBox.setSelectedItem(1);
            updateClassSlots();
        }

        List<InstructorInfo> instructors = erpDataStore.getAllInstructors();
        instructorComboBox.addItem(new InstructorInfo(0, "TBA (Unassigned)"));
        InstructorInfo toSelect = null;
        for (InstructorInfo inst : instructors) {
            instructorComboBox.addItem(inst);
            if (section.getInstructorId() != null && inst.getInstructorId() == section.getInstructorId()) {
                toSelect = inst;
            }
        }
        if (toSelect != null) {
            instructorComboBox.setSelectedItem(toSelect);
        }
    }
    /**
     * Called when the "Save" button is clicked.
     */
    private void onSave() {
        InstructorInfo selectedInstructor = (InstructorInfo) instructorComboBox.getSelectedItem();
        Integer instructorId = (selectedInstructor == null || selectedInstructor.getInstructorId() == 0)
                ? null : selectedInstructor.getInstructorId();
        StringBuilder dayTimeBuilder = new StringBuilder();
        List<String> combinedSlots = new ArrayList<>();

        for (int i = 0; i < dayBoxes.size(); i++) {
            String dayName = (String) dayBoxes.get(i).getSelectedItem(); // "Monday"
            String dayAbbr = REVERSE_DAY_MAP.get(dayName); // "Mon"
            String time = timeFields.get(i).getText();

            if (time.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Error: Time for Class " + (i+1) + " cannot be empty.");
                return;
            }

            combinedSlots.add(dayAbbr + " " + time);
        }
        String dayTimeString = String.join(" / ", combinedSlots);

        String message = adminService.updateSection(
                section.getSectionId(),
                instructorId,
                dayTimeString, // Pass the new string
                roomField.getText(),
                capacityField.getText(),
                (String) semesterBox.getSelectedItem(),
                yearField.getText()
        );

        JOptionPane.showMessageDialog(this, message);

        if (message.startsWith("Success")) {
            onSaveCallback.run(); // Call the refresh function
            dispose(); // Close the dialog
        }
    }
}