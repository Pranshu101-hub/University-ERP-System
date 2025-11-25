package edu.univ.erp.auth;

public class UserSession {
    private final int userId;
    private final String role;

    public UserSession(int userId, String role) {
        this.userId=userId;
        this.role=role;
    }

    public int getUserId() { return userId; }
    public String getRole() { return role; }

    public boolean isAdmin() { return "Admin".equals(role); }
    public boolean isInstructor() { return "Instructor".equals(role); }
    public boolean isStudent() { return "Student".equals(role); }
}