package com.railway.controller;

import com.railway.booking.backend.dao.UserDAO;
import com.railway.booking.backend.model.Session;
import com.railway.booking.backend.model.User;
import com.railway.booking.backend.service.UserService;
import com.railway.booking.backend.util.AuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import org.springframework.stereotype.Component;

@Component
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private UserDAO userDAO;
    private final UserService userService;
    
    public UserController(UserService userService, UserDAO userDAO) {
        this.userService = userService;
        this.userDAO = userDAO;
    }
    
    /**
     * Update user profile (owner or admin only)
     */
    public boolean updateUser(Session session, User user) {
        AuthorizationManager.requireOwnerOrAdmin(session, user.getUserId(), "update user profile");
        logger.info("User {} updating profile for user ID: {}", session.getUsername(), user.getUserId());
        return userDAO.updateUser(user);
    }
    
    /**
     * Change user password - uses UserService to ensure BCrypt hashing (owner or admin only)
     */
    public boolean changePassword(Session session, int userId, String currentPassword, String newPassword) {
        AuthorizationManager.requireOwnerOrAdmin(session, userId, "change password");
        logger.info("User {} changing password for user ID: {}", session.getUsername(), userId);
        return userService.changePassword(userId, currentPassword, newPassword);
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        return userDAO.getUserById(userId);
    }
    
    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        return userDAO.getUserByUsername(username);
    }
    
    /**
     * Check if username exists
     */
    public boolean isUsernameExists(String username) {
        return userDAO.isUsernameExists(username);
    }
    
    /**
     * Check if email exists
     */
    public boolean isEmailExists(String email) {
        return userDAO.isEmailExists(email);
    }
    
    /**
     * Get all users (ADMIN only)
     */
    public java.util.List<User> getAllUsers(Session session) {
        AuthorizationManager.requireAdmin(session, "get all users");
        logger.info("Admin {} retrieving all users", session.getUsername());
        return userDAO.getAllUsers();
    }
    
    /**
     * Delete user (ADMIN only)
     */
    public boolean deleteUser(Session session, int userId) {
        AuthorizationManager.requireAdmin(session, "delete user");
        logger.info("Admin {} deleting user ID: {}", session.getUsername(), userId);
        return userDAO.deleteUser(userId);
    }
}

