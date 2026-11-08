package com.railway.controller;

import com.railway.booking.backend.model.User;
import com.railway.booking.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Registration controller — delegates all validation to UserService.
 * No UI code here; the view layer handles displaying messages to the user.
 */
@Component
public class RegistrationController {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);
    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Register a new user.
     * Validation is handled entirely by UserService (single source of truth).
     *
     * @param user The user object containing registration details
     * @return A result message: "SUCCESS" on success, or an error description
     *         string
     */
    public String registerUser(User user) {
        logger.info("Registration request for username: {}", user != null ? user.getUsername() : "null");

        if (user == null) {
            return "Invalid user data.";
        }

        // Delegate all validation and persistence to the service layer
        String result = userService.registerUser(user);

        if ("SUCCESS".equals(result)) {
            logger.info("User registered successfully: {}", user.getUsername());
        } else {
            logger.warn("Registration failed for user {}: {}", user.getUsername(), result);
        }

        return result;
    }

    /**
     * Boolean convenience wrapper for Swing UI callers.
     */
    public boolean registerUserBoolean(User user) {
        return "SUCCESS".equals(registerUser(user));
    }
}
