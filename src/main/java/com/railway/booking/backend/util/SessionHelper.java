package com.railway.booking.backend.util;

import com.railway.booking.backend.model.Session;
import com.railway.booking.backend.model.User;

/**
 * Helper utility to convert between Session and User objects
 */
public class SessionHelper {
    
    /**
     * Extract User object from Session
     * Note: This creates a minimal User object with only session data
     * Full user details should be loaded from database if needed
     */
    public static User getUserFromSession(Session session) {
        if (session == null) {
            return null;
        }
        
        User user = new User();
        user.setUserId(session.getUserId());
        user.setUsername(session.getUsername());
        user.setUserType(session.getUserType());
        return user;
    }
    
    /**
     * Create Session from User (for backward compatibility)
     * Note: This should only be used during migration
     */
    public static Session createSessionFromUser(User user) {
        if (user == null) {
            return null;
        }
        
        return SessionManager.createSession(user);
    }
}
