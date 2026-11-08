package com.railway.booking.backend.dao;

import com.railway.booking.backend.model.SeatAvailability;
import com.railway.booking.backend.model.TrainSeat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;

/**
 * DAO for SeatAvailability - Real-time seat tracking per journey date
 * CRITICAL: Uses row-level locking to prevent overbooking
 */
@Repository
public class SeatAvailabilityDAO {
    private static final Logger logger = LoggerFactory.getLogger(SeatAvailabilityDAO.class);
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<SeatAvailability> availabilityRowMapper = (rs, rowNum) -> {
        SeatAvailability sa = new SeatAvailability();
        sa.setAvailabilityId(rs.getInt("id"));
        sa.setTrainId(rs.getInt("train_id"));
        sa.setCoachType(TrainSeat.CoachType.valueOf(rs.getString("coach_type")));
        sa.setJourneyDate(rs.getDate("journey_date"));
        sa.setTotalSeats(rs.getInt("total_seats"));
        sa.setAvailableSeats(rs.getInt("available_seats"));
        sa.setBookedSeats(rs.getInt("booked_seats"));
        sa.setLastUpdated(rs.getTimestamp("last_updated"));
        return sa;
    };

    private final RowMapper<SeatAvailability> availabilityWithTrainRowMapper = (rs, rowNum) -> {
        SeatAvailability sa = availabilityRowMapper.mapRow(rs, rowNum);
        sa.setTrainNumber(rs.getString("train_number"));
        sa.setTrainName(rs.getString("train_name"));
        return sa;
    };

    public SeatAvailabilityDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Get availability for specific train, coach type, and date
     * Uses SELECT FOR UPDATE to prevent race conditions
     */
    @Transactional
    public SeatAvailability getAvailabilityForUpdate(int trainId, TrainSeat.CoachType coachType, Date journeyDate) {
        String sql = "SELECT * FROM seat_availability WHERE train_id = ? AND coach_type = ? AND journey_date = ? FOR UPDATE";
        try {
            return jdbcTemplate.queryForObject(sql, availabilityRowMapper, trainId, coachType.name(), journeyDate);
        } catch (EmptyResultDataAccessException e) {
            logger.debug("No availability record found, will create new one");
            return null;
        } catch (Exception e) {
            logger.error("Error getting availability with lock", e);
            return null;
        }
    }

    /**
     * Get availability (without lock) for reading only
     */
    public SeatAvailability getAvailability(int trainId, TrainSeat.CoachType coachType, Date journeyDate) {
        String sql = "SELECT * FROM seat_availability WHERE train_id = ? AND coach_type = ? AND journey_date = ?";
        try {
            return jdbcTemplate.queryForObject(sql, availabilityRowMapper, trainId, coachType.name(), journeyDate);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            logger.error("Error getting availability", e);
            return null;
        }
    }

    /**
     * Get all availability for a train on a specific date
     */
    public List<SeatAvailability> getAvailabilityByTrainAndDate(int trainId, Date journeyDate) {
        String sql = "SELECT sa.*, t.train_number, t.train_name FROM seat_availability sa " +
                     "JOIN trains t ON sa.train_id = t.id " +
                     "WHERE sa.train_id = ? AND sa.journey_date = ? ORDER BY sa.coach_type";
        try {
            return jdbcTemplate.query(sql, availabilityWithTrainRowMapper, trainId, journeyDate);
        } catch (Exception e) {
            logger.error("Error getting availability for train: {}, date: {}", trainId, journeyDate, e);
            return List.of();
        }
    }

    /**
     * Initialize availability record for a new journey date
     */
    public boolean initializeAvailability(int trainId, TrainSeat.CoachType coachType, Date journeyDate, int totalSeats) {
        String sql = "INSERT INTO seat_availability (train_id, coach_type, journey_date, total_seats, available_seats, booked_seats) " +
                     "VALUES (?, ?, ?, ?, ?, 0) ON DUPLICATE KEY UPDATE total_seats = ?";
        try {
            int rows = jdbcTemplate.update(sql, trainId, coachType.name(), journeyDate, totalSeats, totalSeats, totalSeats);
            return rows > 0;
        } catch (Exception e) {
            logger.error("Error initializing availability", e);
            return false;
        }
    }

    /**
     * BOOK SEATS - Decrease available seats
     * Returns true if booking successful
     */
    @Transactional
    public boolean bookSeats(int trainId, TrainSeat.CoachType coachType, Date journeyDate, int seatsToBook) {
        // First check if enough seats available with lock
        String checkSql = "SELECT available_seats FROM seat_availability WHERE train_id = ? AND coach_type = ? AND journey_date = ? FOR UPDATE";
        try {
            Integer available = jdbcTemplate.queryForObject(checkSql, Integer.class, trainId, coachType.name(), journeyDate);
            if (available == null || available < seatsToBook) {
                logger.warn("Not enough seats available. Requested: {}, Available: {}", seatsToBook, available);
                return false;
            }

            // Update availability
            String updateSql = "UPDATE seat_availability SET available_seats = available_seats - ?, booked_seats = booked_seats + ? " +
                               "WHERE train_id = ? AND coach_type = ? AND journey_date = ? AND available_seats >= ?";
            int rows = jdbcTemplate.update(updateSql, seatsToBook, seatsToBook, trainId, coachType.name(), journeyDate, seatsToBook);
            
            if (rows > 0) {
                logger.info("Booked {} seats for train: {}, coach: {}, date: {}", seatsToBook, trainId, coachType, journeyDate);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error booking seats", e);
            return false;
        }
    }

    /**
     * CANCEL SEATS - Increase available seats
     * Returns true if cancellation successful
     */
    @Transactional
    public boolean releaseSeats(int trainId, TrainSeat.CoachType coachType, Date journeyDate, int seatsToRelease) {
        String sql = "UPDATE seat_availability SET available_seats = available_seats + ?, booked_seats = booked_seats - ? " +
                     "WHERE train_id = ? AND coach_type = ? AND journey_date = ? AND booked_seats >= ?";
        try {
            int rows = jdbcTemplate.update(sql, seatsToRelease, seatsToRelease, trainId, coachType.name(), journeyDate, seatsToRelease);
            if (rows > 0) {
                logger.info("Released {} seats for train: {}, coach: {}, date: {}", seatsToRelease, trainId, coachType, journeyDate);
                return true;
            }
            logger.warn("Failed to release seats - may already be released");
            return false;
        } catch (Exception e) {
            logger.error("Error releasing seats", e);
            return false;
        }
    }

    /**
     * Check if seats are available (quick check without booking)
     */
    public boolean isAvailable(int trainId, TrainSeat.CoachType coachType, Date journeyDate, int requestedSeats) {
        String sql = "SELECT available_seats FROM seat_availability WHERE train_id = ? AND coach_type = ? AND journey_date = ?";
        try {
            Integer available = jdbcTemplate.queryForObject(sql, Integer.class, trainId, coachType.name(), journeyDate);
            return available != null && available >= requestedSeats;
        } catch (EmptyResultDataAccessException e) {
            // No record exists - need to initialize first
            return false;
        } catch (Exception e) {
            logger.error("Error checking availability", e);
            return false;
        }
    }

    /**
     * Get available seats count
     */
    public int getAvailableSeats(int trainId, TrainSeat.CoachType coachType, Date journeyDate) {
        String sql = "SELECT available_seats FROM seat_availability WHERE train_id = ? AND coach_type = ? AND journey_date = ?";
        try {
            Integer available = jdbcTemplate.queryForObject(sql, Integer.class, trainId, coachType.name(), journeyDate);
            return available != null ? available : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get all availability records (for admin/reporting)
     */
    public List<SeatAvailability> getAllAvailability() {
        String sql = "SELECT sa.*, t.train_number, t.train_name FROM seat_availability sa " +
                     "JOIN trains t ON sa.train_id = t.id ORDER BY sa.journey_date DESC, t.train_number";
        try {
            return jdbcTemplate.query(sql, availabilityWithTrainRowMapper);
        } catch (Exception e) {
            logger.error("Error getting all availability", e);
            return List.of();
        }
    }

    /**
     * Delete old availability records (cleanup)
     */
    public boolean deleteOldAvailability(Date beforeDate) {
        String sql = "DELETE FROM seat_availability WHERE journey_date < ?";
        try {
            int rows = jdbcTemplate.update(sql, beforeDate);
            logger.info("Deleted {} old availability records", rows);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting old availability", e);
            return false;
        }
    }

    /**
     * Get availability summary grouped by coach type for a train on a date
     * Useful for displaying available seats by coach type in search results
     */
    public List<SeatAvailability> getAvailabilitySummaryByTrain(int trainId, Date journeyDate) {
        String sql = "SELECT sa.*, t.train_number, t.train_name FROM seat_availability sa " +
                     "JOIN trains t ON sa.train_id = t.id " +
                     "WHERE sa.train_id = ? AND sa.journey_date = ? " +
                     "ORDER BY sa.coach_type";
        try {
            return jdbcTemplate.query(sql, availabilityWithTrainRowMapper, trainId, journeyDate);
        } catch (Exception e) {
            logger.error("Error getting availability summary for train: {}, date: {}", trainId, journeyDate, e);
            return List.of();
        }
    }
}
