package edu.univ.erp.ui;// Package: edu.univ.erp.ui.common

import edu.univ.erp.access.AccessChecker;
import edu.univ.erp.auth.UserSession;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

//main dashboard after login
public class MainDashboard extends JFrame {
    //all panels
    private final UserSession session;
    private final AccessChecker accessChecker;
    private final boolean isMaintenanceOn;
    private MyRegistrationsPanel myRegistrationsPanel;
    private MyGradesPanel myGradesPanel; // <-- ADD THIS
    private MySectionsPanel mySectionsPanel;
    private MyTimetablePanel myTimetablePanel;
    private AdminCoursePanel adminCoursePanel;
    private AdminMaintenancePanel adminMaintenancePanel;
    private CourseCatalogPanel courseCatalogPanel;
    private AdminUnlockPanel adminUnlockPanel;
    public MainDashboard(UserSession session) {
        this.session=session;
        this.accessChecker=new AccessChecker();
        this.isMaintenanceOn=accessChecker.isMaintenanceOn();
        setTitle("ERP Dashboard");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());// main layout
        JPanel topPanel=createTopPanel(); //top panel
        add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane=createTabbedPane(); //tabs
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Component selectedPanel=tabbedPane.getSelectedComponent(); //panels for all tabs
                if (selectedPanel == courseCatalogPanel) {
                    courseCatalogPanel.loadCatalogData();
                }
                else if (selectedPanel == myRegistrationsPanel) {
                    myRegistrationsPanel.loadRegistrationData();
                }
                else if (selectedPanel == myGradesPanel) {
                    myGradesPanel.loadGradesData();
                }
                else if (selectedPanel == myTimetablePanel) {
                    myTimetablePanel.loadTimetableData();
                }
                else if (selectedPanel == adminCoursePanel) {
                    adminCoursePanel.refreshAllData();
                }
                else if (selectedPanel == mySectionsPanel) {
                    mySectionsPanel.loadSectionsData();
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
        JPanel topPanel=new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        String role=session.getRole();
        JLabel welcomeLabel=new JLabel("Welcome, " + role + " (User ID: " + session.getUserId() + ")"); // welcome msg on the left
        welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(Font.BOLD));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        if (isMaintenanceOn && !session.isAdmin()) { // maintenance mode placement
            JLabel maintenanceBanner=new JLabel("MAINTENANCE MODE IS ON: All changes are disabled.");
            maintenanceBanner.setFont(maintenanceBanner.getFont().deriveFont(Font.BOLD));
            maintenanceBanner.setForeground(Color.RED);
            maintenanceBanner.setHorizontalAlignment(SwingConstants.CENTER);
            topPanel.add(maintenanceBanner, BorderLayout.CENTER);
        }
        JButton logoutButton=new JButton("Logout");
        logoutButton.addActionListener(e -> {
            new LoginWindow().setVisible(true); // open a new login window
            dispose();//close this dashboard
        });
        topPanel.add(logoutButton, BorderLayout.EAST); // logout button
        return topPanel;
    }

    private JTabbedPane createTabbedPane() { //common tabs
        JTabbedPane tabbedPane=new JTabbedPane();
        this.courseCatalogPanel=new CourseCatalogPanel(session); //Course Catalog for EVERYONE
        tabbedPane.addTab("Course Catalog", this.courseCatalogPanel);
        switch (session.getRole()) { // role-specific tabs
            case "Student": createStudentTabs(tabbedPane); break;
            case "Instructor": createInstructorTabs(tabbedPane); break;
            case "Admin": createAdminTabs(tabbedPane); break;
        }
        tabbedPane.addTab("My Profile", new MyProfilePanel(session)); // My Profile for EVERYONE
        return tabbedPane;
    }

    private void createStudentTabs(JTabbedPane tabs) { //student tabs
        this.myRegistrationsPanel=new MyRegistrationsPanel(session);
        tabs.addTab("My Registrations", this.myRegistrationsPanel);
        this.myTimetablePanel=new MyTimetablePanel(session);
        tabs.addTab("My Timetable", this.myTimetablePanel);
        this.myGradesPanel=new MyGradesPanel(session);
        tabs.addTab("My Grades", this.myGradesPanel);
    }

    private void createInstructorTabs(JTabbedPane tabs) { //inst tabs
        this.mySectionsPanel=new MySectionsPanel(session);
        tabs.addTab("My Sections", this.mySectionsPanel);
    }

    private void createAdminTabs(JTabbedPane tabs) { //admin tabs
        this.adminCoursePanel=new AdminCoursePanel(session);
        tabs.addTab("Course Management", this.adminCoursePanel);
        tabs.addTab("User Management", new AdminUserPanel());
        this.adminMaintenancePanel=new AdminMaintenancePanel();
        tabs.addTab("Maintenance", this.adminMaintenancePanel);
        this.adminUnlockPanel=new AdminUnlockPanel();
        tabs.addTab("Unlock Users", this.adminUnlockPanel);
    }
}