package edu.univ.erp.auth;

import edu.univ.erp.data.AuthDataStore;
import edu.univ.erp.domain.AuthUser;
import org.mindrot.jbcrypt.BCrypt;
import edu.univ.erp.access.AccessChecker;
public class AuthService {

    private final AuthDataStore authDataStore;
    private final AccessChecker accessChecker;
    public AuthService() {
        this.authDataStore=new AuthDataStore();
        this.accessChecker = new AccessChecker();
    }
    //attempts to log in a user
    public UserSession login(String username, String typedPassword) throws Exception {

        // check if locked
        int failures=authDataStore.getFailedAttempts(username);
        if (failures >= 5) {
            throw new Exception("ACCOUNT LOCKED: Too many failed attempts. Contact Admin.");
        }

        // find user
        AuthUser authUser=authDataStore.findUserByUsername(username);
        if (authUser == null) {
            throw new Exception("Incorrect username or password.");
        }
        // check Password
        if (BCrypt.checkpw(typedPassword, authUser.getPasswordHash())) {
            authDataStore.resetFailedAttempts(username); // reset password counter to 0
            return new UserSession(authUser.getUserId(), authUser.getRole()); //userSession if successful
        } else {
            authDataStore.incrementFailedAttempts(username);
            int currentFailures=failures + 1;

            if (currentFailures >= 5) {
                throw new Exception("ACCOUNT LOCKED: Maximum attempts reached.");
            } else if (currentFailures == 4) {
                throw new Exception("Incorrect password. WARNING: 1 attempt remaining!");
            } else if (currentFailures == 3) {
                throw new Exception("Incorrect password. WARNING: 2 attempts remaining!");
            } else if (currentFailures == 2) {
                throw new Exception("Incorrect password. WARNING: 3 attempts remaining!");
            } else {
                throw new Exception("Incorrect password. WARNING: 4 attempts remaining!");
            }
        }
    }

    //creates a new user in auth system
    public int createUser(String username, String plainPassword, String role) {
        String passwordHash=BCrypt.hashpw(plainPassword, BCrypt.gensalt());// hash the password
        return authDataStore.createUser(username, passwordHash, role); //user's ID if successful
    }

    //changes a user's password
    public String changePassword(int userId, String oldPassword, String newPassword, String confirmPassword) {
        AuthUser authUser = authDataStore.findUserById(userId);
        if (authUser == null) {
            return "Error: User not found.";
        }
        if (accessChecker.isMaintenanceOn() && !"Admin".equals(authUser.getRole())) {
            return "Error: Maintenance Mode is ON. Password changes are disabled.";
        }
        //validate new password
        if (newPassword.isEmpty() || newPassword.length() < 8) {return "Error: New password must be at least 8 characters long.";}
        if (!newPassword.equals(confirmPassword)) {return "Error: New passwords do not match.";}
        if (authUser == null) {return "Error: User not found.";}
        if (!BCrypt.checkpw(oldPassword, authUser.getPasswordHash())) { //check if the old password is correct
            return "Error: Incorrect old password.";
        }
        String newPasswordHash=BCrypt.hashpw(newPassword, BCrypt.gensalt()); //hash the new password
        boolean success=authDataStore.updatePassword(userId, newPasswordHash); // save the new hash
        if (success) {
            return "Success! Password has been changed.";
        } else {
            return "Error: Could not update password.";
        }
    }

    //admin force-reset of a user's password and unlocks the account
    public boolean resetPasswordForUser(String username, String newPlainPassword) {
        AuthUser user=authDataStore.findUserByUsername(username); //get user by id
        if (user == null) return false;
        String newHash=BCrypt.hashpw(newPlainPassword, BCrypt.gensalt()); //hash new password
        boolean success=authDataStore.updatePassword(user.getUserId(), newHash);//upadte in db
        //unlock the account
        if (success) {
            authDataStore.resetFailedAttempts(username);
        }
        return success;
    }

}