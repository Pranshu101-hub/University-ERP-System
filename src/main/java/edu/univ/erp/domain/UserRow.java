package edu.univ.erp.domain;

public class UserRow {
    private final int userId;
    private final String username;
    private final String role;
    private final String status;

    public UserRow(int userId, String username, String role, String status) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.status = status;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
}