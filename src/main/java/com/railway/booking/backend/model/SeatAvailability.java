package com.railway.booking.backend.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * SeatAvailability model - tracks real-time seat availability
 * per train, per coach type, per journey date
 */
public class SeatAvailability {
    private int availabilityId;
    private int trainId;
    private TrainSeat.CoachType coachType;
    private Date journeyDate;
    private int totalSeats;
    private int availableSeats;
    private int bookedSeats;
    private Timestamp lastUpdated;

    // Additional fields for display
    private String trainNumber;
    private String trainName;

    // Constructors
    public SeatAvailability() {
    }

    public SeatAvailability(int trainId, TrainSeat.CoachType coachType, Date journeyDate, 
                           int totalSeats, int availableSeats) {
        this.trainId = trainId;
        this.coachType = coachType;
        this.journeyDate = journeyDate;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.bookedSeats = totalSeats - availableSeats;
    }

    // Getters and Setters
    public int getAvailabilityId() {
        return availabilityId;
    }

    public void setAvailabilityId(int availabilityId) {
        this.availabilityId = availabilityId;
    }

    public int getTrainId() {
        return trainId;
    }

    public void setTrainId(int trainId) {
        this.trainId = trainId;
    }

    public TrainSeat.CoachType getCoachType() {
        return coachType;
    }

    public void setCoachType(TrainSeat.CoachType coachType) {
        this.coachType = coachType;
    }

    public Date getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(Date journeyDate) {
        this.journeyDate = journeyDate;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
        this.bookedSeats = this.totalSeats - availableSeats;
    }

    public int getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(int bookedSeats) {
        this.bookedSeats = bookedSeats;
        this.availableSeats = this.totalSeats - bookedSeats;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    /**
     * Decrease available seats when booking
     * @param seats number of seats to book
     * @return true if seats were available and decreased
     */
    public boolean bookSeats(int seats) {
        if (availableSeats >= seats) {
            availableSeats -= seats;
            bookedSeats += seats;
            return true;
        }
        return false;
    }

    /**
     * Increase available seats when cancelling
     * @param seats number of seats to release
     * @return true if seats were released
     */
    public boolean releaseSeats(int seats) {
        if (bookedSeats >= seats) {
            availableSeats += seats;
            bookedSeats -= seats;
            return true;
        }
        return false;
    }

    /**
     * Check if seats are available
     * @param requestedSeats number of seats requested
     * @return true if enough seats available
     */
    public boolean isAvailable(int requestedSeats) {
        return availableSeats >= requestedSeats;
    }

    @Override
    public String toString() {
        return "SeatAvailability{" +
                "trainId=" + trainId +
                ", coachType=" + coachType +
                ", journeyDate=" + journeyDate +
                ", available=" + availableSeats +
                "/" + totalSeats +
                '}';
    }
}
