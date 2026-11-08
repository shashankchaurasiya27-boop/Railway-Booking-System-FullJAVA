package com.railway.booking.backend.api.dto.response;

import com.railway.booking.backend.model.Ticket;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class TrainSearchResponse {
    private List<TrainInfo> trains;

    public TrainSearchResponse() {
    }

    public TrainSearchResponse(List<Ticket> tickets) {
        this.trains = tickets.stream()
                .map(TrainInfo::new)
                .collect(Collectors.toList());
    }

    public List<TrainInfo> getTrains() {
        return trains;
    }

    public void setTrains(List<TrainInfo> trains) {
        this.trains = trains;
    }

    public static class TrainInfo {
        private int trainId;
        private String trainNumber;
        private String trainName;
        private String sourceStation;
        private String destinationStation;
        private String departureTime;
        private String arrivalTime;
        private BigDecimal fare;
        private int availableSeats;

        public TrainInfo() {
        }

        public TrainInfo(Ticket ticket) {
            this.trainId = ticket.getTrainId();
            this.trainNumber = ticket.getTrainNumber();
            this.trainName = ticket.getTrainName();
            this.sourceStation = ticket.getSourceStationName();
            this.destinationStation = ticket.getDestinationStationName();
            this.departureTime = ticket.getDepartureTime() != null ? ticket.getDepartureTime().toString() : null;
            this.arrivalTime = ticket.getArrivalTime() != null ? ticket.getArrivalTime().toString() : null;
            this.fare = ticket.getFare();
            this.availableSeats = ticket.getAvailableSeats();
        }

        // Getters and setters
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

        public String getSourceStation() {
            return sourceStation;
        }

        public void setSourceStation(String sourceStation) {
            this.sourceStation = sourceStation;
        }

        public String getDestinationStation() {
            return destinationStation;
        }

        public void setDestinationStation(String destinationStation) {
            this.destinationStation = destinationStation;
        }

        public String getDepartureTime() {
            return departureTime;
        }

        public void setDepartureTime(String departureTime) {
            this.departureTime = departureTime;
        }

        public String getArrivalTime() {
            return arrivalTime;
        }

        public void setArrivalTime(String arrivalTime) {
            this.arrivalTime = arrivalTime;
        }

        public BigDecimal getFare() {
            return fare;
        }

        public void setFare(BigDecimal fare) {
            this.fare = fare;
        }

        public int getAvailableSeats() {
            return availableSeats;
        }

        public void setAvailableSeats(int availableSeats) {
            this.availableSeats = availableSeats;
        }
    }
}
