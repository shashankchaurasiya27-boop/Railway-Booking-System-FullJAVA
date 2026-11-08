package com.railway.booking.backend.model;

import java.sql.Timestamp;

/**
 * BookingLog model - Audit trail for booking status changes
 * Tracks complete history of booking lifecycle
 */
public class BookingLog {
    private int id;
    private String bookingId;
    private String oldStatus;
    private String newStatus;
    private String message;
    private Timestamp timestamp;
    
    // Additional fields for display
    private String userName;
    private String trainName;
    
    // Constructors
    public BookingLog() {
    }
    
    public BookingLog(String bookingId, String oldStatus, String newStatus, String message) {
        this.bookingId = bookingId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.message = message;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }
    
    public String getOldStatus() {
        return oldStatus;
    }
    
    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }
    
    public String getNewStatus() {
        return newStatus;
    }
    
    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Timestamp getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getTrainName() {
        return trainName;
    }
    
    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }
    
    /**
     * Format log entry for display
     * Format: [HH:mm:ss] OLD → NEW: Message
     */
    public String getFormattedLog() {
        String timeStr = timestamp != null ? 
            timestamp.toLocalDateTime().toLocalTime().toString().substring(0, 8) : 
            "Unknown";
        return String.format("[%s] %s → %s: %s", timeStr, oldStatus, newStatus, message);
    }
    
    @Override
    public String toString() {
        return "BookingLog{" +
                "id=" + id +
                ", bookingId='" + bookingId + '\'' +
                ", oldStatus='" + oldStatus + '\'' +
                ", newStatus='" + newStatus + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
