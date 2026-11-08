package com.railway.booking.backend.model;

import com.railway.booking.backend.model.TrainSeat;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Booking {
    private String bookingId;
    private String pnr;
    private int userId;
    private int trainId;
    private int routeId;
    private Date journeyDate;
    private String passengerName;
    private int passengerAge;
    private Gender passengerGender;
    private String seatNumber;
    private TrainSeat.CoachType coachType;
    private BookingStatus bookingStatus;
    private BigDecimal totalFare;
    private Timestamp bookingDate;
    private int numberOfPassengers;
    private int priority;
    private Timestamp updatedAt;
    
    // Additional fields for display purposes
    private String trainName;
    private String trainNumber;
    private String userName;
    private String routeDescription;

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public enum BookingStatus {
        PENDING, CONFIRMED, RAC, WAITING, CANCELLED
    }

    // Constructors
    public Booking() {
    }

    public Booking(String bookingId, int userId, int trainId, int routeId, 
                   Date journeyDate, String passengerName, int passengerAge, 
                   Gender passengerGender, String seatNumber, BookingStatus bookingStatus, 
                   BigDecimal totalFare) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.trainId = trainId;
        this.routeId = routeId;
        this.journeyDate = journeyDate;
        this.passengerName = passengerName;
        this.passengerAge = passengerAge;
        this.passengerGender = passengerGender;
        this.seatNumber = seatNumber;
        this.bookingStatus = bookingStatus;
        this.totalFare = totalFare;
    }

    // Getters and Setters
    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
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

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public Date getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(Date journeyDate) {
        this.journeyDate = journeyDate;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public int getPassengerAge() {
        return passengerAge;
    }

    public void setPassengerAge(int passengerAge) {
        this.passengerAge = passengerAge;
    }

    public Gender getPassengerGender() {
        return passengerGender;
    }

    public void setPassengerGender(Gender passengerGender) {
        this.passengerGender = passengerGender;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public TrainSeat.CoachType getCoachType() {
        return coachType;
    }

    public void setCoachType(TrainSeat.CoachType coachType) {
        this.coachType = coachType;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public BigDecimal getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(BigDecimal totalFare) {
        this.totalFare = totalFare;
    }

    public Timestamp getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Timestamp bookingDate) {
        this.bookingDate = bookingDate;
    }

    public int getNumberOfPassengers() {
        return numberOfPassengers;
    }

    public void setNumberOfPassengers(int numberOfPassengers) {
        this.numberOfPassengers = numberOfPassengers;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRouteDescription() {
        return routeDescription;
    }

    public void setRouteDescription(String routeDescription) {
        this.routeDescription = routeDescription;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId='" + bookingId + '\'' +
                ", passengerName='" + passengerName + '\'' +
                ", trainNumber='" + trainNumber + '\'' +
                ", journeyDate=" + journeyDate +
                ", seatNumber='" + seatNumber + '\'' +
                ", bookingStatus=" + bookingStatus +
                ", totalFare=" + totalFare +
                '}';
    }
}
