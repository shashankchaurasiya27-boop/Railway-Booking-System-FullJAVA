package com.railway.booking.backend.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Payment {
    private int paymentId;
    private String bookingId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private Timestamp paymentDate;

    public enum PaymentMethod {
        CARD, UPI, CASH, NET_BANKING
    }

    public enum PaymentStatus {
        SUCCESS, FAILED, PENDING, REFUNDED
    }

    // Constructors
    public Payment() {
    }

    public Payment(String bookingId, BigDecimal amount, PaymentMethod paymentMethod, 
                   PaymentStatus paymentStatus, String transactionId) {
        this.bookingId = bookingId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.transactionId = transactionId;
    }

    // Getters and Setters
    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
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

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Timestamp getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Timestamp paymentDate) {
        this.paymentDate = paymentDate;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", bookingId='" + bookingId + '\'' +
                ", amount=" + amount +
                ", paymentMethod=" + paymentMethod +
                ", paymentStatus=" + paymentStatus +
                ", transactionId='" + transactionId + '\'' +
                ", paymentDate=" + paymentDate +
                '}';
    }
}
