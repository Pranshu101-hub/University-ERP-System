package edu.univ.erp.data;

import edu.univ.erp.domain.AuthUser;
import edu.univ.erp.domain.LockedUser;
import edu.univ.erp.domain.UserRow;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthDataStore {

    private final MySqlConnectionManager connectionManager;

    public AuthDataStore() {
        this.connectionManager = MySqlConnectionManager.getInstance();
    }

    // --- 1. LOGIN & BASIC LOOKUP ---

    public AuthUser findUserByUsername(String username) {
        String sql = "SELECT user_id, username, password_hash, role FROM users_auth WHERE username = ?";
        try (Connection conn = connectionManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AuthUser(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public AuthUser findUserById(int userId) {
        String sql = "SELECT user_id, username, password_hash, role FROM users_auth WHERE user_id = ?";
        try (Connection conn = connectionManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AuthUser(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // --- 2. USER MANAGEMENT (ADMIN) ---

    public int createUser(String username, String passwordHash, String role) {
        String sql = "INSERT INTO users_auth (username, password_hash, role) VALUES (?, ?, ?)";
        try (Connection conn = connectionManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, role);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public List<UserRow> getAllUsers() {
        List<UserRow> users = new ArrayList<>();
        String sql = "SELECT user_id, username, role, status FROM users_auth ORDER BY role, username";
        try (Connection conn = connectionManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(new UserRow(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    // --- 3. PASSWORD CHANGE ---

    public boolean updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE users_auth SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = connectionManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 4. ACCOUNT LOCKOUT LOGIC (BONUS) ---

    public void incrementFailedAttempts(String username) {
        String sql = "UPDATE users_auth SET failed_attempts = failed_attempts + 1 WHERE username = ?";
        try (Connection conn = connectionManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void resetFailedAttempts(String username) {
        String sql = "UPDATE users_auth SET failed_attempts = 0 WHERE username = ?";
        try (Connection conn = connectionManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public int getFailedAttempts(String username) {
        String sql = "SELECT failed_attempts FROM users_auth WHERE username = ?";
        try (Connection conn = connectionManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("failed_attempts");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<LockedUser> getUsersWithFailedAttempts() {
        List<LockedUser> list = new ArrayList<>();
        String sql = "SELECT username, role, failed_attempts FROM users_auth WHERE failed_attempts > 0";
        try (Connection conn = connectionManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new LockedUser(
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getInt("failed_attempts")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}