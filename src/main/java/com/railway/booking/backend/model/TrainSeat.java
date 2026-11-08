package com.railway.booking.backend.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * TrainSeat model - defines seat configuration for each coach type
 * Seat types: SLEEPER, AC, GENERAL
 */
public class TrainSeat {
    private int trainSeatId;
    private int trainId;
    private CoachType coachType;
    private int totalSeats;
    private BigDecimal fareMultiplier;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public enum CoachType {
        // General/Unreserved
        GENERAL("General", "GEN", 0.8),

        // Sleeper classes
        SL("Sleeper", "SL", 1.0),
        SL_AC("AC Sleeper", "3A", 1.8),

        // AC Tiers
        AC_3TIER("3AC - 3 Tier", "3AC", 1.5),
        AC_2TIER("2AC - 2 Tier", "2AC", 2.0),
        AC_1TIER("1AC - 1 Tier", "1AC", 2.5),
        AC_CHAIR_CAR("AC Chair Car", "CC", 1.2),
        EXECUTIVE_CHAIR_CAR("Executive Chair Car", "EC", 2.2),

        // Legacy mappings (for backward compatibility)
        SLEEPER("Sleeper (Legacy)", "SL", 1.0),
        AC("AC (Legacy)", "AC", 2.0);

        private final String displayName;
        private final String shortCode;
        private final double fareMultiplier;

        CoachType(String displayName, String shortCode, double fareMultiplier) {
            this.displayName = displayName;
            this.shortCode = shortCode;
            this.fareMultiplier = fareMultiplier;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getShortCode() {
            return shortCode;
        }

        public double getDefaultFareMultiplier() {
            return fareMultiplier;
        }

        /**
         * Get coach type from display string or short code
         */
        public static CoachType fromString(String value) {
            if (value == null) return GENERAL;
            String upper = value.toUpperCase().trim();

            // Check by enum name
            try {
                return CoachType.valueOf(upper);
            } catch (IllegalArgumentException e) {
                // Continue to check by display name or short code
            }

            // Check by short code or display name
            for (CoachType type : values()) {
                if (type.shortCode.equals(upper) ||
                    type.displayName.toUpperCase().contains(upper) ||
                    upper.contains(type.shortCode)) {
                    return type;
                }
            }

            // Legacy fallbacks
            if (upper.contains("SLEEP") || upper.equals("SL")) return SL;
            if (upper.contains("1AC") || upper.contains("1ST") || upper.contains("FIRST")) return AC_1TIER;
            if (upper.contains("2AC") || upper.contains("2ND") || upper.contains("SECOND")) return AC_2TIER;
            if (upper.contains("3AC") || upper.contains("3RD") || upper.contains("THIRD")) return AC_3TIER;
            if (upper.contains("AC")) return AC_3TIER;
            if (upper.contains("GEN")) return GENERAL;

            return GENERAL;
        }
    }

    // Constructors
    public TrainSeat() {
    }

    public TrainSeat(int trainId, CoachType coachType, int totalSeats, BigDecimal fareMultiplier) {
        this.trainId = trainId;
        this.coachType = coachType;
        this.totalSeats = totalSeats;
        this.fareMultiplier = fareMultiplier;
    }

    // Getters and Setters
    public int getTrainSeatId() {
        return trainSeatId;
    }

    public void setTrainSeatId(int trainSeatId) {
        this.trainSeatId = trainSeatId;
    }

    public int getTrainId() {
        return trainId;
    }

    public void setTrainId(int trainId) {
        this.trainId = trainId;
    }

    public CoachType getCoachType() {
        return coachType;
    }

    public void setCoachType(CoachType coachType) {
        this.coachType = coachType;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public BigDecimal getFareMultiplier() {
        return fareMultiplier;
    }

    public void setFareMultiplier(BigDecimal fareMultiplier) {
        this.fareMultiplier = fareMultiplier;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "TrainSeat{" +
                "trainSeatId=" + trainSeatId +
                ", trainId=" + trainId +
                ", coachType=" + coachType +
                ", totalSeats=" + totalSeats +
                ", fareMultiplier=" + fareMultiplier +
                '}';
    }
}
