package com.railway.controller;

import com.railway.booking.backend.dao.RouteDAO;
import com.railway.booking.backend.dao.StationDAO;
import com.railway.booking.backend.dao.TrainDAO;
import com.railway.booking.backend.model.Route;
import com.railway.booking.backend.model.Session;
import com.railway.booking.backend.model.Station;
import com.railway.booking.backend.model.Train;
import com.railway.booking.backend.util.AuthorizationManager;
import com.railway.util.ApplicationContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TrainController {
    private static final Logger logger = LoggerFactory.getLogger(TrainController.class);
    private TrainDAO trainDAO;
    private RouteDAO routeDAO;
    private StationDAO stationDAO;

    public TrainController() {
        this.trainDAO = ApplicationContextProvider.getBean(TrainDAO.class);
        this.routeDAO = ApplicationContextProvider.getBean(RouteDAO.class);
        this.stationDAO = ApplicationContextProvider.getBean(StationDAO.class);
    }

    /**
     * Get all trains
     */
    public List<Train> getAllTrains() {
        return trainDAO.getAllTrains();
    }

    /**
     * Get train by ID
     */
    public Train getTrainById(int trainId) {
        return trainDAO.getTrainById(trainId);
    }

    /**
     * Get train by train number
     */
    public Train getTrainByNumber(String trainNumber) {
        return trainDAO.getTrainByNumber(trainNumber);
    }

    /**
     * Search trains by number or name
     */
    public List<Route> searchRoutesByTrain(String trainNumber, String trainName) {
        List<Route> routes = routeDAO.getAllRoutes();

        if (!trainNumber.isEmpty() || !trainName.isEmpty()) {
            routes = routes.stream()
                    .filter(route -> {
                        String rNum = route.getTrainNumber() != null ? route.getTrainNumber() : "";
                        String rName = route.getTrainName() != null ? route.getTrainName() : "";
                        boolean matchesNumber = trainNumber.isEmpty() ||
                                rNum.toLowerCase().contains(trainNumber.toLowerCase());
                        boolean matchesName = trainName.isEmpty() ||
                                rName.toLowerCase().contains(trainName.toLowerCase());
                        return matchesNumber || matchesName;
                    })
                    .collect(java.util.stream.Collectors.toList());
        }

        return routes;
    }

    /**
     * Search routes by source and destination stations
     */
    public List<Route> searchRoutesByStations(int sourceStationId, int destinationStationId) {
        return routeDAO.searchRoutesByStations(sourceStationId, destinationStationId);
    }

    /**
     * Get route by ID
     */
    public Route getRouteById(int routeId) {
        return routeDAO.getRouteById(routeId);
    }

    /**
     * Get all stations
     */
    public List<Station> getAllStations() {
        return stationDAO.getAllStations();
    }

    /**
     * Get station by ID
     */
    public Station getStationById(int stationId) {
        return stationDAO.getStationById(stationId);
    }

    /**
     * Search stations by name
     */
    public List<Station> searchStationsByName(String name) {
        return stationDAO.searchStationsByName(name);
    }

    /**
     * Add new train (ADMIN only)
     */
    public boolean addTrain(Session session, Train train) {
        AuthorizationManager.requireAdmin(session, "add train");
        logger.info("Admin {} adding train: {}", session.getUsername(), train.getTrainNumber());
        return trainDAO.createTrain(train);
    }

    /**
     * Update train (ADMIN only)
     */
    public boolean updateTrain(Session session, Train train) {
        AuthorizationManager.requireAdmin(session, "update train");
        logger.info("Admin {} updating train: {}", session.getUsername(), train.getTrainNumber());
        return trainDAO.updateTrain(train);
    }

    /**
     * Delete train (ADMIN only)
     */
    public boolean deleteTrain(Session session, int trainId) {
        AuthorizationManager.requireAdmin(session, "delete train");
        logger.info("Admin {} deleting train ID: {}", session.getUsername(), trainId);
        return trainDAO.deleteTrain(trainId);
    }

    /**
     * Add new route (ADMIN only)
     */
    public boolean addRoute(Session session, Route route) {
        AuthorizationManager.requireAdmin(session, "add route");
        logger.info("Admin {} adding route", session.getUsername());
        return routeDAO.addRoute(route);
    }

    /**
     * Update route (ADMIN only)
     */
    public boolean updateRoute(Session session, Route route) {
        AuthorizationManager.requireAdmin(session, "update route");
        logger.info("Admin {} updating route ID: {}", session.getUsername(), route.getRouteId());
        return routeDAO.updateRoute(route);
    }

    /**
     * Delete route (ADMIN only)
     */
    public boolean deleteRoute(Session session, int routeId) {
        AuthorizationManager.requireAdmin(session, "delete route");
        logger.info("Admin {} deleting route ID: {}", session.getUsername(), routeId);
        return routeDAO.deleteRoute(routeId);
    }

    /**
     * Add new station (ADMIN only)
     */
    public boolean addStation(Session session, Station station) {
        AuthorizationManager.requireAdmin(session, "add station");
        logger.info("Admin {} adding station: {}", session.getUsername(), station.getStationName());
        return stationDAO.addStation(station);
    }

    /**
     * Update station (ADMIN only)
     */
    public boolean updateStation(Session session, Station station) {
        AuthorizationManager.requireAdmin(session, "update station");
        logger.info("Admin {} updating station: {}", session.getUsername(), station.getStationName());
        return stationDAO.updateStation(station);
    }

    /**
     * Delete station (ADMIN only)
     */
    public boolean deleteStation(Session session, int stationId) {
        AuthorizationManager.requireAdmin(session, "delete station");
        logger.info("Admin {} deleting station ID: {}", session.getUsername(), stationId);
        return stationDAO.deleteStation(stationId);
    }
}
