package com.railway.booking.backend.model;

import java.sql.Time;

public class Route {
    private int id;
    private int trainId;
    private int sourceStationId;
    private int destinationStationId;
    private Time departureTime;
    private Time arrivalTime;
    private double distanceKm;
    private double fare;
    private int availableSeats;
    
    // Additional fields for display purposes
    private String sourceStationName;
    private String destinationStationName;
    private String trainName;
    private String trainNumber;

    // Constructors
    public Route() {}

    public Route(int trainId, int sourceStationId, int destinationStationId, 
                Time departureTime, Time arrivalTime, double distanceKm, double fare, int availableSeats) {
        this.trainId = trainId;
        this.sourceStationId = sourceStationId;
        this.destinationStationId = destinationStationId;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.distanceKm = distanceKm;
        this.fare = fare;
        this.availableSeats = availableSeats;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }
    
    // Alias for getId() for backward compatibility
    public int getRouteId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTrainId() {
        return trainId;
    }

    public void setTrainId(int trainId) {
        this.trainId = trainId;
    }

    public int getSourceStationId() {
        return sourceStationId;
    }

    public void setSourceStationId(int sourceStationId) {
        this.sourceStationId = sourceStationId;
    }

    public int getDestinationStationId() {
        return destinationStationId;
    }

    public void setDestinationStationId(int destinationStationId) {
        this.destinationStationId = destinationStationId;
    }

    public Time getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Time departureTime) {
        this.departureTime = departureTime;
    }

    public Time getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Time arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public String getSourceStationName() {
        return sourceStationName;
    }

    public void setSourceStationName(String sourceStationName) {
        this.sourceStationName = sourceStationName;
    }

    public String getDestinationStationName() {
        return destinationStationName;
    }

    public void setDestinationStationName(String destinationStationName) {
        this.destinationStationName = destinationStationName;
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

    @Override
    public String toString() {
        return String.format("Route %d: %s to %s (%s - %s) - ₹%.2f", 
                           id, sourceStationName, destinationStationName, 
                           departureTime, arrivalTime, fare);
    }
}

