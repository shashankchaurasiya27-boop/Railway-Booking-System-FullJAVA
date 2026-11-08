package com.railway.booking.backend.api.controller;

import com.railway.booking.backend.api.dto.request.PaymentRequest;
import com.railway.booking.backend.api.dto.response.ErrorResponse;
import com.railway.booking.backend.api.dto.response.PaymentResponse;
import com.railway.booking.backend.model.Payment;
import com.railway.booking.backend.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for payment operations
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentRestController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentRestController.class);
    private final PaymentService paymentService;

    public PaymentRestController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Process payment endpoint
     * POST /api/payments
     */
    @PostMapping
    public ResponseEntity<?> processPayment(@RequestBody PaymentRequest request) {
        logger.info("Processing payment for booking: {}", request.getBookingId());

        try {
            // Convert payment method string to enum
            Payment.PaymentMethod paymentMethod;
            try {
                paymentMethod = Payment.PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Invalid payment method",
                                "Payment method must be one of: CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING"));
            }

            // Process payment - returns String result
            String result = paymentService.processPayment(
                    request.getBookingId(),
                    request.getAmount(),
                    paymentMethod);

            if (result == null || !result.startsWith("SUCCESS:")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Payment failed",
                                result != null ? result : "Payment processing failed"));
            }

            // Extract transaction ID from result
            String transactionId = result.substring("SUCCESS:".length());

            // Fetch the payment record
            Payment payment = paymentService.getPaymentByBookingId(request.getBookingId());

            if (payment == null) {
                return ResponseEntity.ok(new SimplePaymentResponse(transactionId, "Payment processed successfully"));
            }

            logger.info("Payment processed successfully: {}", transactionId);
            return ResponseEntity.ok(new PaymentResponse(payment));

        } catch (Exception e) {
            logger.error("Payment processing error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Server error",
                            "An error occurred during payment processing: " + e.getMessage()));
        }
    }

    /**
     * Get payment by booking ID endpoint
     * GET /api/payments/booking/{bookingId}
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getPaymentByBookingId(@PathVariable String bookingId) {
        logger.info("Fetching payment for booking: {}", bookingId);

        try {
            Payment payment = paymentService.getPaymentByBookingId(bookingId);

            if (payment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Payment not found", "No payment found for this booking"));
            }

            return ResponseEntity.ok(new PaymentResponse(payment));

        } catch (Exception e) {
            logger.error("Error fetching payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Server error", "An error occurred while fetching payment"));
        }
    }

    // Simple payment response
    private static class SimplePaymentResponse {
        private String transactionId;
        private String message;

        public SimplePaymentResponse(String transactionId, String message) {
            this.transactionId = transactionId;
            this.message = message;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public String getMessage() {
            return message;
        }
    }
}
