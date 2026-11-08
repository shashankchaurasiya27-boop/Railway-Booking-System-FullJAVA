package com.railway.booking.backend.dao;

import com.railway.booking.backend.model.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RouteDAO {
    private static final Logger logger = LoggerFactory.getLogger(RouteDAO.class);
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Route> routeRowMapper = (rs, rowNum) -> {
        Route route = new Route();
        route.setId(rs.getInt("id"));
        route.setTrainId(rs.getInt("train_id"));
        route.setSourceStationId(rs.getInt("source_station_id"));
        route.setDestinationStationId(rs.getInt("destination_station_id"));
        route.setDepartureTime(rs.getTime("departure_time"));
        route.setArrivalTime(rs.getTime("arrival_time"));
        route.setDistanceKm(rs.getDouble("distance_km"));
        route.setFare(rs.getDouble("fare"));
        // Populate display fields (only available with JOIN queries)
        try { route.setAvailableSeats(rs.getInt("available_seats")); } catch (Exception ignored) {}
        try { route.setTrainNumber(rs.getString("train_number")); } catch (Exception ignored) {}
        try { route.setTrainName(rs.getString("train_name")); } catch (Exception ignored) {}
        try { route.setSourceStationName(rs.getString("source_name")); } catch (Exception ignored) {}
        try { route.setDestinationStationName(rs.getString("dest_name")); } catch (Exception ignored) {}
        return route;
    };

    // Common JOIN SQL that enriches routes with train & station display data
    private static final String ROUTE_JOIN_SQL =
        "SELECT r.*, " +
        "  t.train_number, t.train_name, " +
        "  s1.station_name AS source_name, " +
        "  s2.station_name AS dest_name " +
        "FROM routes r " +
        "LEFT JOIN trains t ON r.train_id = t.id " +
        "LEFT JOIN stations s1 ON r.source_station_id = s1.id " +
        "LEFT JOIN stations s2 ON r.destination_station_id = s2.id ";

    public RouteDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean addRoute(Route route) {
        String sql = "INSERT INTO routes (train_id, source_station_id, destination_station_id, " +
                "departure_time, arrival_time, distance_km, fare) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            int rows = jdbcTemplate.update(sql, route.getTrainId(), route.getSourceStationId(),
                    route.getDestinationStationId(), route.getDepartureTime(), route.getArrivalTime(),
                    route.getDistanceKm(), route.getFare());
            return rows > 0;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return false;
        }
    }

    public Route getRouteById(int routeId) {
        String sql = ROUTE_JOIN_SQL + "WHERE r.id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, routeRowMapper, routeId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return null;
        }
    }

    public List<Route> getAllRoutes() {
        try {
            return jdbcTemplate.query(ROUTE_JOIN_SQL, routeRowMapper);
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return List.of();
        }
    }

    public List<Route> getRoutesByTrainId(int trainId) {
        String sql = ROUTE_JOIN_SQL + "WHERE r.train_id = ?";
        try {
            return jdbcTemplate.query(sql, routeRowMapper, trainId);
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return List.of();
        }
    }

    public List<Route> searchRoutesByStations(int sourceStationId, int destinationStationId) {
        String sql = ROUTE_JOIN_SQL + "WHERE r.source_station_id = ? AND r.destination_station_id = ?";
        try {
            return jdbcTemplate.query(sql, routeRowMapper, sourceStationId, destinationStationId);
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return List.of();
        }
    }

    public boolean updateRoute(Route route) {
        String sql = "UPDATE routes SET train_id = ?, source_station_id = ?, destination_station_id = ?, " +
                "departure_time = ?, arrival_time = ?, distance_km = ?, fare = ? WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql, route.getTrainId(), route.getSourceStationId(),
                    route.getDestinationStationId(), route.getDepartureTime(), route.getArrivalTime(),
                    route.getDistanceKm(), route.getFare(), route.getId());
            return rows > 0;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return false;
        }
    }

    public boolean deleteRoute(int routeId) {
        String sql = "DELETE FROM routes WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql, routeId);
            return rows > 0;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return false;
        }
    }
}
