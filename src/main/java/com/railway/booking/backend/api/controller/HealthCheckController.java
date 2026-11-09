package com.railway.booking.backend.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller for production readiness verification
 */
@RestController
@RequestMapping("/api/health")
public class HealthCheckController {
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);
    private final JdbcTemplate jdbcTemplate;
    private final LocalDateTime startupTime = LocalDateTime.now();

    public HealthCheckController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Basic health check endpoint
     * GET /api/health
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();

        try {
            // Application status
            health.put("status", "UP");
            health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            health.put("application", "Railway Booking System");
            health.put("version", "1.0.0");
            health.put("mode", "Backend-Only (REST API)");

            // Runtime information
            Map<String, Object> runtime = new HashMap<>();
            runtime.put("javaVersion", System.getProperty("java.version"));
            runtime.put("javaVendor", System.getProperty("java.vendor"));
            runtime.put("javaVirtualThreads", isVirtualThreadsSupported());
            runtime.put("osName", System.getProperty("os.name"));
            runtime.put("osVersion", System.getProperty("os.version"));
            runtime.put("processors", Runtime.getRuntime().availableProcessors());
            health.put("runtime", runtime);

            // Memory information
            Map<String, Object> memory = new HashMap<>();
            Runtime rt = Runtime.getRuntime();
            long maxMemory = rt.maxMemory() / (1024 * 1024);
            long totalMemory = rt.totalMemory() / (1024 * 1024);
            long freeMemory = rt.freeMemory() / (1024 * 1024);
            long usedMemory = totalMemory - freeMemory;

            memory.put("maxMB", maxMemory);
            memory.put("totalMB", totalMemory);
            memory.put("usedMB", usedMemory);
            memory.put("freeMB", freeMemory);
            memory.put("usagePercent", (usedMemory * 100) / totalMemory);
            health.put("memory", memory);

            // Uptime
            long uptimeSeconds = java.time.Duration.between(startupTime, LocalDateTime.now()).getSeconds();
            health.put("uptimeSeconds", uptimeSeconds);
            health.put("uptimeFormatted", formatUptime(uptimeSeconds));

            // Database connectivity check
            Map<String, Object> database = checkDatabaseHealth();
            health.put("database", database);

            // Overall health status
            boolean isHealthy = "UP".equals(database.get("status"));
            health.put("status", isHealthy ? "UP" : "DOWN");

            logger.info("Health check performed - Status: {}", health.get("status"));

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            logger.error("Health check failed", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }

    /**
     * Detailed health check with database statistics
     * GET /api/health/detailed
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            // Get basic health
            ResponseEntity<Map<String, Object>> basicHealth = health();
            health.putAll(basicHealth.getBody());

            // Add database statistics
            Map<String, Object> dbStats = getDatabaseStatistics();
            health.put("databaseStatistics", dbStats);

            // Thread information
            Map<String, Object> threads = new HashMap<>();
            threads.put("activeThreads", Thread.activeCount());
            threads.put("currentThread", Thread.currentThread().getName());
            threads.put("isVirtual", Thread.currentThread().isVirtual());
            health.put("threads", threads);

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            logger.error("Detailed health check failed", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }

    /**
     * Database connectivity check
     */
    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> db = new HashMap<>();

        try {
            // Test database connection with a simple query
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            if (result != null && result == 1) {
                db.put("status", "UP");
                db.put("message", "Database connection successful");

                // Get database info (compatible with PostgreSQL and MySQL)
                String dbName = "database";
                try {
                    dbName = jdbcTemplate.queryForObject("SELECT current_database()", String.class);
                } catch (Exception e1) {
                    try {
                        dbName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
                    } catch (Exception ignored) {
                    }
                }
                db.put("database", dbName);

                String dbVersion = jdbcTemplate.queryForObject(
                        "SELECT VERSION()", String.class);
                db.put("version", dbVersion);
            } else {
                db.put("status", "DOWN");
                db.put("message", "Database query returned unexpected result");
            }

        } catch (Exception e) {
            logger.error("Database health check failed", e);
            db.put("status", "DOWN");
            db.put("message", "Database connection failed: " + e.getMessage());
        }

        return db;
    }

    /**
     * Get database statistics
     */
    private Map<String, Object> getDatabaseStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            stats.put("users", getTableCount("users"));
            stats.put("stations", getTableCount("stations"));
            stats.put("trains", getTableCount("trains"));
            stats.put("routes", getTableCount("routes"));
            stats.put("bookings", getTableCount("bookings"));
            stats.put("tickets", getTableCount("tickets"));
            stats.put("payments", getTableCount("payments"));
        } catch (Exception e) {
            logger.error("Failed to get database statistics", e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * Get count of records in a table
     */
    private int getTableCount(String tableName) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM " + tableName, Integer.class);
        } catch (Exception e) {
            logger.warn("Failed to get count for table: {}", tableName);
            return -1;
        }
    }

    /**
     * Check if virtual threads are supported
     */
    private boolean isVirtualThreadsSupported() {
        try {
            // Try to create a virtual thread to verify support
            Thread vt = Thread.ofVirtual().unstarted(() -> {
            });
            return vt.isVirtual();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Format uptime in human-readable format
     */
    private String formatUptime(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours, minutes, secs);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
}
