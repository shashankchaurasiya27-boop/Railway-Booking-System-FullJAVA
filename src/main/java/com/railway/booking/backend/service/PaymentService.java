package com.railway.booking.backend.service;

import com.railway.booking.backend.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

/**
 * Payment service using Spring JdbcTemplate for full @Transactional support.
 * All DB operations participate in the same transaction as the calling service.
 */
@Service
@Transactional
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private final JdbcTemplate jdbcTemplate;

    public PaymentService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Process payment and persist it within the current transaction.
     */
    public String processPayment(String bookingId, BigDecimal amount, Payment.PaymentMethod method) {
        // Simulate payment gateway (blocking, but runs within Spring's managed thread)
        boolean paymentSuccess = simulatePaymentGateway(amount, method);

        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setAmount(amount);
        payment.setPaymentMethod(method);
        payment.setPaymentStatus(paymentSuccess ? Payment.PaymentStatus.SUCCESS : Payment.PaymentStatus.FAILED);
        payment.setTransactionId(generateTransactionId());

        boolean saved = savePayment(payment);

        if (saved && paymentSuccess) {
            logger.info("Payment processed successfully: {}", payment.getTransactionId());
            return "SUCCESS:" + payment.getTransactionId();
        } else {
            return "Payment processing failed. Please try again.";
        }
    }

    /**
     * Save payment to database using JdbcTemplate (participates in @Transactional).
     */
    private boolean savePayment(Payment payment) {
        String sql = "INSERT INTO payments (booking_id, amount, payment_method, payment_status, transaction_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        try {
            int rowsAffected = jdbcTemplate.update(sql,
                    payment.getBookingId(),
                    payment.getAmount(),
                    payment.getPaymentMethod().name(),
                    payment.getPaymentStatus().name(),
                    payment.getTransactionId());
            return rowsAffected > 0;
        } catch (Exception e) {
            logger.error("Failed to save payment for booking: {}", payment.getBookingId(), e);
            return false;
        }
    }

    /**
     * Simulate payment gateway (90% success rate).
     */
    private boolean simulatePaymentGateway(BigDecimal amount, Payment.PaymentMethod method) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Payment simulation interrupted");
        }
        return new Random().nextInt(10) < 9;
    }

    /**
     * Generate a collision-safe transaction ID using UUID.
     */
    private String generateTransactionId() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    /**
     * Get payment by booking ID using JdbcTemplate.
     */
    @Transactional(readOnly = true)
    public Payment getPaymentByBookingId(String bookingId) {
        String sql = "SELECT * FROM payments WHERE booking_id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Payment payment = new Payment();
                payment.setPaymentId(rs.getInt("payment_id"));
                payment.setBookingId(rs.getString("booking_id"));
                payment.setAmount(rs.getBigDecimal("amount"));
                payment.setPaymentMethod(Payment.PaymentMethod.valueOf(rs.getString("payment_method")));
                payment.setPaymentStatus(Payment.PaymentStatus.valueOf(rs.getString("payment_status")));
                payment.setTransactionId(rs.getString("transaction_id"));
                payment.setPaymentDate(rs.getTimestamp("payment_date"));
                return payment;
            }, bookingId);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            logger.debug("No payment found for booking: {}", bookingId);
            return null;
        } catch (Exception e) {
            logger.error("Failed to get payment for booking: {}", bookingId, e);
            return null;
        }
    }
}
