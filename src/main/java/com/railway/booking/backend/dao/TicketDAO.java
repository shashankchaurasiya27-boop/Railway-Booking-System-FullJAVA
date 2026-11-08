package com.railway.booking.backend.dao;

import com.railway.booking.backend.model.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public class TicketDAO {
    private static final Logger logger = LoggerFactory.getLogger(TicketDAO.class);
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Ticket> ticketRowMapper = (rs, rowNum) -> {
        Ticket ticket = new Ticket();
        ticket.setRouteId(rs.getInt("route_id"));
        ticket.setTrainId(rs.getInt("train_id"));
        ticket.setTrainNumber(rs.getString("train_number"));
        ticket.setTrainName(rs.getString("train_name"));
        ticket.setTrainType(rs.getString("train_type"));
        ticket.setSourceStationId(rs.getInt("source_station_id"));
        ticket.setSourceStationName(rs.getString("source_name"));
        ticket.setSourceStationCode(rs.getString("source_code"));
        ticket.setDestinationStationId(rs.getInt("destination_station_id"));
        ticket.setDestinationStationName(rs.getString("destination_name"));
        ticket.setDestinationStationCode(rs.getString("destination_code"));
        ticket.setDepartureTime(rs.getTime("departure_time"));
        ticket.setArrivalTime(rs.getTime("arrival_time"));
        ticket.setDistanceKm(rs.getDouble("distance_km"));
        ticket.setFare(rs.getBigDecimal("fare"));
        return ticket;
    };

    public TicketDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Ticket> searchTrains(String sourceStation, String destinationStation, Date journeyDate) {
        String sql = "SELECT r.route_id, r.train_id, t.train_number, t.train_name, t.train_type, " +
                "t.total_seats, r.source_station_id, s1.station_name as source_name, " +
                "s1.station_code as source_code, r.destination_station_id, " +
                "s2.station_name as destination_name, s2.station_code as destination_code, " +
                "r.departure_time, r.arrival_time, r.distance_km, r.fare " +
                "FROM routes r " +
                "JOIN trains t ON r.train_id = t.train_id " +
                "JOIN stations s1 ON r.source_station_id = s1.station_id " +
                "JOIN stations s2 ON r.destination_station_id = s2.station_id " +
                "WHERE (s1.station_name = ? OR s1.station_code = ?) " +
                "AND (s2.station_name = ? OR s2.station_code = ?) " +
                "AND t.status = 'ACTIVE'";

        try {
            List<Ticket> tickets = jdbcTemplate.query(sql, ticketRowMapper,
                    sourceStation, sourceStation, destinationStation, destinationStation);

            // Set journey date and calculate available seats for each ticket
            tickets.forEach(ticket -> {
                ticket.setJourneyDate(journeyDate);
                // Note: total_seats should be retrieved from the query or set separately
            });

            return tickets;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return List.of();
        }
    }

    public List<String> getAllStations() {
        String sql = "SELECT station_name FROM stations ORDER BY station_name";
        try {
            return jdbcTemplate.queryForList(sql, String.class);
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return List.of();
        }
    }

    private int getBookedSeatsForRoute(int trainId, Date journeyDate) {
        String sql = "SELECT COALESCE(SUM(number_of_seats), 0) FROM bookings " +
                "WHERE train_id = ? AND journey_date = ? " +
                "AND status = 'CONFIRMED'";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, trainId, journeyDate);
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return 0;
        }
    }
}
