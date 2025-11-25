package edu.univ.erp.ui;// Package: edu.univ.erp.ui.auth

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.UserSession;
import edu.univ.erp.ui.MainDashboard; // We will create this next

import javax.swing.*;
import java.awt.*;

/**
 * The main login window for the application[cite: 58].
 */
public class LoginWindow extends JFrame {

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final JLabel statusLabel;

    private final AuthService authService;

    public LoginWindow() {
        this.authService = new AuthService();

        setTitle("University ERP - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        // Use a more organized layout
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 10, 5, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Username
        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("Username:"), c);
        c.gridx = 1;
        c.gridy = 0;
        usernameField = new JTextField(20);
        panel.add(usernameField, c);

        // Password
        c.gridx = 0;
        c.gridy = 1;
        panel.add(new JLabel("Password:"), c);
        c.gridx = 1;
        c.gridy = 1;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, c);

        // Login Button
        c.gridx = 1;
        c.gridy = 2;
        c.anchor = GridBagConstraints.LINE_END;
        c.fill = GridBagConstraints.NONE;
        loginButton = new JButton("Login");
        panel.add(loginButton, c);

        // Status Label
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);
        panel.add(statusLabel, c);

        add(panel);

        // Add action listener for the login button
        loginButton.addActionListener(e -> onLogin());

        // Allow login by pressing Enter
        getRootPane().setDefaultButton(loginButton);
    }

    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username and password cannot be empty.");
            return;
        }

        try {
            // Call the AuthService
            // This will now either return a session OR throw an exception with a message
            UserSession session = authService.login(username, password);

            // If we get here, login was successful!
            statusLabel.setForeground(new Color(27, 161, 94)); // Green
            statusLabel.setText("Login Successful!");

            // Open the dashboard
            MainDashboard dashboard = new MainDashboard(session);
            dashboard.setVisible(true);

            // Close this window
            this.dispose();

        } catch (Exception e) {
            // Login Failed (Locked, Warning, or Wrong Password)
            // Show the specific message from AuthService in the UI
            statusLabel.setForeground(Color.RED);
            statusLabel.setText(e.getMessage());
        }
    }
}