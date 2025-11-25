package edu.univ.erp.ui;

import edu.univ.erp.access.AccessChecker;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import java.awt.*;

public class AdminMaintenancePanel extends JPanel {

    private final AdminService adminService;
    private final AccessChecker accessChecker;

    private JLabel statusLabel;
    private JButton toggleButton;

    public AdminMaintenancePanel() {
        this.adminService = new AdminService();
        this.accessChecker = new AccessChecker(); // To read the current status

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel titleLabel = new JLabel("Maintenance Mode");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        add(titleLabel, c);

        // Status Label
        c.gridx = 0; c.gridy = 1; c.gridwidth = 1;
        add(new JLabel("Current Status:"), c);

        statusLabel = new JLabel();
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        c.gridx = 1; c.gridy = 1;
        add(statusLabel, c);

        // Toggle Button
        toggleButton = new JButton();
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        toggleButton.addActionListener(e -> onToggle());
        add(toggleButton, c);

        // Set the initial state
        updateStatus();
    }


    //Make this PUBLIC so the dashboard can call it to refresh.
    public void updateStatus() {
        boolean isCurrentlyOn = accessChecker.isMaintenanceOn();
        if (isCurrentlyOn) {
            statusLabel.setText("ON");
            statusLabel.setForeground(Color.RED);
            toggleButton.setText("Turn Maintenance Mode OFF");
            toggleButton.setBackground(new Color(25, 135, 84)); // Green
            toggleButton.setForeground(Color.WHITE);
        } else {
            statusLabel.setText("OFF");
            statusLabel.setForeground(new Color(25, 135, 84)); // Green
            toggleButton.setText("Turn Maintenance Mode ON");
            toggleButton.setBackground(new Color(220, 53, 69)); // Red
            toggleButton.setForeground(Color.WHITE);
        }
    }

    private void onToggle() {
        boolean isCurrentlyOn = accessChecker.isMaintenanceOn();
        String message = adminService.setMaintenanceMode(!isCurrentlyOn);
        JOptionPane.showMessageDialog(this, message);
        updateStatus();
    }
}