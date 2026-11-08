package com.railway.booking.backend.api.dto.request;

import java.math.BigDecimal;

public class PaymentRequest {
    private String bookingId;
    private BigDecimal amount;
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING

    public PaymentRequest() {
    }

    public PaymentRequest(String bookingId, BigDecimal amount, String paymentMethod) {
        this.bookingId = bookingId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
