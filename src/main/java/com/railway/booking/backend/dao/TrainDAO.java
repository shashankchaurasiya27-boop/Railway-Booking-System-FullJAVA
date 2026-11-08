package com.railway.booking.backend.dao;

import com.railway.booking.backend.model.Train;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TrainDAO {
    private static final Logger logger = LoggerFactory.getLogger(TrainDAO.class);
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Train> trainRowMapper = (rs, rowNum) -> {
        Train train = new Train();
        train.setTrainId(rs.getInt("id"));
        train.setTrainNumber(rs.getString("train_number"));
        train.setTrainName(rs.getString("train_name"));
        train.setTrainType(Train.TrainType.valueOf(rs.getString("train_type")));
        train.setTotalSeats(rs.getInt("total_seats"));
        train.setStatus(Train.TrainStatus.valueOf(rs.getString("status")));
        return train;
    };

    public TrainDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Train getTrainById(int trainId) {
        String sql = "SELECT * FROM trains WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, trainRowMapper, trainId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return null;
        }
    }

    public Train getTrainByNumber(String trainNumber) {
        String sql = "SELECT * FROM trains WHERE train_number = ?";
        try {
            return jdbcTemplate.queryForObject(sql, trainRowMapper, trainNumber);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return null;
        }
    }

    public List<Train> getAllTrains() {
        String sql = "SELECT * FROM trains ORDER BY train_number";
        try {
            return jdbcTemplate.query(sql, trainRowMapper);
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return List.of();
        }
    }

    public boolean createTrain(Train train) {
        String sql = "INSERT INTO trains (train_number, train_name, train_type, total_seats, status) VALUES (?, ?, ?, ?, ?)";
        try {
            int rows = jdbcTemplate.update(sql, train.getTrainNumber(), train.getTrainName(), train.getTrainType().name(), train.getTotalSeats(), train.getStatus().name());
            return rows > 0;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return false;
        }
    }

    public boolean updateTrain(Train train) {
        String sql = "UPDATE trains SET train_number = ?, train_name = ?, train_type = ?, total_seats = ?, status = ? WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql, train.getTrainNumber(), train.getTrainName(), 
                train.getTrainType().name(), train.getTotalSeats(), train.getStatus().name(), train.getTrainId());
            return rows > 0;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return false;
        }
    }

    public boolean deleteTrain(int trainId) {
        String sql = "DELETE FROM trains WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql, trainId);
            return rows > 0;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return false;
        }
    }
}
