package com.railway.booking.backend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database connection utility - now uses HikariCP connection pooling
 * This class maintains backward compatibility while delegating to DatabaseConnectionPool
 */
public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    
    /**
     * Get a database connection from the pool
     * 
     * @return Database connection
     * @throws SQLException if connection cannot be obtained
     */
    public static Connection getConnection() throws SQLException {
        logger.debug("Getting connection from pool");
        return DatabaseConnectionPool.getConnection();
    }
    
    /**
     * Close the connection pool
     * Note: Individual connections should be closed in try-with-resources
     * This method closes the entire pool and should only be called on shutdown
     */
    public static void closeConnection() {
        logger.info("Closing connection pool");
        DatabaseConnectionPool.closePool();
    }
    
    /**
     * Test database connection
     */
    public static void testConnection() {
        DatabaseConnectionPool.testConnection();
    }
    
    /**
     * Get pool statistics
     */
    public static String getPoolStats() {
        return DatabaseConnectionPool.getPoolStats();
    }
    
    /**
     * Print pool statistics
     */
    public static void printPoolStats() {
        DatabaseConnectionPool.printPoolStats();
    }
}