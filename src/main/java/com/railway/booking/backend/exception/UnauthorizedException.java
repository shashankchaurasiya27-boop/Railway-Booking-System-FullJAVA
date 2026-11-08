package com.railway.booking.backend.exception;

/**
 * Exception thrown when a user attempts to perform an action without proper authorization
 */
public class UnauthorizedException extends RuntimeException {
    private String username;
    private String requiredRole;
    private String attemptedAction;
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String username, String requiredRole, String attemptedAction) {
        super(String.format("User '%s' is not authorized to perform action '%s'. Required role: %s", 
            username, attemptedAction, requiredRole));
        this.username = username;
        this.requiredRole = requiredRole;
        this.attemptedAction = attemptedAction;
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getRequiredRole() {
        return requiredRole;
    }
    
    public String getAttemptedAction() {
        return attemptedAction;
    }
}
