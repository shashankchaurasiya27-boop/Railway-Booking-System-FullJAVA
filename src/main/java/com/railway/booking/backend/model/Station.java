package com.railway.booking.backend.model;

public class Station {
    private int id;
    private String stationName;
    private String stationCode;
    private String city;
    private String state;

    // Constructors
    public Station() {}

    public Station(String stationName, String stationCode, String city, String state) {
        this.stationName = stationName;
        this.stationCode = stationCode;
        this.city = city;
        this.state = state;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getStationCode() {
        return stationCode;
    }

    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s, %s", stationName, stationCode, city, state);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Station station = (Station) obj;
        return id == station.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}

