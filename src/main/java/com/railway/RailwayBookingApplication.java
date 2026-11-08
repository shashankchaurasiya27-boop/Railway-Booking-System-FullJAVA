package com.railway;

import com.railway.booking.backend.util.DatabaseConnection;
import com.railway.booking.backend.util.DatabaseInitializer;
import com.railway.view.LoginFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import javax.swing.SwingUtilities;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.railway",
        "com.railway.booking.backend"
})
public class RailwayBookingApplication {
    private static final Logger logger = LoggerFactory.getLogger(RailwayBookingApplication.class);

    public static void main(String[] args) {
        // Spring Boot runs headless by default, we need to bypass it for Swing GUI
        System.setProperty("java.awt.headless", "false");
        logger.info("=== Railway Booking System Starting (Spring Boot) ===");

        // Add shutdown hook for cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Application shutting down...");
            com.railway.booking.backend.util.SessionManager.shutdown();
            DatabaseConnection.closeConnection();
            logger.info("Application shutdown complete");
        }));

        // Start Spring Boot application context
        ConfigurableApplicationContext context = SpringApplication.run(RailwayBookingApplication.class, args);
        logger.info("Spring Boot context started successfully");

        // Test database connection
        logger.info("Testing database connection");
        DatabaseConnection.testConnection();

        // Display connection pool statistics
        logger.info("Connection Pool Status: {}", DatabaseConnection.getPoolStats());

        // Check if database is initialized
        if (!DatabaseInitializer.isDatabaseInitialized()) {
            logger.info("Database not initialized. Initializing now...");
            if (DatabaseInitializer.initializeDatabase()) {
                logger.info("Database initialized successfully");
                DatabaseInitializer.printDatabaseStats();
            } else {
                logger.error("Failed to initialize database. Please check your MySQL connection.");
                logger.error("Make sure MySQL is running and the credentials in database.properties are correct.");
                System.exit(1);
            }
        } else {
            logger.info("Database already initialized");
            DatabaseInitializer.printDatabaseStats();
        }

        // Set system look and feel
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            logger.debug("System look and feel set successfully");
        } catch (Exception e) {
            logger.warn("Failed to set system look and feel", e);
        }

        // Launch the application on the Event Dispatch Thread
        // ===================================================================
        // BACKEND-ONLY MODE
        // ===================================================================
        // Swing UI is disabled - application runs as REST API server only
        // To re-enable UI, uncomment the code block below

        // Retrieve server port from Spring environment
        String serverPort = context.getEnvironment().getProperty("local.server.port", "8080");

        logger.info("=".repeat(70));
        logger.info("APPLICATION MODE: Backend-Only (REST API Server)");
        logger.info("Swing UI is DISABLED");
        logger.info("=".repeat(70));
        logger.info("REST API Server running on: http://localhost:{}", serverPort);
        logger.info("API Endpoints available at: http://localhost:{}/api", serverPort);
        logger.info("Health Check: http://localhost:{}/actuator/health", serverPort);
        logger.info("=".repeat(70));
        logger.info("Application started successfully in BACKEND-ONLY mode");
        logger.info("Press Ctrl+C to stop the server");

        // ===================================================================
        // SWING UI CODE
        // ===================================================================
        logger.info("Launching GUI");
        SwingUtilities.invokeLater(() -> {
            try {
                new LoginFrame().setVisible(true);
            } catch (Exception e) {
                logger.error("Failed to launch GUI", e);
                System.exit(1);
            }
        });
        logger.info("Application started successfully");
    }
}
