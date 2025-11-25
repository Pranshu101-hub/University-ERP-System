package edu.univ.erp.ui;

import edu.univ.erp.domain.UserRow;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;

public class AdminUserPanel extends JPanel {

    private final AdminService adminService;

    // --- Create User Components ---
    private CardLayout cardLayout;
    private JPanel fieldsPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JPanel studentFieldsPanel, instructorFieldsPanel, adminFieldsPanel;

    // --- CHANGED: programField is now a ComboBox ---
    private JTextField rollNoField;
    private JComboBox<String> programComboBox; // <-- NEW DROPDOWN
    private JTextField yearField;

    private JTextField deptField, titleField;

    // --- User List Components ---
    private JTable usersTable;
    private DefaultTableModel tableModel;

    public AdminUserPanel() {
        this.adminService = new AdminService();
        setLayout(new GridLayout(1, 2, 10, 10)); // Split into 2 columns
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel createPanel = buildCreateUserPanel();
        add(createPanel);

        JPanel listPanel = buildUserListPanel();
        add(listPanel);

        // Initial load
        refreshUserList();
    }

    private JPanel buildCreateUserPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Create New User"));

        // --- Top: Common Fields ---
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; topPanel.add(new JLabel("Username:"), c);
        c.gridx = 1; c.gridy = 0; usernameField = new JTextField(15); topPanel.add(usernameField, c);

        c.gridx = 0; c.gridy = 1; topPanel.add(new JLabel("Password:"), c);
        c.gridx = 1; c.gridy = 1; passwordField = new JPasswordField(15); topPanel.add(passwordField, c);

        c.gridx = 0; c.gridy = 2; topPanel.add(new JLabel("Role:"), c);
        c.gridx = 1; c.gridy = 2;
        roleComboBox = new JComboBox<>(new String[]{"Student", "Instructor", "Admin"});
        topPanel.add(roleComboBox, c);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // --- Center: Dynamic Fields ---
        cardLayout = new CardLayout();
        fieldsPanel = new JPanel(cardLayout);

        buildStudentFields();
        buildInstructorFields();
        buildAdminFields();

        fieldsPanel.add(studentFieldsPanel, "Student");
        fieldsPanel.add(instructorFieldsPanel, "Instructor");
        fieldsPanel.add(adminFieldsPanel, "Admin");

        mainPanel.add(fieldsPanel, BorderLayout.CENTER);

        // --- Bottom: Save Button ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Create User");
        saveButton.setBackground(new Color(25, 135, 84)); // Green
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> onSaveUser());
        bottomPanel.add(saveButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Listener for dropdown
        roleComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                cardLayout.show(fieldsPanel, (String) e.getItem());
            }
        });

        return mainPanel;
    }

    private JPanel buildUserListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("All Users"));

        String[] columnNames = {"ID", "Username", "Role", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        usersTable = new JTable(tableModel);
        usersTable.setFillsViewportHeight(true);

        JButton refreshButton = new JButton("Refresh List");
        refreshButton.addActionListener(e -> refreshUserList());

        panel.add(new JScrollPane(usersTable), BorderLayout.CENTER);
        panel.add(refreshButton, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshUserList() {
        tableModel.setRowCount(0);
        List<UserRow> users = adminService.getAllUsers();
        for (UserRow user : users) {
            tableModel.addRow(new Object[]{
                    user.getUserId(),
                    user.getUsername(),
                    user.getRole(),
                    user.getStatus()
            });
        }
    }

    // --- Helper Builders ---

    private void buildStudentFields() {
        studentFieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; studentFieldsPanel.add(new JLabel("Roll No:"), c);
        c.gridx = 1; c.gridy = 0; rollNoField = new JTextField(15); studentFieldsPanel.add(rollNoField, c);

        c.gridx = 0; c.gridy = 1; studentFieldsPanel.add(new JLabel("Program:"), c);

        // --- UPDATED: Use JComboBox for Program ---
        String[] programs = {"CSE", "CSAI", "CSAM", "CSB", "CSD", "CSSS", "CSEcon", "ECE", "EVE"};
        programComboBox = new JComboBox<>(programs);
        c.gridx = 1; c.gridy = 1; studentFieldsPanel.add(programComboBox, c);
        // ------------------------------------------

        c.gridx = 0; c.gridy = 2; studentFieldsPanel.add(new JLabel("Year:"), c);
        c.gridx = 1; c.gridy = 2; yearField = new JTextField(15); studentFieldsPanel.add(yearField, c);
    }

    private void buildInstructorFields() {
        instructorFieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0; instructorFieldsPanel.add(new JLabel("Dept:"), c);
        c.gridx = 1; c.gridy = 0; deptField = new JTextField(15); instructorFieldsPanel.add(deptField, c);
        c.gridx = 0; c.gridy = 1; instructorFieldsPanel.add(new JLabel("Title:"), c);
        c.gridx = 1; c.gridy = 1; titleField = new JTextField(15); instructorFieldsPanel.add(titleField, c);
    }

    private void buildAdminFields() {
        adminFieldsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        adminFieldsPanel.add(new JLabel("No extra details needed for Admin."));
    }

    private void onSaveUser() {
        String role = (String) roleComboBox.getSelectedItem();

        // --- UPDATED: Get Program from ComboBox ---
        String selectedProgram = (String) programComboBox.getSelectedItem();
        // ------------------------------------------

        String message = adminService.createNewUser(
                usernameField.getText(),
                new String(passwordField.getPassword()),
                role,
                rollNoField.getText(),
                selectedProgram, // Pass the selected value
                yearField.getText(),
                deptField.getText(),
                titleField.getText()
        );
        JOptionPane.showMessageDialog(this, message);
        if (message.startsWith("Success")) {
            refreshUserList();
        }
    }
}