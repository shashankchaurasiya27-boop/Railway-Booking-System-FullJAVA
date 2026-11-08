package com.railway.booking.backend.api.dto.request;

public class TrainSearchRequest {
    private String sourceStation;
    private String destinationStation;
    private String journeyDate; // Format: yyyy-MM-dd

    public TrainSearchRequest() {
    }

    public TrainSearchRequest(String sourceStation, String destinationStation, String journeyDate) {
        this.sourceStation = sourceStation;
        this.destinationStation = destinationStation;
        this.journeyDate = journeyDate;
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

    public String getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(String journeyDate) {
        this.journeyDate = journeyDate;
    }
}
