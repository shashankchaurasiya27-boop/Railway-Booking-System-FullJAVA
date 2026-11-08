package com.railway.booking.backend.api.dto.response;

import com.railway.booking.backend.model.Booking;

import java.math.BigDecimal;

public class BookingResponse {
    private String bookingId;
    private int userId;
    private int trainId;
    private String trainName;
    private String journeyDate;
    private String bookingStatus;
    private BigDecimal totalFare;
    private int numberOfPassengers;
    private String bookingDate;

    public BookingResponse() {
    }

    public BookingResponse(Booking booking) {
        this.bookingId = booking.getBookingId();
        this.userId = booking.getUserId();
        this.trainId = booking.getTrainId();
        this.trainName = booking.getTrainName();
        this.journeyDate = booking.getJourneyDate() != null ? booking.getJourneyDate().toString() : null;
        this.bookingStatus = booking.getBookingStatus().name();
        this.totalFare = booking.getTotalFare();
        this.numberOfPassengers = booking.getNumberOfPassengers();
        this.bookingDate = booking.getBookingDate() != null ? booking.getBookingDate().toString() : null;
    }

    // Getters and setters
    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTrainId() {
        return trainId;
    }

    public void setTrainId(int trainId) {
        this.trainId = trainId;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(String journeyDate) {
        this.journeyDate = journeyDate;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public BigDecimal getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(BigDecimal totalFare) {
        this.totalFare = totalFare;
    }

    public int getNumberOfPassengers() {
        return numberOfPassengers;
    }

    public void setNumberOfPassengers(int numberOfPassengers) {
        this.numberOfPassengers = numberOfPassengers;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }
}
