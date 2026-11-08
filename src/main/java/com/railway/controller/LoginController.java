package com.railway.controller;

import com.railway.booking.backend.model.Session;
import com.railway.booking.backend.model.User;
import com.railway.booking.backend.service.UserService;
import com.railway.booking.backend.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Login and create session
     * 
     * @param username Username
     * @param password Password
     * @return Session if login successful, null otherwise
     */
    public Session login(String username, String password) {
        logger.info("Login attempt for username: {}", username);

        User user = userService.login(username, password);
        if (user != null) {
            Session session = SessionManager.createSession(user);
            logger.info("Login successful for user: {}, session created: {}", username, session.getSessionId());
            return session;
        } else {
            logger.warn("Login failed for username: {}", username);
            return null;
        }
    }

    /**
     * Logout - invalidate session
     * 
     * @param sessionId Session ID to logout
     */
    public void logout(String sessionId) {
        if (sessionId != null) {
            logger.info("Logout request for session: {}", sessionId);
            SessionManager.logout(sessionId);
        }
    }
}
