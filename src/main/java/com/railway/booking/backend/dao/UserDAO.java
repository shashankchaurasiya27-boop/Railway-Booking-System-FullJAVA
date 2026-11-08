package com.railway.booking.backend.dao;

import com.railway.booking.backend.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private final JdbcTemplate jdbcTemplate;

    // RowMapper for User entity
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setUserId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setAddress(rs.getString("address"));
        user.setUserType(User.UserType.valueOf(rs.getString("role")));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    };

    public UserDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Register a new user
     */
    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, password, first_name, last_name, email, phone, address, role) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            int rowsAffected = jdbcTemplate.update(sql,
                    user.getUsername(),
                    user.getPassword(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getAddress(),
                    user.getUserType().name());
            return rowsAffected > 0;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return false;
        }
    }

    /**
     * Authenticate user login
     */
    public User authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try {
            return jdbcTemplate.queryForObject(sql, userRowMapper, username, password);
        } catch (EmptyResultDataAccessException e) {
            logger.debug("No user found with username: {}", username);
            return null;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return null;
        }
    }

    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, userRowMapper, userId);
        } catch (EmptyResultDataAccessException e) {
            logger.debug("No user found with ID: {}", userId);
            return null;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return null;
        }
    }

    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try {
            return jdbcTemplate.queryForObject(sql, userRowMapper, username);
        } catch (EmptyResultDataAccessException e) {
            logger.debug("No user found with username: {}", username);
            return null;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return null;
        }
    }

    /**
     * Check if username exists
     */
    public boolean isUsernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return false;
        }
    }

    /**
     * Check if email exists
     */
    public boolean isEmailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return false;
        }
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";

        try {
            return jdbcTemplate.query(sql, userRowMapper);
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return List.of();
        }
    }

    /**
     * Update user profile
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, " +
                "phone = ?, address = ? WHERE id = ?";

        try {
            int rowsAffected = jdbcTemplate.update(sql,
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getAddress(),
                    user.getUserId());
            return rowsAffected > 0;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return false;
        }
    }

    /**
     * Delete user
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";

        try {
            int rowsAffected = jdbcTemplate.update(sql, userId);
            return rowsAffected > 0;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return false;
        }
    }

    /**
     * Update user password
     */
    public boolean updateUserPassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";

        try {
            int rowsAffected = jdbcTemplate.update(sql, newPassword, userId);
            return rowsAffected > 0;
        } catch (Exception e) {
            logger.error("Database operation failed", e);
            return false;
        }
    }
}
