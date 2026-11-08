package com.railway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Legacy main class - delegates to RailwayBookingApplication
 * Kept for backward compatibility
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Delegating to Spring Boot application...");
        RailwayBookingApplication.main(args);
    }
}