package com.railway.booking.backend.exception;

/**
 * Exception thrown when a session is invalid or expired
 */
public class InvalidSessionException extends RuntimeException {
    private String sessionId;
    
    public InvalidSessionException(String message) {
        super(message);
    }
    
    public InvalidSessionException(String sessionId, String message) {
        super(message);
        this.sessionId = sessionId;
    }
    
    public InvalidSessionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public String getSessionId() {
        return sessionId;
    }
}
