package com.railway.booking.backend.model;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Session model for managing user sessions
 */
public class Session {
    private String sessionId;
    private int userId;
    private String username;
    private User.UserType userType;
    private Timestamp createdAt;
    private Timestamp lastAccessedAt;
    private Timestamp expiresAt;
    private boolean active;
    
    // Session timeout in milliseconds (30 minutes)
    public static final long SESSION_TIMEOUT_MS = 30 * 60 * 1000;
    
    /**
     * Create a new session for a user
     */
    public Session(User user) {
        this.sessionId = UUID.randomUUID().toString();
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.userType = user.getUserType();
        
        long currentTime = System.currentTimeMillis();
        this.createdAt = new Timestamp(currentTime);
        this.lastAccessedAt = new Timestamp(currentTime);
        this.expiresAt = new Timestamp(currentTime + SESSION_TIMEOUT_MS);
        this.active = true;
    }
    
    /**
     * Refresh session - update last accessed time and expiration
     */
    public void refresh() {
        long currentTime = System.currentTimeMillis();
        this.lastAccessedAt = new Timestamp(currentTime);
        this.expiresAt = new Timestamp(currentTime + SESSION_TIMEOUT_MS);
    }
    
    /**
     * Check if session is expired
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt.getTime();
    }
    
    /**
     * Check if session is valid (active and not expired)
     */
    public boolean isValid() {
        return active && !isExpired();
    }
    
    /**
     * Invalidate the session
     */
    public void invalidate() {
        this.active = false;
    }
    
    // Getters and Setters
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public User.UserType getUserType() {
        return userType;
    }
    
    public void setUserType(User.UserType userType) {
        this.userType = userType;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getLastAccessedAt() {
        return lastAccessedAt;
    }
    
    public void setLastAccessedAt(Timestamp lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }
    
    public Timestamp getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    @Override
    public String toString() {
        return "Session{" +
                "sessionId='" + sessionId + '\'' +
                ", username='" + username + '\'' +
                ", userType=" + userType +
                ", expiresAt=" + expiresAt +
                ", active=" + active +
                '}';
    }
}
