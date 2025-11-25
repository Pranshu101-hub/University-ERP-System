package edu.univ.erp.service;

import edu.univ.erp.data.ErpDataStore;
import edu.univ.erp.auth.AuthService;
import edu.univ.erp.data.AuthDataStore;
import edu.univ.erp.domain.CourseSectionRow;
import edu.univ.erp.domain.LockedUser;
import edu.univ.erp.domain.UserRow;
import java.util.List;

public class AdminService {

    private final ErpDataStore erpDataStore;
    private final AuthService authService;
    private final AuthDataStore authDataStore;
    public AdminService() {
        this.erpDataStore=new ErpDataStore();
        this.authService=new AuthService();
        this.authDataStore=new AuthDataStore();
    }


    //creates a new course
    public String createCourse(String code, String title, String creditsStr) {
        // check input
        if (code.isEmpty() || title.isEmpty() || creditsStr.isEmpty()) {
            return "Error: All fields are required.";
        }
        int credits;
        try {
            credits=Integer.parseInt(creditsStr);
            if (credits <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            return "Error: Credits must be a positive number.";
        }
        boolean success=erpDataStore.createCourse(code, title, credits);
        if (success) {
            return "Success! Course created.";
        } else {
            return "Error: Could not create course (is the code a duplicate?).";
        }
    }


    //new section
    public String createSection(int courseId, Integer instructorId, String dayTime, String room,
                                String capacityStr, String semester, String yearStr) {

        // check input
        if (dayTime.isEmpty() || room.isEmpty() || capacityStr.isEmpty() || semester.isEmpty() || yearStr.isEmpty()) {
            return "Error: All fields are required.";
        }
        int capacity, year;
        try {
            capacity=Integer.parseInt(capacityStr);
            year=Integer.parseInt(yearStr);
            if (capacity <= 0 || year < 2020) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            return "Error: Capacity and Year must be valid positive numbers.";
        }
        boolean success=erpDataStore.createSection(courseId, instructorId, dayTime, room, capacity, semester, year);

        if (success) {
            return "Success! Section created.";
        } else {
            return "Error: Could not create section.";
        }
    }

    // new user (stu,inst,adm)
    public String createNewUser(String username, String password, String role,
                                String rollNo, String program, String yearStr,
                                String department, String title) {

        // 1. Validate inputs
        if (username.isEmpty() || password.isEmpty()) {
            return "Error: Username and Password are required.";
        }
        int newUserId=authService.createUser(username, password, role); //create in auth db
        if (newUserId== -1) {
            return "Error: Could not create user (username may be a duplicate).";
        }

        boolean profileSuccess=false;
        try {
            if ("Student".equals(role)) {
                int year=Integer.parseInt(yearStr);
                profileSuccess=erpDataStore.createStudentProfile(newUserId, rollNo, program, year);
            } else if ("Instructor".equals(role)) {
                profileSuccess=erpDataStore.createInstructorProfile(newUserId, department, title);
            } else if ("Admin".equals(role)) {
                profileSuccess=true;
            }
        } catch (Exception e) {
            return "Error: Profile information was invalid (e.g., Year must be a number).";
        }
        if (profileSuccess) {
            return "Success! User "+username+" created with ID "+newUserId+".";
        } else {
            return "Error: Auth user was created, but profile creation failed.";
        }
    }

    public List<UserRow> getAllUsers() {
        return authDataStore.getAllUsers();
    }
    //toggle maintenance mode
    public String setMaintenanceMode(boolean isEnabled) {
        String value;
        if (isEnabled) {
            value = "true";
        } else {
            value = "false";
        }
        boolean success=erpDataStore.updateSetting("maintenance_on", value);
        if (success) {
            return "Success! Maintenance mode is now "+(isEnabled ? "ON" : "OFF");
        } else {
            return "Error: Could not update maintenance mode.";
        }
    }

     //updates an existing section after validating input.
    public String updateSection(int sectionId, Integer instructorId, String dayTime, String room,
                                String capacityStr, String semester, String yearStr) {
// check inout
        if (dayTime.isEmpty() || room.isEmpty() || capacityStr.isEmpty() || semester.isEmpty() || yearStr.isEmpty()) {
            return "Error: All fields are required.";
        }

        int capacity, year;
        try {
            capacity=Integer.parseInt(capacityStr);
            year=Integer.parseInt(yearStr);
            if (capacity <= 0 || year < 2020) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            return "Error: Capacity and Year must be valid positive numbers.";
        }
        boolean success=erpDataStore.updateSection(sectionId, instructorId, dayTime, room, capacity, semester, year);

        if (success) {
            return "Success! Section updated.";
        } else {
            return "Error: Could not update section.";
        }

    }

    //deletes a course if no sections
    public String deleteCourse(int courseId) {
        if (erpDataStore.hasSections(courseId)) {//check for secrtions
            return "Error: Cannot delete course. It has sections attached. "+"Please delete all sections for this course first.";
        }
        // 2. All checks passed. Proceed with deletion.
        boolean success=erpDataStore.deleteCourse(courseId);

        if (success) {
            return "Success! Course has been deleted.";
        } else {
            return "Error: Could not delete course.";
        }

    }
    public List<LockedUser> getLockedUsers() {
        return authDataStore.getUsersWithFailedAttempts();
    }

    public String unlockUser(String username) {
        authDataStore.resetFailedAttempts(username);
        return "Success! Account for "+username+" has been unlocked.";
    }

    //resets a specific user's password
    public String adminResetPassword(String username, String newPassword) {
        if (newPassword== null || newPassword.trim().length() < 8) {
            return "Error: Password must be at least 8 characters.";
        }
        boolean success=authService.resetPasswordForUser(username, newPassword);
        if (success){
            return "Success! Password reset and account unlocked.";
        } else {
            return "Error: Could not reset password.";
        }
    }
    /**
     * Deletes a section ONLY if it has no students.
     */
    public String deleteSection(int sectionId) {
        // 1. Check for students
        if (erpDataStore.hasEnrollments(sectionId)) {
            return "Error: Cannot delete section. Students are enrolled!";
        }

        // 2. Delete
        boolean success = erpDataStore.deleteSection(sectionId);
        return success ? "Success! Section deleted." : "Error: Delete failed.";
    }

    // Helper to get the list for the UI
    public List<CourseSectionRow> getAllSections() {
        return erpDataStore.getAllSections();
    }
}