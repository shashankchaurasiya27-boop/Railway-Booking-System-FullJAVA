package com.railway.booking.backend.util;

public class Constants {
    
    // Application Constants
    public static final String APP_NAME = "Railway Booking System";
    public static final String APP_VERSION = "1.0.0";
    
    // Database Constants
    public static final String DB_NAME = "railway_db";
    
    // User Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    
    // Booking Status
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_WAITING = "WAITING";
    
    // Payment Status
    public static final String PAYMENT_SUCCESS = "SUCCESS";
    public static final String PAYMENT_FAILED = "FAILED";
    public static final String PAYMENT_PENDING = "PENDING";
    
    // Train Status
    public static final String TRAIN_ACTIVE = "ACTIVE";
    public static final String TRAIN_INACTIVE = "INACTIVE";
    
    // Error Messages
    public static final String ERROR_DATABASE = "Database connection error. Please try again.";
    public static final String ERROR_INVALID_LOGIN = "Invalid username or password.";
    public static final String ERROR_USER_EXISTS = "Username or email already exists.";
    public static final String ERROR_INVALID_INPUT = "Please fill all required fields correctly.";
    public static final String ERROR_NO_SEATS = "No seats available for this train.";
    public static final String ERROR_BOOKING_FAILED = "Booking failed. Please try again.";
    public static final String ERROR_PAYMENT_FAILED = "Payment processing failed.";
    
    // Success Messages
    public static final String SUCCESS_REGISTRATION = "Registration successful! Please login.";
    public static final String SUCCESS_LOGIN = "Login successful!";
    public static final String SUCCESS_BOOKING = "Booking confirmed successfully!";
    public static final String SUCCESS_CANCELLATION = "Booking cancelled successfully!";
    public static final String SUCCESS_PAYMENT = "Payment processed successfully!";
    public static final String SUCCESS_TRAIN_ADDED = "Train added successfully!";
    public static final String SUCCESS_TRAIN_UPDATED = "Train updated successfully!";
    
    // UI Constants
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 800;
    public static final int DIALOG_WIDTH = 400;
    public static final int DIALOG_HEIGHT = 300;
    
    // Date Format
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    // Booking ID Prefix
    public static final String BOOKING_PREFIX = "RB";
    
    // Transaction ID Prefix
    public static final String TRANSACTION_PREFIX = "TXN";
    
    // Seat Number Format
    public static final String SEAT_PREFIX = "S";
    
    // Fare Calculation
    public static final double BASE_FARE_PER_KM = 0.50;
    public static final double EXPRESS_MULTIPLIER = 1.2;
    public static final double SUPERFAST_MULTIPLIER = 1.5;
    
    // Validation Constants
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 20;
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 20;
    public static final int MIN_AGE = 1;
    public static final int MAX_AGE = 120;
    
    private Constants() {
        // Private constructor to prevent instantiation
    }
}
