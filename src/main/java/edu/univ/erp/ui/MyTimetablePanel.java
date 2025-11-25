package edu.univ.erp.ui; // Your flat UI package

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.data.ErpDataStore;
import edu.univ.erp.domain.EnrollmentRow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;

public class MyTimetablePanel extends JPanel {

    private final UserSession session;
    private final ErpDataStore erpDataStore;

    //  need 5 separate TableModels
    private DefaultTableModel mondayModel;
    private DefaultTableModel tuesdayModel;
    private DefaultTableModel wednesdayModel;
    private DefaultTableModel thursdayModel;
    private DefaultTableModel fridayModel;

    private static class TimetableEntry implements Comparable<TimetableEntry> {
        String day;
        String time;
        String code;
        String title;
        String room;
        String instructor;

        private static final Map<String, Integer> DAY_ORDER = Map.of(
                "Monday", 1, "Tuesday", 2, "Wednesday", 3, "Thursday", 4, "Friday", 5, "Saturday", 6, "Sunday", 7
        );

        @Override
        public int compareTo(TimetableEntry other) {
            int dayCompare = DAY_ORDER.getOrDefault(this.day, 0).compareTo(DAY_ORDER.getOrDefault(other.day, 0));
            if (dayCompare != 0) return dayCompare;
            return this.time.compareTo(other.time); // Sort by time if days are the same
        }
    }

    public MyTimetablePanel(UserSession session) {
        this.session = session;
        this.erpDataStore = new ErpDataStore();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("My Weekly Timetable");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // This panel will stack all 5 day-panels vertically
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));

        // Create the 5 table models
        mondayModel = createTableModel();
        tuesdayModel = createTableModel();
        wednesdayModel = createTableModel();
        thursdayModel = createTableModel();
        fridayModel = createTableModel();

        // Load the data into the models *first*
        loadTimetableData();

        // Now, create the 5 day panels (Row count is > 0 here)
        mainContentPanel.add(createDayTablePanel("MONDAY", mondayModel));
        mainContentPanel.add(createDayTablePanel("TUESDAY", tuesdayModel));
        mainContentPanel.add(createDayTablePanel("WEDNESDAY", wednesdayModel));
        mainContentPanel.add(createDayTablePanel("THURSDAY", thursdayModel));
        mainContentPanel.add(createDayTablePanel("FRIDAY", fridayModel));

        // Put the stacking panel inside a ScrollPane
        JScrollPane scrollPane = new JScrollPane(mainContentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Data has already been loaded
    }

    /**
     * Helper method to create a new, non-editable table model.
     */
    private DefaultTableModel createTableModel() {
        String[] columnNames = {"Time", "Course Code", "Title", "Room", "Instructor"};
        return new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }
    /**
     * Helper method to create a single day's panel (e.g., "MONDAY" + its table).
     */
    private JPanel createDayTablePanel(String title, DefaultTableModel model) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Spacing between days

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(titleLabel, BorderLayout.NORTH);

        JTable table = new JTable(model);
        // We set the preferred size based on row height
        table.setPreferredScrollableViewportSize(
                new Dimension(table.getPreferredSize().width, table.getRowHeight() * model.getRowCount())
        );
        table.setFillsViewportHeight(false); // Stop it from stretching

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.add(scrollPane, BorderLayout.NORTH);

        panel.add(tableWrapper, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Make this method PUBLIC so the dashboard can call it to refresh.
     */
    public void loadTimetableData() {
        // 1. Clear all 5 tables
        mondayModel.setRowCount(0);
        tuesdayModel.setRowCount(0);
        wednesdayModel.setRowCount(0);
        thursdayModel.setRowCount(0);
        fridayModel.setRowCount(0);

        // 2. Get and parse the data
        List<EnrollmentRow> registrations = erpDataStore.getStudentRegistrations(session.getUserId());
        List<TimetableEntry> entries = parseRegistrations(registrations);
        Collections.sort(entries);

        // 3. Add rows to the correct table model
        for (TimetableEntry entry : entries) {
            Object[] row = {
                    entry.time,
                    entry.code,
                    entry.title,
                    entry.room,
                    entry.instructor
            };

            switch (entry.day) {
                case "Monday": mondayModel.addRow(row); break;
                case "Tuesday": tuesdayModel.addRow(row); break;
                case "Wednesday": wednesdayModel.addRow(row); break;
                case "Thursday": thursdayModel.addRow(row); break;
                case "Friday": fridayModel.addRow(row); break;
            }
        }
    }


    private List<TimetableEntry> parseRegistrations(List<EnrollmentRow> registrations) {
        List<TimetableEntry> entries = new ArrayList<>();
        Map<String, String> dayMap = Map.of("Mon", "Monday", "Tue", "Tuesday", "Wed", "Wednesday",
                "Thu", "Thursday", "Fri", "Friday", "Sat", "Saturday", "Sun", "Sunday");

        for (EnrollmentRow row : registrations) {
            try {
                // 1. Split by " / " to handle multiple independent slots
                String[] slots = row.getDayTime().split(" / ");

                for (String slot : slots) {
                    // 2. Split "Day Time"
                    String[] parts = slot.split(" ", 2);
                    String dayPart = parts[0];
                    String timePart = parts[1];

                    // 3. Split "Mon/Wed" just in case old data exists
                    String[] days = dayPart.split("/");

                    for (String dayAbbr : days) {
                        TimetableEntry entry = new TimetableEntry();
                        // Map "Mon" -> "Monday"
                        entry.day = dayMap.getOrDefault(dayAbbr, dayAbbr);
                        entry.time = timePart;
                        entry.code = row.getCourseCode();
                        entry.title = row.getCourseTitle();
                        entry.room = row.getRoom();
                        entry.instructor = row.getInstructorName();
                        entries.add(entry);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return entries;
    }
}