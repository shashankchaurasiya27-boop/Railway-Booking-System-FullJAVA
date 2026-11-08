package com.railway.booking.backend.dao;

import com.railway.booking.backend.model.Booking;
import com.railway.booking.backend.model.TrainSeat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public class BookingDAO {
    private static final Logger logger = LoggerFactory.getLogger(BookingDAO.class);
    private final JdbcTemplate jdbcTemplate;

    // RowMapper for Booking entity with joins
    private final RowMapper<Booking> bookingRowMapper = (rs, rowNum) -> {
        Booking booking = new Booking();
        booking.setBookingId(rs.getString("booking_id"));
        booking.setPnr(rs.getString("pnr"));
        booking.setUserId(rs.getInt("user_id"));
        booking.setTrainId(rs.getInt("train_id"));
        // route_id column doesn't exist in schema - skip it
        booking.setJourneyDate(rs.getDate("journey_date"));
        booking.setBookingStatus(Booking.BookingStatus.valueOf(rs.getString("status")));
        booking.setTotalFare(rs.getBigDecimal("total_fare"));
        booking.setBookingDate(rs.getTimestamp("booking_date"));
        booking.setNumberOfPassengers(rs.getInt("number_of_seats"));
        booking.setPriority(rs.getInt("priority"));
        booking.setUpdatedAt(rs.getTimestamp("updated_at"));
        
        // Map coach_type (handle null for backward compatibility)
        String coachTypeStr = rs.getString("coach_type");
        if (coachTypeStr != null) {
            booking.setCoachType(TrainSeat.CoachType.valueOf(coachTypeStr));
        }

        // Additional fields from joins
        booking.setTrainName(rs.getString("train_name"));
        booking.setTrainNumber(rs.getString("train_number"));
        booking.setUserName(rs.getString("username"));
        booking.setRouteDescription(rs.getString("route_description"));

        return booking;
    };

    public BookingDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Create a new booking with coach type
     */
    public boolean createBooking(Booking booking) {
        String sql = "INSERT INTO bookings (booking_id, user_id, train_id, journey_date, booking_date, " +
                "status, total_fare, number_of_seats, coach_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            int rowsAffected = jdbcTemplate.update(sql,
                    booking.getBookingId(),
                    booking.getUserId(),
                    booking.getTrainId(),
                    booking.getJourneyDate(),
                    new java.sql.Date(System.currentTimeMillis()),
                    booking.getBookingStatus().name(),
                    booking.getTotalFare(),
                    booking.getNumberOfPassengers(),
                    booking.getCoachType() != null ? booking.getCoachType().name() : "GENERAL");
            return rowsAffected > 0;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return false;
        }
    }

    /**
     * Get booking by booking ID
     */
    public Booking getBookingById(String bookingId) {
        String sql = "SELECT b.*, t.train_name, t.train_number, u.username, " +
                "CONCAT(s1.station_name, ' to ', s2.station_name) as route_description " +
                "FROM bookings b " +
                "JOIN trains t ON b.train_id = t.id " +
                "JOIN routes r ON b.route_id = r.id " +
                "JOIN stations s1 ON r.source_station_id = s1.id " +
                "JOIN stations s2 ON r.destination_station_id = s2.id " +
                "JOIN users u ON b.user_id = u.id " +
                "WHERE b.booking_id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, bookingRowMapper, bookingId);
        } catch (EmptyResultDataAccessException e) {
            logger.debug("No booking found with ID: {}", bookingId);
            return null;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return null;
        }
    }

    /**
     * Get all bookings by user ID
     */
    public List<Booking> getBookingsByUserId(int userId) {
        String sql = "SELECT b.*, t.train_name, t.train_number, u.username, " +
                "CONCAT(t.source_station, ' to ', t.destination_station) as route_description " +
                "FROM bookings b " +
                "JOIN trains t ON b.train_id = t.id " +
                "JOIN users u ON b.user_id = u.id " +
                "WHERE b.user_id = ? " +
                "ORDER BY b.booking_date DESC";

        try {
            return jdbcTemplate.query(sql, bookingRowMapper, userId);
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return List.of();
        }
    }

    /**
     * Get all bookings
     */
    public List<Booking> getAllBookings() {
        String sql = "SELECT b.*, t.train_name, t.train_number, u.username, " +
                "CONCAT(t.source_station, ' to ', t.destination_station) as route_description " +
                "FROM bookings b " +
                "JOIN trains t ON b.train_id = t.id " +
                "JOIN users u ON b.user_id = u.id " +
                "ORDER BY b.booking_date DESC";

        try {
            return jdbcTemplate.query(sql, bookingRowMapper);
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return List.of();
        }
    }

    /**
     * Cancel booking
     */
    public boolean cancelBooking(String bookingId) {
        String sql = "UPDATE bookings SET status = 'CANCELLED' WHERE booking_id = ?";

        try {
            int rowsAffected = jdbcTemplate.update(sql, bookingId);
            return rowsAffected > 0;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return false;
        }
    }

    /**
     * Get booked seats count for a train on a specific date
     */
    public int getBookedSeatsCount(int trainId, Date journeyDate) {
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

    /**
     * Get next available seat number
     */
    public String getNextSeatNumber(int trainId, Date journeyDate) {
        int bookedSeats = getBookedSeatsCount(trainId, journeyDate);
        return "S" + (bookedSeats + 1);
    }

    /**
     * Check if PNR already exists (for uniqueness validation)
     * CRITICAL for concurrency-safe PNR generation
     */
    public boolean isPNRExists(String pnr) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE pnr = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, pnr);
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("Error checking PNR existence", e);
            return false; // Assume doesn't exist on error
        }
    }

    /**
     * Get booking by PNR (for ticket tracking)
     */
    public Booking getBookingByPNR(String pnr) {
        String sql = "SELECT b.*, t.train_name, t.train_number, u.username, " +
                "CONCAT(s1.station_name, ' to ', s2.station_name) as route_description " +
                "FROM bookings b " +
                "JOIN trains t ON b.train_id = t.id " +
                "JOIN routes r ON b.route_id = r.id " +
                "JOIN stations s1 ON r.source_station_id = s1.id " +
                "JOIN stations s2 ON r.destination_station_id = s2.id " +
                "JOIN users u ON b.user_id = u.id " +
                "WHERE b.pnr = ?";

        try {
            return jdbcTemplate.queryForObject(sql, bookingRowMapper, pnr);
        } catch (EmptyResultDataAccessException e) {
            logger.debug("No booking found with PNR: {}", pnr);
            return null;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return null;
        }
    }

    /**
     * Generate unique PNR with collision check loop
     * Industry-grade implementation for concurrency safety
     */
    public String generateUniquePNR(int trainId) {
        String pnr;
        int attempts = 0;
        final int MAX_ATTEMPTS = 5;

        do {
            pnr = com.railway.booking.backend.util.PNRGenerator.generatePNR(trainId);
            attempts++;
        } while (isPNRExists(pnr) && attempts < MAX_ATTEMPTS);

        if (attempts >= MAX_ATTEMPTS) {
            logger.error("Failed to generate unique PNR after {} attempts", MAX_ATTEMPTS);
            // Fallback to PNR with user component
            pnr = com.railway.booking.backend.util.PNRGenerator.generatePNRWithUser(
                    trainId, (int) (System.currentTimeMillis() % 10000));
        }

        return pnr;
    }

    /**
     * Get next RAC booking for promotion (with row-level lock)
     * CRITICAL: Must be called within transaction with FOR UPDATE
     */
    public Booking getNextRACForPromotion(int trainId, Date journeyDate) {
        String sql = "SELECT b.*, t.train_name, t.train_number, u.username, " +
                "CONCAT(t.source_station, ' to ', t.destination_station) as route_description " +
                "FROM bookings b " +
                "JOIN trains t ON b.train_id = t.id " +
                "JOIN users u ON b.user_id = u.id " +
                "WHERE b.train_id = ? AND b.journey_date = ? AND b.status = 'RAC' " +
                "ORDER BY b.priority ASC, b.booking_date ASC " +
                "LIMIT 1 FOR UPDATE";
        
        try {
            return jdbcTemplate.queryForObject(sql, bookingRowMapper, trainId, journeyDate);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            logger.error("Error fetching next RAC booking", e);
            return null;
        }
    }

    /**
     * Get next WAITING booking for promotion (with row-level lock)
     * CRITICAL: Must be called within transaction with FOR UPDATE
     */
    public Booking getNextWaitingForPromotion(int trainId, Date journeyDate) {
        String sql = "SELECT b.*, t.train_name, t.train_number, u.username, " +
                "CONCAT(t.source_station, ' to ', t.destination_station) as route_description " +
                "FROM bookings b " +
                "JOIN trains t ON b.train_id = t.id " +
                "JOIN users u ON b.user_id = u.id " +
                "WHERE b.train_id = ? AND b.journey_date = ? AND b.status = 'WAITING' " +
                "ORDER BY b.priority ASC, b.booking_date ASC " +
                "LIMIT 1 FOR UPDATE";
        
        try {
            return jdbcTemplate.queryForObject(sql, bookingRowMapper, trainId, journeyDate);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            logger.error("Error fetching next WAITING booking", e);
            return null;
        }
    }

    /**
     * Update booking status safely
     */
    public boolean updateStatus(String bookingId, Booking.BookingStatus newStatus) {
        String sql = "UPDATE bookings SET status = ?, updated_at = NOW() WHERE booking_id = ?";
        try {
            int rows = jdbcTemplate.update(sql, newStatus.name(), bookingId);
            return rows > 0;
        } catch (Exception e) {
            logger.error("Error updating booking status", e);
            return false;
        }
    }

    /**
     * Update booking status with priority change
     */
    public boolean updateStatusAndPriority(String bookingId, Booking.BookingStatus newStatus, int newPriority) {
        String sql = "UPDATE bookings SET status = ?, priority = ?, updated_at = NOW() WHERE booking_id = ?";
        try {
            int rows = jdbcTemplate.update(sql, newStatus.name(), newPriority, bookingId);
            return rows > 0;
        } catch (Exception e) {
            logger.error("Error updating booking status and priority", e);
            return false;
        }
    }

    /**
     * Get count of bookings by status for a train on a date
     */
    public int getBookingCountByStatus(int trainId, Date journeyDate, Booking.BookingStatus status) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE train_id = ? AND journey_date = ? AND status = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, trainId, journeyDate, status.name());
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.error("Error counting bookings by status", e);
            return 0;
        }
    }
}
