package com.railway.booking.backend.util;

import com.railway.booking.backend.exception.UnauthorizedException;
import com.railway.booking.backend.model.Session;
import com.railway.booking.backend.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authorization manager for role-based access control
 */
public class AuthorizationManager {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationManager.class);
    
    /**
     * Check if user has required role
     * 
     * @param session User session
     * @param requiredRole Required role
     * @param action Action being attempted
     * @throws UnauthorizedException if user doesn't have required role
     */
    public static void requireRole(Session session, User.UserType requiredRole, String action) {
        if (session == null) {
            logger.warn("Authorization failed: null session for action: {}", action);
            throw new UnauthorizedException("Session required for action: " + action);
        }
        
        User.UserType userRole = session.getUserType();
        
        if (userRole != requiredRole) {
            logger.warn("Authorization failed: user {} (role: {}) attempted {} (requires: {})", 
                session.getUsername(), userRole, action, requiredRole);
            throw new UnauthorizedException(session.getUsername(), requiredRole.name(), action);
        }
        
        logger.debug("Authorization successful: user {} performed {}", session.getUsername(), action);
    }
    
    /**
     * Check if user has ADMIN role
     * 
     * @param session User session
     * @param action Action being attempted
     * @throws UnauthorizedException if user is not an admin
     */
    public static void requireAdmin(Session session, String action) {
        requireRole(session, User.UserType.ADMIN, action);
    }
    
    /**
     * Check if user has USER role (regular user)
     * 
     * @param session User session
     * @param action Action being attempted
     * @throws UnauthorizedException if user is not a regular user
     */
    public static void requireUser(Session session, String action) {
        requireRole(session, User.UserType.USER, action);
    }
    
    /**
     * Check if user is accessing their own resource
     * 
     * @param session User session
     * @param resourceUserId User ID of the resource being accessed
     * @param action Action being attempted
     * @throws UnauthorizedException if user is not the owner and not an admin
     */
    public static void requireOwnerOrAdmin(Session session, int resourceUserId, String action) {
        if (session == null) {
            logger.warn("Authorization failed: null session for action: {}", action);
            throw new UnauthorizedException("Session required for action: " + action);
        }
        
        // Allow if user is the owner
        if (session.getUserId() == resourceUserId) {
            logger.debug("Authorization successful: user {} is owner for {}", session.getUsername(), action);
            return;
        }
        
        // Allow if user is admin
        if (session.getUserType() == User.UserType.ADMIN) {
            logger.debug("Authorization successful: admin {} performed {}", session.getUsername(), action);
            return;
        }
        
        // Deny access
        logger.warn("Authorization failed: user {} (ID: {}) attempted to access resource owned by user ID {} for action: {}", 
            session.getUsername(), session.getUserId(), resourceUserId, action);
        throw new UnauthorizedException(
            String.format("User '%s' is not authorized to %s. Must be resource owner or admin.", 
                session.getUsername(), action));
    }
    
    /**
     * Check if user is authenticated (has valid session)
     * 
     * @param session User session
     * @param action Action being attempted
     * @throws UnauthorizedException if session is null
     */
    public static void requireAuthenticated(Session session, String action) {
        if (session == null) {
            logger.warn("Authorization failed: null session for action: {}", action);
            throw new UnauthorizedException("Authentication required for action: " + action);
        }
        
        logger.debug("Authorization successful: authenticated user {} for {}", session.getUsername(), action);
    }
    
    /**
     * Check if user has any of the specified roles
     * 
     * @param session User session
     * @param allowedRoles Allowed roles
     * @param action Action being attempted
     * @throws UnauthorizedException if user doesn't have any of the allowed roles
     */
    public static void requireAnyRole(Session session, User.UserType[] allowedRoles, String action) {
        if (session == null) {
            logger.warn("Authorization failed: null session for action: {}", action);
            throw new UnauthorizedException("Session required for action: " + action);
        }
        
        User.UserType userRole = session.getUserType();
        
        for (User.UserType allowedRole : allowedRoles) {
            if (userRole == allowedRole) {
                logger.debug("Authorization successful: user {} (role: {}) performed {}", 
                    session.getUsername(), userRole, action);
                return;
            }
        }
        
        // Build allowed roles string
        StringBuilder allowedRolesStr = new StringBuilder();
        for (int i = 0; i < allowedRoles.length; i++) {
            allowedRolesStr.append(allowedRoles[i].name());
            if (i < allowedRoles.length - 1) {
                allowedRolesStr.append(", ");
            }
        }
        
        logger.warn("Authorization failed: user {} (role: {}) attempted {} (requires one of: {})", 
            session.getUsername(), userRole, action, allowedRolesStr);
        throw new UnauthorizedException(session.getUsername(), allowedRolesStr.toString(), action);
    }
}
