package com.railway.booking.backend.api.exception;

/**
 * Custom exception for booking-related failures
 */
public class BookingException extends RuntimeException {
    public BookingException(String message) {
        super(message);
    }

    public BookingException(String message, Throwable cause) {
        super(message, cause);
    }
}
