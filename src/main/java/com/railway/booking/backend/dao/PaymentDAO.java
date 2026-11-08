package com.railway.booking.backend.dao;

import com.railway.booking.backend.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PaymentDAO {
    private static final Logger logger = LoggerFactory.getLogger(PaymentDAO.class);
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Payment> paymentRowMapper = (rs, rowNum) -> {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getInt("id"));
        payment.setBookingId(rs.getString("booking_id"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setPaymentMethod(Payment.PaymentMethod.valueOf(rs.getString("payment_method")));
        payment.setPaymentStatus(Payment.PaymentStatus.valueOf(rs.getString("payment_status")));
        payment.setTransactionId(rs.getString("transaction_id"));
        payment.setPaymentDate(rs.getTimestamp("payment_date"));
        return payment;
    };

    public PaymentDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean createPayment(Payment payment) {
        String sql = "INSERT INTO payments (booking_id, amount, payment_method, payment_status, transaction_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        try {
            int rows = jdbcTemplate.update(sql,
                    payment.getBookingId(),
                    payment.getAmount(),
                    payment.getPaymentMethod().name(),
                    payment.getPaymentStatus().name(),
                    payment.getTransactionId());
            return rows > 0;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return false;
        }
    }

    public Payment getPaymentById(String paymentId) {
        String sql = "SELECT * FROM payments WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, paymentRowMapper, paymentId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return null;
        }
    }

    public Payment getPaymentByBookingId(String bookingId) {
        String sql = "SELECT * FROM payments WHERE booking_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, paymentRowMapper, bookingId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return null;
        }
    }

    public List<Payment> getAllPayments() {
        String sql = "SELECT * FROM payments ORDER BY payment_date DESC";
        try {
            return jdbcTemplate.query(sql, paymentRowMapper);
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return List.of();
        }
    }

    public boolean updatePaymentStatus(String paymentId, Payment.PaymentStatus status) {
        String sql = "UPDATE payments SET payment_status = ? WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql, status.name(), paymentId);
            return rows > 0;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return false;
        }
    }

    public List<Payment> getPaymentsByStatus(Payment.PaymentStatus status) {
        String sql = "SELECT * FROM payments WHERE payment_status = ? ORDER BY payment_date DESC";
        try {
            return jdbcTemplate.query(sql, paymentRowMapper, status.name());
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return List.of();
        }
    }
}
