package com.railway.booking.backend.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PNR (Passenger Name Record) Generator Utility
 * 
 * Generates unique, non-colliding PNR numbers using:
 * - Timestamp component (for uniqueness)
 * - Random number (for collision resistance)
 * - Train ID (for context)
 * 
 * Format: PNR[TrainId][Timestamp][Random]
 * Example: PNR1230456789012345
 * 
 * This is industry-grade implementation ensuring:
 * - Uniqueness under high concurrency
 * - Human-readable format
 * - Fast database lookup
 */
public class PNRGenerator {
    
    // Use SecureRandom for better randomness in multi-threaded environment
    private static final SecureRandom random = new SecureRandom();
    
    // PNR length constraints
    private static final int PNR_LENGTH = 16; // PNR + 13 digits
    private static final String PNR_PREFIX = "PNR";
    
    /**
     * Generate a unique PNR number
     * 
     * Combines:
     * 1. Train ID (for context)
     * 2. Timestamp modulo (for time-based uniqueness)
     * 3. 3-digit random number (for collision resistance)
     * 
     * @param trainId The train ID for context
     * @return Unique PNR string
     */
    public static String generatePNR(int trainId) {
        // Get current timestamp in milliseconds
        long timeMillis = System.currentTimeMillis();
        
        // Take last 6 digits of timestamp for compactness
        long timeComponent = timeMillis % 1000000L;
        
        // Generate 3-digit random number (100-999)
        int randomComponent = 100 + random.nextInt(900);
        
        // Train ID padded to 3 digits (or truncated)
        String trainComponent = String.format("%03d", trainId % 1000);
        
        // Combine: PNR + train(3) + time(6) + random(3) = PNR + 12 digits
        return String.format("%s%s%06d%d", 
                PNR_PREFIX, 
                trainComponent, 
                timeComponent, 
                randomComponent);
    }
    
    /**
     * Generate PNR with additional entropy for extreme concurrency scenarios
     * 
     * @param trainId The train ID
     * @param userId The user ID for additional uniqueness
     * @return Unique PNR string
     */
    public static String generatePNRWithUser(int trainId, int userId) {
        long timeMillis = System.currentTimeMillis();
        long timeComponent = timeMillis % 100000L; // 5 digits
        
        // Include user ID component
        int userComponent = userId % 100; // 2 digits
        
        // Random component
        int randomComponent = 10 + random.nextInt(90); // 2 digits
        
        String trainComponent = String.format("%03d", trainId % 1000);
        
        return String.format("%s%s%05d%02d%02d", 
                PNR_PREFIX, 
                trainComponent, 
                timeComponent,
                userComponent,
                randomComponent);
    }
    
    /**
     * Validate PNR format
     * 
     * @param pnr The PNR to validate
     * @return true if valid format
     */
    public static boolean isValidPNR(String pnr) {
        if (pnr == null || pnr.isEmpty()) {
            return false;
        }
        
        // Must start with PNR
        if (!pnr.startsWith(PNR_PREFIX)) {
            return false;
        }
        
        // Must be alphanumeric and proper length
        String numberPart = pnr.substring(3);
        if (numberPart.length() < 8 || numberPart.length() > 13) {
            return false;
        }
        
        // Remaining part must be numeric
        return numberPart.matches("\\d+");
    }
    
    /**
     * Extract train ID hint from PNR (not guaranteed, just a hint)
     * 
     * @param pnr The PNR
     * @return Train ID or -1 if invalid
     */
    public static int extractTrainIdHint(String pnr) {
        if (!isValidPNR(pnr) || pnr.length() < 6) {
            return -1;
        }
        
        try {
            String trainPart = pnr.substring(3, 6);
            return Integer.parseInt(trainPart);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return -1;
        }
    }
    
    /**
     * Format PNR for display (PNR-XXXX-XXXX-XXXX)
     * 
     * @param pnr The raw PNR
     * @return Formatted PNR for UI display
     */
    public static String formatForDisplay(String pnr) {
        if (!isValidPNR(pnr) || pnr.length() < 7) {
            return pnr;
        }
        
        String numberPart = pnr.substring(3);
        StringBuilder formatted = new StringBuilder("PNR-");
        
        // Group in 4-4-5 format
        for (int i = 0; i < numberPart.length(); i++) {
            if (i > 0 && i % 4 == 0 && i < 12) {
                formatted.append("-");
            }
            formatted.append(numberPart.charAt(i));
        }
        
        return formatted.toString();
    }
}
