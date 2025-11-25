package edu.univ.erp.ui; // Flat UI package

import edu.univ.erp.domain.LockedUser;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminUnlockPanel extends JPanel {

    private final AdminService adminService;
    private JTable usersTable;
    private DefaultTableModel tableModel;

    public AdminUnlockPanel() {
        this.adminService = new AdminService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("Manage Locked Accounts");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        add(titleLabel, BorderLayout.NORTH);

        // Table
        JScrollPane scrollPane = createTablePanel();
        add(scrollPane, BorderLayout.CENTER);

        // --- UPDATED BUTTON PANEL ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // 1. Change Password Button (New)
        JButton changePassButton = new JButton("Change Password & Unlock");
        changePassButton.addActionListener(e -> onChangePassword());
        buttonPanel.add(changePassButton);

        // 2. Unlock Button (Renamed)
        JButton unlockButton = new JButton("Unlock Only");
        unlockButton.setBackground(new Color(25, 135, 84)); // Green
        unlockButton.setForeground(Color.WHITE);
        unlockButton.addActionListener(e -> onUnlock());
        buttonPanel.add(unlockButton);

        add(buttonPanel, BorderLayout.SOUTH);
        // ---------------------------

        loadData();
    }

    private JScrollPane createTablePanel() {
        String[] columnNames = {"Username", "Role", "Failed Attempts", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        usersTable = new JTable(tableModel);
        usersTable.setFillsViewportHeight(true);
        return new JScrollPane(usersTable);
    }

    public void loadData() {
        tableModel.setRowCount(0);
        List<LockedUser> users = adminService.getLockedUsers();

        for (LockedUser user : users) {
            tableModel.addRow(new Object[]{
                    user.getUsername(),
                    user.getRole(),
                    user.getFailedAttempts(),
                    user.getStatus()
            });
        }
    }

    private void onUnlock() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow ==-1) {
            JOptionPane.showMessageDialog(this, "Please select a user to unlock.");
            return;
        }

        String username = (String) tableModel.getValueAt(selectedRow, 0);

        String message = adminService.unlockUser(username);
        JOptionPane.showMessageDialog(this, message);

        loadData(); // Refresh table
    }

    private void onChangePassword() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow ==-1) {
            JOptionPane.showMessageDialog(this, "Please select a user.");
            return;
        }

        String username = (String) tableModel.getValueAt(selectedRow, 0);

        // Ask Admin for the new password
        String newPass = JOptionPane.showInputDialog(this, "Enter new password for " + username + ":");

        if (newPass != null) {
            String message = adminService.adminResetPassword(username, newPass);
            JOptionPane.showMessageDialog(this, message);
            loadData(); // Refresh to show updated status
        }
    }
}