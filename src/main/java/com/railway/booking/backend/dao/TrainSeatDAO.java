package com.railway.booking.backend.dao;

import com.railway.booking.backend.model.TrainSeat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DAO for TrainSeat entity - manages seat configuration per coach type
 */
@Repository
public class TrainSeatDAO {
    private static final Logger logger = LoggerFactory.getLogger(TrainSeatDAO.class);
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<TrainSeat> trainSeatRowMapper = (rs, rowNum) -> {
        TrainSeat trainSeat = new TrainSeat();
        trainSeat.setTrainSeatId(rs.getInt("id"));
        trainSeat.setTrainId(rs.getInt("train_id"));
        trainSeat.setCoachType(TrainSeat.CoachType.valueOf(rs.getString("coach_type")));
        trainSeat.setTotalSeats(rs.getInt("total_seats"));
        trainSeat.setFareMultiplier(rs.getBigDecimal("fare_multiplier"));
        trainSeat.setCreatedAt(rs.getTimestamp("created_at"));
        trainSeat.setUpdatedAt(rs.getTimestamp("updated_at"));
        return trainSeat;
    };

    public TrainSeatDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Get all seat configurations for a train
     */
    public List<TrainSeat> getSeatsByTrainId(int trainId) {
        String sql = "SELECT * FROM train_seats WHERE train_id = ? ORDER BY coach_type";
        try {
            return jdbcTemplate.query(sql, trainSeatRowMapper, trainId);
        } catch (Exception e) {
            logger.error("Error getting train seats for trainId: {}", trainId, e);
            return List.of();
        }
    }

    /**
     * Get specific coach type configuration for a train
     */
    public TrainSeat getSeatByTrainAndType(int trainId, TrainSeat.CoachType coachType) {
        String sql = "SELECT * FROM train_seats WHERE train_id = ? AND coach_type = ?";
        try {
            return jdbcTemplate.queryForObject(sql, trainSeatRowMapper, trainId, coachType.name());
        } catch (EmptyResultDataAccessException e) {
            logger.debug("No seat config found for trainId: {}, coachType: {}", trainId, coachType);
            return null;
        } catch (Exception e) {
            logger.error("Error getting train seat config", e);
            return null;
        }
    }

    /**
     * Create train seat configuration
     */
    public boolean createTrainSeat(TrainSeat trainSeat) {
        String sql = "INSERT INTO train_seats (train_id, coach_type, total_seats, fare_multiplier) VALUES (?, ?, ?, ?)";
        try {
            int rows = jdbcTemplate.update(sql,
                    trainSeat.getTrainId(),
                    trainSeat.getCoachType().name(),
                    trainSeat.getTotalSeats(),
                    trainSeat.getFareMultiplier());
            return rows > 0;
        } catch (Exception e) {
            logger.error("Error creating train seat config", e);
            return false;
        }
    }

    /**
     * Update train seat configuration
     */
    public boolean updateTrainSeat(TrainSeat trainSeat) {
        String sql = "UPDATE train_seats SET total_seats = ?, fare_multiplier = ? WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql,
                    trainSeat.getTotalSeats(),
                    trainSeat.getFareMultiplier(),
                    trainSeat.getTrainSeatId());
            return rows > 0;
        } catch (Exception e) {
            logger.error("Error updating train seat config", e);
            return false;
        }
    }

    /**
     * Delete train seat configuration
     */
    public boolean deleteTrainSeat(int trainSeatId) {
        String sql = "DELETE FROM train_seats WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql, trainSeatId);
            return rows > 0;
        } catch (Exception e) {
            logger.error("Error deleting train seat config", e);
            return false;
        }
    }

    /**
     * Check if coach type exists for train
     */
    public boolean coachTypeExists(int trainId, TrainSeat.CoachType coachType) {
        String sql = "SELECT COUNT(*) FROM train_seats WHERE train_id = ? AND coach_type = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, trainId, coachType.name());
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("Error checking coach type existence", e);
            return false;
        }
    }

    /**
     * Get fare multiplier for coach type
     */
    public double getFareMultiplier(int trainId, TrainSeat.CoachType coachType) {
        String sql = "SELECT fare_multiplier FROM train_seats WHERE train_id = ? AND coach_type = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Double.class, trainId, coachType.name());
        } catch (Exception e) {
            logger.warn("Fare multiplier not found for trainId: {}, coachType: {}, using default 1.0", trainId, coachType);
            return 1.0;
        }
    }
}
