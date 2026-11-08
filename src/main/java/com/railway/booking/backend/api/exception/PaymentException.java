package com.railway.booking.backend.api.exception;

/**
 * Custom exception for payment-related failures
 */
public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
