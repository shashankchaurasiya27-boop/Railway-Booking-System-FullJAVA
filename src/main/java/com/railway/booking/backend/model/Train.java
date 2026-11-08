package com.railway.booking.backend.model;

public class Train {
    private int trainId;
    private String trainNumber;
    private String trainName;
    private int totalSeats;
    private TrainType trainType;
    private TrainStatus status;

    public enum TrainType {
        EXPRESS, PASSENGER, SUPERFAST
    }

    public enum TrainStatus {
        ACTIVE, INACTIVE
    }

    // Constructors
    public Train() {
    }

    public Train(String trainNumber, String trainName, int totalSeats, TrainType trainType, TrainStatus status) {
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.totalSeats = totalSeats;
        this.trainType = trainType;
        this.status = status;
    }

    // Getters and Setters
    public int getTrainId() {
        return trainId;
    }

    public void setTrainId(int trainId) {
        this.trainId = trainId;
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

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public TrainType getTrainType() {
        return trainType;
    }

    public void setTrainType(TrainType trainType) {
        this.trainType = trainType;
    }

    public TrainStatus getStatus() {
        return status;
    }

    public void setStatus(TrainStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Train{" +
                "trainId=" + trainId +
                ", trainNumber='" + trainNumber + '\'' +
                ", trainName='" + trainName + '\'' +
                ", totalSeats=" + totalSeats +
                ", trainType=" + trainType +
                ", status=" + status +
                '}';
    }
}
