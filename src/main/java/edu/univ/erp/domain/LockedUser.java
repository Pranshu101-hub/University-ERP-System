package edu.univ.erp.domain;

public class LockedUser {
    private final String username;
    private final String role;
    private final int failedAttempts;

    public LockedUser(String username, String role, int failedAttempts) {
        this.username=username;
        this.role=role;
        this.failedAttempts=failedAttempts;
    }

    public String getUsername() { return username; }
    public String getRole() { return role; }
    public int getFailedAttempts() { return failedAttempts; }
    public String getStatus() {
        if (failedAttempts >= 5) {
            return "LOCKED";
        } else {
            return "Warning (" + failedAttempts + "/5)";
        }
    }
}