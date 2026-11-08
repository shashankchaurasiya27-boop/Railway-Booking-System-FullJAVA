package com.railway.booking.backend.service;

import com.railway.booking.backend.model.TrainSeat;
import com.railway.booking.backend.util.DatabaseConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

/**
 * Dynamic Pricing (Surge Pricing) Service
 * Implements real-time fare calculation based on seat occupancy
 * 
 * Formula: price = base_price × fare_multiplier × (1 + surge_factor)
 * where surge_factor = occupancy (capped at MAX_SURGE = 0.8)
 * 
 * Example pricing:
 * - 10% occupancy → 1.1x surge → ₹550 (base ₹500)
 * - 50% occupancy → 1.5x surge → ₹750
 * - 90% occupancy → 1.8x surge (capped) → ₹900
 */
@Service
public class DynamicPricingService {
    
    private static final Logger logger = LoggerFactory.getLogger(DynamicPricingService.class);
    
    // Maximum surge factor (80% to avoid extreme pricing)
    private static final double MAX_SURGE = 0.8;
    
    // Minimum surge factor (always at least 1.0x base price)
    private static final double MIN_SURGE = 0.0;
    
    /**
     * Calculate dynamic fare with surge pricing
     * 
     * @param trainId Train ID
     * @param coachType Coach type (SL, AC_3TIER, etc.)
     * @param journeyDate Journey date
     * @param passengerCount Number of passengers
     * @return Calculated dynamic fare
     */
    public double calculateDynamicFare(int trainId, TrainSeat.CoachType coachType, 
                                       Date journeyDate, int passengerCount) {
        try {
            double baseFare = getTrainBaseFare(trainId);
            double multiplier = coachType.getDefaultFareMultiplier();
            
            // Get seat statistics
            SeatStats stats = getSeatStats(trainId, coachType.name(), journeyDate);
            
            double occupancy = stats.getOccupancy();
            double surgeFactor = calculateSurgeFactor(occupancy);
            
            double finalFare = baseFare * multiplier * (1 + surgeFactor) * passengerCount;
            
            logger.info("Dynamic fare calculated: Train={}, Coach={}, Occupancy={:.1%}, " +
                       "Surge={:.2f}, Base={}×{}×{:.2f}×{} = ₹{:.2f}",
                       trainId, coachType, occupancy, surgeFactor, 
                       baseFare, multiplier, (1 + surgeFactor), passengerCount, finalFare);
            
            return Math.round(finalFare * 100.0) / 100.0; // Round to 2 decimal places
            
        } catch (SQLException e) {
            logger.error("Error calculating dynamic fare", e);
            // Fallback to base fare calculation
            return calculateFallbackFare(trainId, coachType, passengerCount);
        }
    }
    
    /**
     * Calculate fare without database lookup (for UI preview)
     * Uses estimated occupancy based on available seats
     */
    public double calculateEstimatedFare(double baseFare, TrainSeat.CoachType coachType, 
                                          int availableSeats, int totalSeats, int passengerCount) {
        double multiplier = coachType.getDefaultFareMultiplier();
        double occupancy = totalSeats > 0 ? (double) (totalSeats - availableSeats) / totalSeats : 0;
        double surgeFactor = calculateSurgeFactor(occupancy);
        
        double finalFare = baseFare * multiplier * (1 + surgeFactor) * passengerCount;
        
        logger.debug("Estimated fare: Base={}×{}×{:.2f}×{} = ₹{:.2f} (Occupancy: {:.1%})",
                    baseFare, multiplier, (1 + surgeFactor), passengerCount, finalFare, occupancy);
        
        return Math.round(finalFare * 100.0) / 100.0;
    }
    
    /**
     * Get seat statistics for a specific train/coach/date
     */
    public SeatStats getSeatStats(int trainId, String coachType, Date journeyDate) throws SQLException {
        String sql = "SELECT total_seats, available_seats, booked_seats FROM seat_availability " +
                    "WHERE train_id = ? AND coach_type = ? AND journey_date = ?";
        
        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, trainId);
            ps.setString(2, coachType);
            ps.setDate(3, journeyDate);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int totalSeats = rs.getInt("total_seats");
                int availableSeats = rs.getInt("available_seats");
                int bookedSeats = rs.getInt("booked_seats");
                
                return new SeatStats(totalSeats, availableSeats, bookedSeats);
            }
            
            // If no record found, return default stats
            logger.warn("No seat availability record found for train={}, coach={}, date={}",
                       trainId, coachType, journeyDate);
            return new SeatStats(0, 0, 0);
        }
    }
    
    /**
     * Calculate surge factor based on occupancy
     * Formula: surge = occupancy (capped at MAX_SURGE)
     */
    private double calculateSurgeFactor(double occupancy) {
        double surge = Math.max(MIN_SURGE, Math.min(occupancy, MAX_SURGE));
        return surge;
    }
    
    /**
     * Get train's base fare from trains table
     */
    private double getTrainBaseFare(int trainId) throws SQLException {
        String sql = "SELECT fare_per_seat FROM trains WHERE id = ?";
        
        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, trainId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("fare_per_seat");
            }
        }
        
        logger.warn("Could not find base fare for train {}, using default 100.0", trainId);
        return 100.0; // Default base fare
    }
    
    /**
     * Fallback fare calculation when database lookup fails
     */
    private double calculateFallbackFare(int trainId, TrainSeat.CoachType coachType, int passengerCount) {
        double baseFare = 100.0;
        double multiplier = coachType.getDefaultFareMultiplier();
        return baseFare * multiplier * passengerCount;
    }
    
    /**
     * Get pricing breakdown for display
     */
    public PricingBreakdown getPricingBreakdown(int trainId, TrainSeat.CoachType coachType, 
                                               Date journeyDate, int passengerCount) {
        try {
            double baseFare = getTrainBaseFare(trainId);
            double multiplier = coachType.getDefaultFareMultiplier();
            SeatStats stats = getSeatStats(trainId, coachType.name(), journeyDate);
            
            double occupancy = stats.getOccupancy();
            double surgeFactor = calculateSurgeFactor(occupancy);
            double coachFare = baseFare * multiplier;
            double surgeAmount = coachFare * surgeFactor;
            double totalPerPassenger = coachFare + surgeAmount;
            double totalFare = totalPerPassenger * passengerCount;
            
            return new PricingBreakdown(
                baseFare,
                multiplier,
                coachType.getShortCode(),
                coachFare,
                occupancy,
                surgeFactor,
                surgeAmount,
                totalPerPassenger,
                passengerCount,
                totalFare
            );
            
        } catch (SQLException e) {
            logger.error("Error getting pricing breakdown", e);
            return null;
        }
    }
    
    /**
     * Seat statistics inner class
     */
    public static class SeatStats {
        private final int totalSeats;
        private final int availableSeats;
        private final int bookedSeats;
        
        public SeatStats(int totalSeats, int availableSeats, int bookedSeats) {
            this.totalSeats = totalSeats;
            this.availableSeats = availableSeats;
            this.bookedSeats = bookedSeats;
        }
        
        public int getTotalSeats() { return totalSeats; }
        public int getAvailableSeats() { return availableSeats; }
        public int getBookedSeats() { return bookedSeats; }
        
        public double getOccupancy() {
            return totalSeats > 0 ? (double) bookedSeats / totalSeats : 0;
        }
        
        public double getAvailabilityPercentage() {
            return totalSeats > 0 ? (double) availableSeats / totalSeats : 0;
        }
    }
    
    /**
     * Pricing breakdown for UI display
     */
    public static class PricingBreakdown {
        private final double baseFare;
        private final double multiplier;
        private final String coachCode;
        private final double coachFare;
        private final double occupancy;
        private final double surgeFactor;
        private final double surgeAmount;
        private final double perPassengerFare;
        private final int passengerCount;
        private final double totalFare;
        
        public PricingBreakdown(double baseFare, double multiplier, String coachCode,
                               double coachFare, double occupancy, double surgeFactor,
                               double surgeAmount, double perPassengerFare, 
                               int passengerCount, double totalFare) {
            this.baseFare = baseFare;
            this.multiplier = multiplier;
            this.coachCode = coachCode;
            this.coachFare = coachFare;
            this.occupancy = occupancy;
            this.surgeFactor = surgeFactor;
            this.surgeAmount = surgeAmount;
            this.perPassengerFare = perPassengerFare;
            this.passengerCount = passengerCount;
            this.totalFare = totalFare;
        }
        
        // Getters
        public double getBaseFare() { return baseFare; }
        public double getMultiplier() { return multiplier; }
        public String getCoachCode() { return coachCode; }
        public double getCoachFare() { return coachFare; }
        public double getOccupancy() { return occupancy; }
        public double getSurgeFactor() { return surgeFactor; }
        public double getSurgeAmount() { return surgeAmount; }
        public double getPerPassengerFare() { return perPassengerFare; }
        public int getPassengerCount() { return passengerCount; }
        public double getTotalFare() { return totalFare; }
        
        public String getFormattedFare() {
            return String.format("₹%.2f", totalFare);
        }
        
        public String getSurgeDescription() {
            if (surgeFactor <= 0.1) return "Low demand - Standard price";
            if (surgeFactor <= 0.4) return "Moderate demand - Slight increase";
            if (surgeFactor <= 0.6) return "High demand - Price surge";
            return "Very high demand - Max surge (capped)";
        }
    }
}
