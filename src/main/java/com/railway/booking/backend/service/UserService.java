package com.railway.booking.backend.service;

import com.railway.booking.backend.dao.UserDAO;
import com.railway.booking.backend.model.User;
import com.railway.booking.backend.util.ValidationUtils;
import com.railway.booking.backend.util.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Register new user with validation
     */
    public String registerUser(User user) {
        // Guard against null input
        if (user == null) {
            logger.warn("registerUser called with null user");
            return "Invalid user data.";
        }

        logger.info("Attempting to register new user: {}", user.getUsername());

        // Validate username
        if (!ValidationUtils.isValidUsername(user.getUsername())) {
            logger.warn("Invalid username format: {}", user.getUsername());
            return "Invalid username. Use 3-20 alphanumeric characters.";
        }

        // Validate password
        if (!ValidationUtils.isValidPassword(user.getPassword())) {
            logger.warn("Invalid password format for user: {}", user.getUsername());
            return "Password must be at least 6 characters long.";
        }

        // Validate email
        if (!ValidationUtils.isValidEmail(user.getEmail())) {
            logger.warn("Invalid email format: {}", user.getEmail());
            return "Invalid email address.";
        }

        // Validate phone
        if (!ValidationUtils.isValidPhone(user.getPhone())) {
            logger.warn("Invalid phone format: {}", user.getPhone());
            return "Invalid phone number. Use 10-digit Indian format.";
        }

        // Check if username exists
        if (userDAO.isUsernameExists(user.getUsername())) {
            logger.warn("Username already exists: {}", user.getUsername());
            return "Username already exists.";
        }

        // Check if email exists
        if (userDAO.isEmailExists(user.getEmail())) {
            logger.warn("Email already registered: {}", user.getEmail());
            return "Email already registered.";
        }

        // Hash the password using BCrypt
        try {
            String plainPassword = user.getPassword();
            String hashedPassword = PasswordHasher.hashPassword(plainPassword);
            user.setPassword(hashedPassword);
            logger.debug("Password hashed successfully for user: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Failed to hash password for user: {}", user.getUsername(), e);
            return "Registration failed. Please try again.";
        }

        // Register user
        boolean success = userDAO.registerUser(user);

        if (success) {
            logger.info("User registered successfully: {}", user.getUsername());
            return "SUCCESS";
        } else {
            logger.error("Failed to register user in database: {}", user.getUsername());
            return "Registration failed. Please try again.";
        }
    }

    /**
     * Register new user (boolean return for controller)
     */
    public boolean registerUserBoolean(User user) {
        String result = registerUser(user);
        return "SUCCESS".equals(result);
    }

    /**
     * Check if username exists
     */
    public boolean userExists(String username) {
        return userDAO.isUsernameExists(username);
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        return userDAO.isEmailExists(email);
    }

    /**
     * Authenticate user login with support for both BCrypt and legacy plain text
     * passwords
     */
    public User login(String username, String password) {
        logger.info("Login attempt for user: {}", username);

        if (!ValidationUtils.isNotEmpty(username) || !ValidationUtils.isNotEmpty(password)) {
            logger.warn("Empty username or password in login attempt");
            return null;
        }

        // Get user from database
        User user = userDAO.getUserByUsername(username);

        if (user == null) {
            logger.warn("Login failed - user not found: {}", username);
            return null;
        }

        String storedPassword = user.getPassword();

        // Check if password is BCrypt hashed
        if (PasswordHasher.isBCryptHash(storedPassword)) {
            // Verify BCrypt password
            if (PasswordHasher.verifyPassword(password, storedPassword)) {
                logger.info("Login successful for user: {}", username);

                // Check if password needs rehashing (e.g., work factor changed)
                if (PasswordHasher.needsRehash(storedPassword)) {
                    logger.info("Password needs rehashing for user: {}", username);
                    rehashUserPassword(user.getUserId(), password);
                }

                return user;
            } else {
                logger.warn("Login failed - invalid password for user: {}", username);
                return null;
            }
        } else {
            // Legacy plain text password - verify and upgrade to BCrypt
            if (storedPassword.equals(password)) {
                logger.info("Login successful with legacy password for user: {}", username);
                logger.info("Upgrading password to BCrypt for user: {}", username);

                // Upgrade to BCrypt
                try {
                    String hashedPassword = PasswordHasher.hashPassword(password);
                    userDAO.updateUserPassword(user.getUserId(), hashedPassword);
                    logger.info("Password upgraded to BCrypt for user: {}", username);
                } catch (Exception e) {
                    logger.error("Failed to upgrade password to BCrypt for user: {}", username, e);
                    // Continue with login even if upgrade fails
                }

                return user;
            } else {
                logger.warn("Login failed - invalid legacy password for user: {}", username);
                return null;
            }
        }
    }

    /**
     * Rehash a user's password with updated work factor
     */
    private void rehashUserPassword(int userId, String plainPassword) {
        try {
            String newHash = PasswordHasher.hashPassword(plainPassword);
            userDAO.updateUserPassword(userId, newHash);
            logger.info("Password rehashed successfully for user ID: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to rehash password for user ID: {}", userId, e);
        }
    }

    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        return userDAO.getUserById(userId);
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    /**
     * Update user profile
     */
    public String updateUser(User user) {
        logger.info("Attempting to update user: {}", user.getUsername());

        // Validate email
        if (!ValidationUtils.isValidEmail(user.getEmail())) {
            logger.warn("Invalid email format during update: {}", user.getEmail());
            return "Invalid email address.";
        }

        // Validate phone
        if (!ValidationUtils.isValidPhone(user.getPhone())) {
            logger.warn("Invalid phone format during update: {}", user.getPhone());
            return "Invalid phone number.";
        }

        boolean success = userDAO.updateUser(user);

        if (success) {
            logger.info("User updated successfully: {}", user.getUsername());
            return "SUCCESS";
        } else {
            logger.error("Failed to update user: {}", user.getUsername());
            return "Update failed. Please try again.";
        }
    }

    /**
     * Change user password
     */
    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        logger.info("Password change attempt for user ID: {}", userId);

        User user = userDAO.getUserById(userId);
        if (user == null) {
            logger.warn("Password change failed - user not found: {}", userId);
            return false;
        }

        String storedPassword = user.getPassword();

        // Verify current password
        boolean currentPasswordValid = false;
        if (PasswordHasher.isBCryptHash(storedPassword)) {
            currentPasswordValid = PasswordHasher.verifyPassword(currentPassword, storedPassword);
        } else {
            // Legacy plain text
            currentPasswordValid = storedPassword.equals(currentPassword);
        }

        if (!currentPasswordValid) {
            logger.warn("Password change failed - invalid current password for user ID: {}", userId);
            return false;
        }

        // Validate new password
        if (!ValidationUtils.isValidPassword(newPassword)) {
            logger.warn("Password change failed - invalid new password format for user ID: {}", userId);
            return false;
        }

        // Hash and update new password
        try {
            String hashedPassword = PasswordHasher.hashPassword(newPassword);
            boolean success = userDAO.updateUserPassword(userId, hashedPassword);

            if (success) {
                logger.info("Password changed successfully for user ID: {}", userId);
            } else {
                logger.error("Failed to update password in database for user ID: {}", userId);
            }

            return success;
        } catch (Exception e) {
            logger.error("Error changing password for user ID: {}", userId, e);
            return false;
        }
    }
}
