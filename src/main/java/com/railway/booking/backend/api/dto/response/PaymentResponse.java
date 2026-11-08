package com.railway.booking.backend.api.dto.response;

import com.railway.booking.backend.model.Payment;

import java.math.BigDecimal;

public class PaymentResponse {
    private String transactionId;
    private String bookingId;
    private BigDecimal amount;
    private String paymentStatus;
    private String paymentMethod;
    private String paymentDate;

    public PaymentResponse() {
    }

    public PaymentResponse(Payment payment) {
        this.transactionId = payment.getTransactionId();
        this.bookingId = payment.getBookingId();
        this.amount = payment.getAmount();
        this.paymentStatus = payment.getPaymentStatus().name();
        this.paymentMethod = payment.getPaymentMethod().name();
        this.paymentDate = payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : null;
    }

    // Getters and setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }
}
