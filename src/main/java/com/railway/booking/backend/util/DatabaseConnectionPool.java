package com.railway.booking.backend.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connection manager using HikariCP connection pooling
 * This replaces the old single static connection approach
 */
public class DatabaseConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionPool.class);

    private static HikariDataSource dataSource;
    private static Properties properties = new Properties();

    static {
        try {
            // Load database properties from classpath
            java.io.InputStream input = DatabaseConnectionPool.class.getClassLoader()
                    .getResourceAsStream("config/database.properties");

            if (input != null) {
                properties.load(input);
                input.close();
                logger.info("Database properties loaded successfully");
            } else {
                // Fallback to default properties
                logger.warn("Database properties file not found, using defaults");
                setDefaultProperties();
            }

            // Environment variable overrides for cloud deployment (Docker, Railway, Render, K8s)
            applyEnvironmentOverrides();

            // Initialize HikariCP connection pool
            initializeConnectionPool();

        } catch (IOException e) {
            logger.error("Failed to load database configuration", e);
            setDefaultProperties();
            initializeConnectionPool();
        }
    }

    /**
     * Set default database properties
     */
    private static void setDefaultProperties() {
        properties.setProperty("db.url",
                "jdbc:mysql://localhost:3306/railway_booking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        properties.setProperty("db.username", "root");
        properties.setProperty("db.password", "");
        properties.setProperty("db.pool.maximumPoolSize", "10");
        properties.setProperty("db.pool.minimumIdle", "2");
        properties.setProperty("db.pool.connectionTimeout", "30000");
        properties.setProperty("db.pool.idleTimeout", "600000");
        properties.setProperty("db.pool.maxLifetime", "1800000");
    }

    /**
     * Apply environment variable overrides for containerized & cloud deployment
     */
    private static void applyEnvironmentOverrides() {
        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            properties.setProperty("db.url", envUrl.trim());
            logger.info("Using DB_URL from environment variable");
        }
        String envUser = System.getenv("DB_USERNAME");
        if (envUser != null && !envUser.trim().isEmpty()) {
            properties.setProperty("db.username", envUser.trim());
            logger.info("Using DB_USERNAME from environment variable: {}", envUser);
        }
        String envPass = System.getenv("DB_PASSWORD");
        if (envPass != null) {
            properties.setProperty("db.password", envPass);
        }
        String envDriver = System.getenv("DB_DRIVER");
        if (envDriver != null && !envDriver.trim().isEmpty()) {
            properties.setProperty("db.driver", envDriver.trim());
        }
    }

    /**
     * Initialize HikariCP connection pool
     */
    private static void initializeConnectionPool() {
        try {
            HikariConfig config = new HikariConfig();

            // JDBC settings
            config.setJdbcUrl(properties.getProperty("db.url"));
            config.setUsername(properties.getProperty("db.username"));
            config.setPassword(properties.getProperty("db.password"));

            // Set driver class if specified
            String driverClass = properties.getProperty("db.driver");
            if (driverClass != null && !driverClass.isEmpty()) {
                config.setDriverClassName(driverClass);
                logger.info("Using database driver: {}", driverClass);
            }

            // Pool configuration
            config.setMaximumPoolSize(Integer.parseInt(
                    properties.getProperty("db.pool.maximumPoolSize", "10")));
            config.setMinimumIdle(Integer.parseInt(
                    properties.getProperty("db.pool.minimumIdle", "2")));
            config.setConnectionTimeout(Long.parseLong(
                    properties.getProperty("db.pool.connectionTimeout", "30000")));
            config.setIdleTimeout(Long.parseLong(
                    properties.getProperty("db.pool.idleTimeout", "600000")));
            config.setMaxLifetime(Long.parseLong(
                    properties.getProperty("db.pool.maxLifetime", "1800000")));

            // Connection pool name
            config.setPoolName("RailwayBookingPool");

            // Performance optimizations for MySQL
            if (driverClass != null && driverClass.contains("mysql")) {
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                config.addDataSourceProperty("useServerPrepStmts", "true");
                config.addDataSourceProperty("useLocalSessionState", "true");
                config.addDataSourceProperty("rewriteBatchedStatements", "true");
                config.addDataSourceProperty("cacheResultSetMetadata", "true");
                config.addDataSourceProperty("cacheServerConfiguration", "true");
                config.addDataSourceProperty("elideSetAutoCommits", "true");
                config.addDataSourceProperty("maintainTimeStats", "false");
                logger.info("Applied MySQL-specific connection optimizations");
            }

            // Performance optimizations for PostgreSQL
            if (driverClass != null && driverClass.contains("postgresql")) {
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                config.addDataSourceProperty("tcpKeepAlive", "true");
                logger.info("Applied PostgreSQL-specific connection optimizations");
            }

            // Create the data source
            dataSource = new HikariDataSource(config);

            logger.info("HikariCP connection pool initialized successfully");
            logger.info("Database URL: {}", properties.getProperty("db.url"));
            logger.info("Pool configuration - Max: {}, Min: {}, Timeout: {}ms",
                    config.getMaximumPoolSize(),
                    config.getMinimumIdle(),
                    config.getConnectionTimeout());

        } catch (Exception e) {
            logger.error("Failed to initialize HikariCP connection pool", e);
            throw new RuntimeException("Could not initialize database connection pool", e);
        }
    }

    /**
     * Get a connection from the pool
     * 
     * @return Database connection from the pool
     * @throws SQLException if connection cannot be obtained
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            logger.error("Data source is null, attempting to reinitialize");
            initializeConnectionPool();
        }

        try {
            Connection conn = dataSource.getConnection();
            logger.debug("Connection obtained from pool");
            return conn;
        } catch (SQLException e) {
            logger.error("Failed to get connection from pool", e);
            throw e;
        }
    }

    /**
     * Get a connection for transaction (autoCommit = false)
     * CRITICAL for concurrency control
     * 
     * @return Database connection with autoCommit disabled
     * @throws SQLException if connection cannot be obtained
     */
    public static Connection getTransactionConnection() throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
        return conn;
    }

    /**
     * Get connection with specific transaction isolation level
     * 
     * @param isolationLevel Connection.TRANSACTION_* constant
     * @return Database connection configured for transactions
     * @throws SQLException if connection cannot be obtained
     */
    public static Connection getConnectionWithIsolation(int isolationLevel) throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
        conn.setTransactionIsolation(isolationLevel);
        logger.debug("Connection obtained with isolation level: {}", isolationLevel);
        return conn;
    }

    /**
     * Close the connection pool
     * Should be called when application shuts down
     */
    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("HikariCP connection pool closed successfully");
        }
    }

    /**
     * Test database connection
     */
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                logger.info("Database connection test successful");
            }
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
        }
    }

    /**
     * Get pool statistics
     */
    public static String getPoolStats() {
        if (dataSource != null) {
            return String.format(
                    "Pool Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
                    dataSource.getHikariPoolMXBean().getActiveConnections(),
                    dataSource.getHikariPoolMXBean().getIdleConnections(),
                    dataSource.getHikariPoolMXBean().getTotalConnections(),
                    dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
        }
        return "Pool not initialized";
    }

    /**
     * Print pool statistics
     */
    public static void printPoolStats() {
        String stats = getPoolStats();
        logger.info("Pool Statistics: {}", stats);
    }
}
