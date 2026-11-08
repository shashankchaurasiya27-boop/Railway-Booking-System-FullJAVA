package com.railway.booking.backend.dao;

import com.railway.booking.backend.model.BookingLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * BookingLogDAO - Audit trail for booking status changes
 * Tracks complete history of booking lifecycle
 */
@Repository
public class BookingLogDAO {
    private static final Logger logger = LoggerFactory.getLogger(BookingLogDAO.class);
    private final JdbcTemplate jdbcTemplate;

    // RowMapper for BookingLog entity
    private final RowMapper<BookingLog> logRowMapper = (rs, rowNum) -> {
        BookingLog log = new BookingLog();
        log.setId(rs.getInt("id"));
        log.setBookingId(rs.getString("booking_id"));
        log.setOldStatus(rs.getString("old_status"));
        log.setNewStatus(rs.getString("new_status"));
        log.setMessage(rs.getString("message"));
        log.setTimestamp(rs.getTimestamp("timestamp"));
        return log;
    };

    public BookingLogDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Log a booking status change
     * @param bookingId The booking ID
     * @param oldStatus Previous status (can be null for new bookings)
     * @param newStatus New status
     * @param message Description of the change
     * @return true if logged successfully
     */
    public boolean logStatusChange(String bookingId, String oldStatus, String newStatus, String message) {
        String sql = "INSERT INTO booking_logs (booking_id, old_status, new_status, message) VALUES (?, ?, ?, ?)";
        try {
            int rows = jdbcTemplate.update(sql, bookingId, oldStatus, newStatus, message);
            if (rows > 0) {
                logger.debug("Logged status change for booking {}: {} → {}", bookingId, oldStatus, newStatus);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error logging status change for booking {}", bookingId, e);
            return false;
        }
    }

    /**
     * Log a booking status change using existing connection (for transactions)
     * @param conn Database connection
     * @param bookingId The booking ID
     * @param oldStatus Previous status
     * @param newStatus New status
     * @param message Description of the change
     * @return true if logged successfully
     */
    public boolean logStatusChange(Connection conn, String bookingId, String oldStatus, String newStatus, String message) throws SQLException {
        String sql = "INSERT INTO booking_logs (booking_id, old_status, new_status, message) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId);
            ps.setString(2, oldStatus);
            ps.setString(3, newStatus);
            ps.setString(4, message);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                logger.debug("Logged status change for booking {}: {} → {}", bookingId, oldStatus, newStatus);
                return true;
            }
            return false;
        }
    }

    /**
     * Get all logs for a specific booking
     * @param bookingId The booking ID
     * @return List of log entries ordered by timestamp
     */
    public List<BookingLog> getLogsByBookingId(String bookingId) {
        String sql = "SELECT * FROM booking_logs WHERE booking_id = ? ORDER BY timestamp ASC";
        try {
            return jdbcTemplate.query(sql, logRowMapper, bookingId);
        } catch (Exception e) {
            logger.error("Error fetching logs for booking {}", bookingId, e);
            return List.of();
        }
    }

    /**
     * Get all logs for a specific booking with user and train details
     * @param bookingId The booking ID
     * @return List of log entries with additional details
     */
    public List<BookingLog> getLogsByBookingIdWithDetails(String bookingId) {
        String sql = "SELECT bl.*, u.username, t.train_name " +
                     "FROM booking_logs bl " +
                     "JOIN bookings b ON bl.booking_id = b.booking_id " +
                     "JOIN users u ON b.user_id = u.id " +
                     "JOIN trains t ON b.train_id = t.id " +
                     "WHERE bl.booking_id = ? " +
                     "ORDER BY bl.timestamp ASC";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                BookingLog log = logRowMapper.mapRow(rs, rowNum);
                log.setUserName(rs.getString("username"));
                log.setTrainName(rs.getString("train_name"));
                return log;
            }, bookingId);
        } catch (Exception e) {
            logger.error("Error fetching logs with details for booking {}", bookingId, e);
            return List.of();
        }
    }

    /**
     * Get recent logs across all bookings
     * @param limit Maximum number of logs to return
     * @return List of recent log entries
     */
    public List<BookingLog> getRecentLogs(int limit) {
        String sql = "SELECT * FROM booking_logs ORDER BY timestamp DESC LIMIT ?";
        try {
            return jdbcTemplate.query(sql, logRowMapper, limit);
        } catch (Exception e) {
            logger.error("Error fetching recent logs", e);
            return List.of();
        }
    }

    /**
     * Delete all logs for a booking (called when booking is deleted)
     * @param bookingId The booking ID
     * @return true if deleted successfully
     */
    public boolean deleteLogsByBookingId(String bookingId) {
        String sql = "DELETE FROM booking_logs WHERE booking_id = ?";
        try {
            int rows = jdbcTemplate.update(sql, bookingId);
            logger.debug("Deleted {} logs for booking {}", rows, bookingId);
            return rows >= 0;
        } catch (Exception e) {
            logger.error("Error deleting logs for booking {}", bookingId, e);
            return false;
        }
    }
}
