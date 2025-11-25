package edu.univ.erp.domain;

public class AuthUser {
    private final int userId;
    private final String username;
    private final String passwordHash;
    private final String role;

    public AuthUser(int userId, String username, String passwordHash, String role) {
        this.userId =userId;
        this.username =username;
        this.passwordHash =passwordHash;
        this.role =role;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
}