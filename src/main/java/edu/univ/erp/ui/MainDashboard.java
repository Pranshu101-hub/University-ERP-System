package edu.univ.erp.ui; // Flat UI package

import edu.univ.erp.access.AccessChecker;
import edu.univ.erp.auth.UserSession;
import edu.univ.erp.ui.AdminCoursePanel;
import edu.univ.erp.ui.AdminMaintenancePanel;
import edu.univ.erp.ui.AdminUnlockPanel;
import edu.univ.erp.ui.AdminUserPanel;
import edu.univ.erp.ui.CourseCatalogPanel;
import edu.univ.erp.ui.LoginWindow;
import edu.univ.erp.ui.MyGradesPanel;
import edu.univ.erp.ui.MyProfilePanel;
import edu.univ.erp.ui.MyRegistrationsPanel;
import edu.univ.erp.ui.MySectionsPanel;
import edu.univ.erp.ui.MyTimetablePanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class MainDashboard extends JFrame {

    private final UserSession session;
    private final AccessChecker accessChecker;
    private final boolean isMaintenanceOn;

    // --- Class Variables (Must be declared here!) ---
    private CourseCatalogPanel courseCatalogPanel;

    private MyRegistrationsPanel myRegistrationsPanel;
    private MyTimetablePanel myTimetablePanel;
    private MyGradesPanel myGradesPanel;

    private MySectionsPanel mySectionsPanel;

    private AdminCoursePanel adminCoursePanel;
    private AdminMaintenancePanel adminMaintenancePanel;
    private AdminUnlockPanel adminUnlockPanel;

    public MainDashboard(UserSession session) {
        this.session = session;
        this.accessChecker = new AccessChecker();
        this.isMaintenanceOn = accessChecker.isMaintenanceOn();

        setTitle("University ERP Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Top Panel
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // 2. Create Tabs
        JTabbedPane tabbedPane = createTabbedPane();

        // 3. Add Listener for Refreshing
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Component selectedPanel = tabbedPane.getSelectedComponent();

                // Check which panel is selected and refresh it
                if (selectedPanel == courseCatalogPanel) {
                    courseCatalogPanel.loadCatalogData();
                }
                else if (selectedPanel == myRegistrationsPanel) {
                    myRegistrationsPanel.loadRegistrationData();
                }
                else if (selectedPanel == myTimetablePanel) {
                    // This refreshes the timetable when you click the tab
                    myTimetablePanel.loadTimetableData();
                }
                else if (selectedPanel == myGradesPanel) {
                    myGradesPanel.loadGradesData();
                }
                else if (selectedPanel == mySectionsPanel) {
                    mySectionsPanel.loadSectionsData();
                }
                else if (selectedPanel == adminCoursePanel) {
                    adminCoursePanel.refreshAllData();
                }
                else if (selectedPanel == adminMaintenancePanel) {
                    adminMaintenancePanel.updateStatus();
                }
                else if (selectedPanel == adminUnlockPanel) {
                    adminUnlockPanel.loadData();
                }
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        String role = session.getRole();
        JLabel welcomeLabel = new JLabel("Welcome, " + role + " (User ID: " + session.getUserId() + ")");
        welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(Font.BOLD));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        if (isMaintenanceOn && !session.isAdmin()) {
            JLabel maintenanceBanner = new JLabel("MAINTENANCE MODE IS ON: All changes are disabled.");
            maintenanceBanner.setFont(maintenanceBanner.getFont().deriveFont(Font.BOLD));
            maintenanceBanner.setForeground(Color.RED);
            maintenanceBanner.setHorizontalAlignment(SwingConstants.CENTER);
            topPanel.add(maintenanceBanner, BorderLayout.CENTER);
        }

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            new LoginWindow().setVisible(true);
            dispose();
        });
        topPanel.add(logoutButton, BorderLayout.EAST);

        return topPanel;
    }

    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // 1. Course Catalog (For Everyone)
        this.courseCatalogPanel = new CourseCatalogPanel(session);
        tabbedPane.addTab("Course Catalog", this.courseCatalogPanel);

        // 2. Role-Specific Tabs
        switch (session.getRole()) {
            case "Student":
                createStudentTabs(tabbedPane);
                break;
            case "Instructor":
                createInstructorTabs(tabbedPane);
                break;
            case "Admin":
                createAdminTabs(tabbedPane);
                break;
        }

        // 3. Profile (For Everyone)
        tabbedPane.addTab("My Profile", new MyProfilePanel(session));

        return tabbedPane;
    }

    private void createStudentTabs(JTabbedPane tabs) {
        this.myRegistrationsPanel = new MyRegistrationsPanel(session);
        tabs.addTab("My Registrations", this.myRegistrationsPanel);

        // --- CRITICAL FIX ---
        // Assign to the CLASS variable 'this.myTimetablePanel'
        // Do NOT write 'MyTimetablePanel myTimetablePanel = ...'
        this.myTimetablePanel = new MyTimetablePanel(session);
        tabs.addTab("My Timetable", this.myTimetablePanel);
        // --------------------

        this.myGradesPanel = new MyGradesPanel(session);
        tabs.addTab("My Grades", this.myGradesPanel);
    }

    private void createInstructorTabs(JTabbedPane tabs) {
        this.mySectionsPanel = new MySectionsPanel(session);
        tabs.addTab("My Sections", this.mySectionsPanel);
    }

    private void createAdminTabs(JTabbedPane tabs) {
        this.adminCoursePanel = new AdminCoursePanel(session);
        tabs.addTab("Course Management", this.adminCoursePanel);

        tabs.addTab("User Management", new AdminUserPanel());

        this.adminMaintenancePanel = new AdminMaintenancePanel();
        tabs.addTab("Maintenance", this.adminMaintenancePanel);

        this.adminUnlockPanel = new AdminUnlockPanel();
        tabs.addTab("Unlock Users", this.adminUnlockPanel);
    }
}