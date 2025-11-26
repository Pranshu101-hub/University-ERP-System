package edu.univ.erp;

import com.formdev.flatlaf.FlatLightLaf;
import edu.univ.erp.data.DatabaseConfig;
import edu.univ.erp.data.MySqlConnectionManager;
import edu.univ.erp.ui.LoginWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JOptionPane;


public class Main {

    public static void main(String[] args) {
        try {
           //for ui setup (FlatLaf)
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 999);
            UIManager.put("Component.arc", 999);
            UIManager.put("ProgressBar.arc", 999);
            UIManager.put("TextComponent.arc", 999);

            //  initialize db conns
            DatabaseConfig authConfig=new DatabaseConfig(
                    "jdbc:mysql://localhost:3306/auth_db", "root", "ospansu");  
            DatabaseConfig erpConfig=new DatabaseConfig(
                    "jdbc:mysql://localhost:3306/erp_db", "root", "ospansu");

            MySqlConnectionManager.init(authConfig, erpConfig);
            System.out.println("Database connection pools initialized.");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Closing database connections...");
                MySqlConnectionManager.getInstance().closeDataSources();
            }));
            //start the swing ui
            SwingUtilities.invokeLater(() -> {
                LoginWindow loginWindow=new LoginWindow();
                loginWindow.setVisible(true);
            });

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to start application: " + e.getMessage());
            // Show a simple Swing error dialog
            JOptionPane.showMessageDialog(null,
                    "Application failed to start.\n" + e.getMessage(),
                    "Startup Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}