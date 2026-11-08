package com.railway.booking.backend.util;

import com.railway.booking.backend.model.Session;
import com.railway.booking.backend.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Thread-safe session manager for handling user sessions
 */
public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    
    // Thread-safe map to store active sessions
    private static final Map<String, Session> activeSessions = new ConcurrentHashMap<>();
    
    // Scheduled executor for cleanup task
    private static final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    
    static {
        // Schedule cleanup task to run every 5 minutes
        cleanupExecutor.scheduleAtFixedRate(() -> {
            cleanupExpiredSessions();
        }, 5, 5, TimeUnit.MINUTES);
        
        logger.info("SessionManager initialized with automatic cleanup every 5 minutes");
    }
    
    /**
     * Create a new session for a user
     * 
     * @param user The user to create session for
     * @return The created session
     */
    public static Session createSession(User user) {
        if (user == null) {
            logger.warn("Attempted to create session for null user");
            throw new IllegalArgumentException("User cannot be null");
        }
        
        Session session = new Session(user);
        activeSessions.put(session.getSessionId(), session);
        
        logger.info("Session created for user: {} (sessionId: {})", user.getUsername(), session.getSessionId());
        logger.debug("Active sessions count: {}", activeSessions.size());
        
        return session;
    }
    
    /**
     * Validate a session by session ID
     * 
     * @param sessionId The session ID to validate
     * @return The session if valid, null otherwise
     */
    public static Session validateSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            logger.debug("Session validation failed: null or empty session ID");
            return null;
        }
        
        Session session = activeSessions.get(sessionId);
        
        if (session == null) {
            logger.warn("Session validation failed: session not found (sessionId: {})", sessionId);
            return null;
        }
        
        if (!session.isValid()) {
            logger.warn("Session validation failed: session expired or inactive (sessionId: {}, user: {})", 
                sessionId, session.getUsername());
            invalidateSession(sessionId);
            return null;
        }
        
        // Refresh session on successful validation
        session.refresh();
        logger.debug("Session validated and refreshed for user: {} (sessionId: {})", 
            session.getUsername(), sessionId);
        
        return session;
    }
    
    /**
     * Invalidate a session
     * 
     * @param sessionId The session ID to invalidate
     */
    public static void invalidateSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return;
        }
        
        Session session = activeSessions.remove(sessionId);
        
        if (session != null) {
            session.invalidate();
            logger.info("Session invalidated for user: {} (sessionId: {})", 
                session.getUsername(), sessionId);
            logger.debug("Active sessions count: {}", activeSessions.size());
        }
    }
    
    /**
     * Logout - invalidate session
     * 
     * @param sessionId The session ID to logout
     */
    public static void logout(String sessionId) {
        Session session = activeSessions.get(sessionId);
        if (session != null) {
            logger.info("User logout: {} (sessionId: {})", session.getUsername(), sessionId);
        }
        invalidateSession(sessionId);
    }
    
    /**
     * Get session by ID without validation
     * 
     * @param sessionId The session ID
     * @return The session or null
     */
    public static Session getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }
    
    /**
     * Check if a session exists and is valid
     * 
     * @param sessionId The session ID to check
     * @return true if session exists and is valid
     */
    public static boolean isSessionValid(String sessionId) {
        return validateSession(sessionId) != null;
    }
    
    /**
     * Get count of active sessions
     * 
     * @return Number of active sessions
     */
    public static int getActiveSessionCount() {
        return activeSessions.size();
    }
    
    /**
     * Cleanup expired sessions
     */
    private static void cleanupExpiredSessions() {
        int initialCount = activeSessions.size();
        int removedCount = 0;
        
        // Remove expired sessions
        activeSessions.entrySet().removeIf(entry -> {
            Session session = entry.getValue();
            if (session.isExpired() || !session.isActive()) {
                logger.debug("Removing expired session for user: {} (sessionId: {})", 
                    session.getUsername(), session.getSessionId());
                return true;
            }
            return false;
        });
        
        removedCount = initialCount - activeSessions.size();
        
        if (removedCount > 0) {
            logger.info("Session cleanup: removed {} expired sessions, {} active sessions remaining", 
                removedCount, activeSessions.size());
        } else {
            logger.debug("Session cleanup: no expired sessions found, {} active sessions", 
                activeSessions.size());
        }
    }
    
    /**
     * Force cleanup of all sessions (for testing or shutdown)
     */
    public static void clearAllSessions() {
        int count = activeSessions.size();
        activeSessions.clear();
        logger.warn("All sessions cleared: {} sessions removed", count);
    }
    
    /**
     * Shutdown the session manager and cleanup executor
     */
    public static void shutdown() {
        logger.info("Shutting down SessionManager");
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        clearAllSessions();
        logger.info("SessionManager shutdown complete");
    }
    
    /**
     * Get session statistics
     * 
     * @return String with session statistics
     */
    public static String getSessionStats() {
        return String.format("Active Sessions: %d", activeSessions.size());
    }
}
