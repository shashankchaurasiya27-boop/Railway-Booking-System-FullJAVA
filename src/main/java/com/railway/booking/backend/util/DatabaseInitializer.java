package com.railway.booking.backend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class to initialize the database schema and load test data
 */
public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    /**
     * Initialize the database with schema and test data
     */
    public static boolean initializeDatabase() {
        logger.info("=== Starting Database Initialization ===");

        // First, create the schema
        if (!executeSchemaFile("database/unified_schema.sql")) {
            logger.error("Failed to create database schema");
            return false;
        }

        // Then, load the test data
        if (!executeSchemaFile("database/test_data.sql")) {
            logger.error("Failed to load test data");
            return false;
        }

        logger.info("=== Database Initialization Complete ===");
        return true;
    }

    /**
     * Execute SQL file from resources
     */
    private static boolean executeSchemaFile(String resourcePath) {
        logger.info("Executing: {}", resourcePath);

        try {
            // Read SQL file from resources
            InputStream inputStream = DatabaseInitializer.class.getClassLoader()
                    .getResourceAsStream(resourcePath);

            if (inputStream == null) {
                logger.error("Could not find resource: {}", resourcePath);
                return false;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sqlBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                // Skip comments and empty lines
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                sqlBuilder.append(line).append(" ");
            }
            reader.close();

            // Split by semicolon to get individual statements
            String[] sqlStatements = sqlBuilder.toString().split(";");

            // Execute each statement
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();

            int successCount = 0;
            int totalCount = 0;

            for (String sql : sqlStatements) {
                sql = sql.trim();
                if (sql.isEmpty()) {
                    continue;
                }

                totalCount++;
                try {
                    stmt.execute(sql);
                    successCount++;
                } catch (SQLException e) {
                    // Some statements might fail if tables already exist, which is okay
                    if (!e.getMessage().contains("already exists") &&
                            !e.getMessage().contains("Duplicate entry")) {
                        logger.warn("SQL Warning: {}", e.getMessage());
                    }
                }
            }

            stmt.close();
            logger.info("✓ Executed {}/{} statements from {}", successCount, totalCount, resourcePath);
            return true;

        } catch (Exception e) {
            logger.error("Error executing {}: {}", resourcePath, e.getMessage());
            return false;
        }
    }

    /**
     * Check if database is initialized
     */
    public static boolean isDatabaseInitialized() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();

            // Check if key tables exist
            stmt.executeQuery("SELECT COUNT(*) FROM users");
            stmt.executeQuery("SELECT COUNT(*) FROM trains");
            stmt.executeQuery("SELECT COUNT(*) FROM stations");
            stmt.executeQuery("SELECT COUNT(*) FROM train_seats");
            stmt.executeQuery("SELECT COUNT(*) FROM seat_availability");

            stmt.close();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Get database statistics
     */
    public static void printDatabaseStats() {
        logger.info("=== Database Statistics ===");

        String[] tables = { "users", "stations", "trains", "routes", "bookings", "tickets", "payments", "train_seats", "seat_availability" };

        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();

            for (String table : tables) {
                try {
                    var rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table);
                    if (rs.next()) {
                        logger.info("{}: {} records", table, rs.getInt(1));
                    }
                } catch (SQLException e) {
                    logger.warn("{}: Table not found", table);
                }
            }

            stmt.close();

        } catch (SQLException e) {
            logger.error("Error getting database stats: {}", e.getMessage());
        }
    }

    /**
     * Reset database (drop all tables)
     */
    public static boolean resetDatabase() {
        logger.info("=== Resetting Database ===");

        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();

            // Disable foreign key checks
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            // Drop tables in reverse order of dependencies
            String[] tables = { "payments", "tickets", "bookings", "seat_availability", "train_seats", "routes", "trains", "stations", "users" };

            for (String table : tables) {
                try {
                    stmt.execute("DROP TABLE IF EXISTS " + table);
                    logger.info("Dropped table: {}", table);
                } catch (SQLException e) {
                    logger.warn("Could not drop table {}", table);
                }
            }

            // Re-enable foreign key checks
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

            stmt.close();
            logger.info("=== Database Reset Complete ===");
            return true;

        } catch (SQLException e) {
            logger.error("Error resetting database: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Main method for standalone execution
     */
    public static void main(String[] args) {
        logger.info("Railway Booking System - Database Initializer");

        // Test database connection
        DatabaseConnection.testConnection();

        // Check if database is already initialized
        if (isDatabaseInitialized()) {
            logger.info("Database is already initialized.");
            logger.info("To reset and reinitialize, call resetDatabase() followed by initializeDatabase()");
        } else {
            logger.info("Database not initialized. Initializing now...");
            initializeDatabase();
        }

        // Print statistics
        printDatabaseStats();

        logger.info("Database initialization complete!");
    }
}
