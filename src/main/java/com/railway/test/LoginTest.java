package com.railway.test;

import com.railway.controller.LoginController;
import com.railway.booking.backend.model.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginTest {
    private static final Logger logger = LoggerFactory.getLogger(LoginTest.class);

    public static void main(String[] args) {
        logger.info("Testing Login Credentials...");
        logger.info("================================");

        // TODO: Convert to Spring Boot Test framework with @SpringBootTest
        // For now, manually create dependencies for testing
        // Note: This test should be run through Spring Boot context for proper
        // JdbcTemplate
        try {
            var dataSource = new com.zaxxer.hikari.HikariDataSource();
            dataSource.setJdbcUrl(
                    "jdbc:mysql://localhost:3306/railway_booking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
            dataSource.setUsername("root");
            dataSource.setPassword("");

            var jdbcTemplate = new org.springframework.jdbc.core.JdbcTemplate(dataSource);

            LoginController loginController = new LoginController(
                    new com.railway.booking.backend.service.UserService(
                            new com.railway.booking.backend.dao.UserDAO(jdbcTemplate)));

            // Test admin credentials
            logger.info("Testing Admin Login:");
            Session adminSession = loginController.login("admin", "admin123");
            if (adminSession != null) {
                logger.info("Admin login SUCCESS!");
                logger.info("   Username: {}", adminSession.getUsername());
                logger.info("   Role: {}", adminSession.getUserType());
                logger.info("   Session ID: {}", adminSession.getSessionId());
                logger.info("   Expires at: {}", adminSession.getExpiresAt());
            } else {
                logger.error("Admin login FAILED!");
            }

            // Test regular user credentials
            logger.info("Testing User Login:");
            Session regularSession = loginController.login("john_doe", "password123");
            if (regularSession != null) {
                logger.info("User login SUCCESS!");
                logger.info("   Username: {}", regularSession.getUsername());
                logger.info("   Role: {}", regularSession.getUserType());
                logger.info("   Session ID: {}", regularSession.getSessionId());
            } else {
                logger.error("User login FAILED!");
            }

            // Test wrong credentials
            logger.info("Testing Wrong Credentials:");
            Session wrongSession = loginController.login("wrong_user", "wrong_password");
            if (wrongSession != null) {
                logger.error("Wrong credentials should have failed!");
            } else {
                logger.info("Wrong credentials correctly rejected!");
            }

            logger.info("Login test completed!");

            // Close datasource
            dataSource.close();
        } catch (Exception e) {
            logger.error("Test failed with exception", e);
        }
    }
}
