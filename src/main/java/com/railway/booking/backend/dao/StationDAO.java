package com.railway.booking.backend.dao;

import com.railway.booking.backend.model.Station;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StationDAO {
    private static final Logger logger = LoggerFactory.getLogger(StationDAO.class);
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Station> stationRowMapper = (rs, rowNum) -> {
        Station station = new Station();
        station.setId(rs.getInt("id"));
        station.setStationName(rs.getString("station_name"));
        station.setStationCode(rs.getString("station_code"));
        station.setCity(rs.getString("city"));
        station.setState(rs.getString("state"));
        return station;
    };

    public StationDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean addStation(Station station) {
        String sql = "INSERT INTO stations (station_name, station_code, city, state) VALUES (?, ?, ?, ?)";
        try {
            int rows = jdbcTemplate.update(sql, station.getStationName(), station.getStationCode(),
                station.getCity(), station.getState());
            return rows > 0;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return false;
        }
    }

    public Station getStationById(int stationId) {
        String sql = "SELECT * FROM stations WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, stationRowMapper, stationId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return null;
        }
    }

    public Station getStationByCode(String stationCode) {
        String sql = "SELECT * FROM stations WHERE station_code = ?";
        try {
            return jdbcTemplate.queryForObject(sql, stationRowMapper, stationCode);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return null;
        }
    }

    public List<Station> getAllStations() {
        String sql = "SELECT * FROM stations ORDER BY station_name";
        try {
            return jdbcTemplate.query(sql, stationRowMapper);
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return List.of();
        }
    }

    public List<Station> searchStationsByName(String name) {
        String sql = "SELECT * FROM stations WHERE station_name LIKE ? OR city LIKE ? ORDER BY station_name";
        try {
            String searchTerm = "%" + name + "%";
            return jdbcTemplate.query(sql, stationRowMapper, searchTerm, searchTerm);
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return List.of();
        }
    }

    public List<Station> getStationsByCity(String city) {
        String sql = "SELECT * FROM stations WHERE city = ? ORDER BY station_name";
        try {
            return jdbcTemplate.query(sql, stationRowMapper, city);
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return List.of();
        }
    }

    public boolean updateStation(Station station) {
        String sql = "UPDATE stations SET station_name = ?, station_code = ?, city = ?, state = ? WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql, station.getStationName(), station.getStationCode(),
                station.getCity(), station.getState(), station.getId());
            return rows > 0;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return false;
        }
    }

    public boolean deleteStation(int stationId) {
        String sql = "DELETE FROM stations WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql, stationId);
            return rows > 0;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return false;
        }
    }

    public boolean isStationCodeExists(String stationCode) {
        String sql = "SELECT COUNT(*) FROM stations WHERE station_code = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, stationCode);
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return false;
        }
    }
}
