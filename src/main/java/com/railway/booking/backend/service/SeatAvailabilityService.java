package com.railway.booking.backend.service;

import com.railway.booking.backend.dao.SeatAvailabilityDAO;
import com.railway.booking.backend.dao.TrainSeatDAO;
import com.railway.booking.backend.model.SeatAvailability;
import com.railway.booking.backend.model.TrainSeat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;

/**
 * Seat Availability Service - Real-time seat tracking and allocation
 * CORE ENGINE for seat management across different coach types
 */
@Service
public class SeatAvailabilityService {
    private static final Logger logger = LoggerFactory.getLogger(SeatAvailabilityService.class);
    
    private final SeatAvailabilityDAO seatAvailabilityDAO;
    private final TrainSeatDAO trainSeatDAO;

    public SeatAvailabilityService(SeatAvailabilityDAO seatAvailabilityDAO, TrainSeatDAO trainSeatDAO) {
        this.seatAvailabilityDAO = seatAvailabilityDAO;
        this.trainSeatDAO = trainSeatDAO;
    }

    /**
     * Check and allocate seats for booking
     * Returns true if seats allocated successfully
     */
    @Transactional
    public boolean allocateSeats(int trainId, TrainSeat.CoachType coachType, Date journeyDate, int seatsNeeded) {
        logger.info("Allocating {} seats for train: {}, coach: {}, date: {}", 
                    seatsNeeded, trainId, coachType, journeyDate);
        
        // First check if availability record exists, if not initialize it
        if (!initializeAvailabilityIfNeeded(trainId, coachType, journeyDate)) {
            logger.error("Failed to initialize availability");
            return false;
        }
        
        // Try to book seats
        boolean success = seatAvailabilityDAO.bookSeats(trainId, coachType, journeyDate, seatsNeeded);
        
        if (success) {
            logger.info("Successfully allocated {} seats", seatsNeeded);
        } else {
            logger.warn("Failed to allocate {} seats - not available", seatsNeeded);
        }
        
        return success;
    }

    /**
     * Release seats when booking is cancelled
     * Returns true if seats released successfully
     */
    @Transactional
    public boolean releaseSeats(int trainId, TrainSeat.CoachType coachType, Date journeyDate, int seatsToRelease) {
        logger.info("Releasing {} seats for train: {}, coach: {}, date: {}", 
                    seatsToRelease, trainId, coachType, journeyDate);
        
        boolean success = seatAvailabilityDAO.releaseSeats(trainId, coachType, journeyDate, seatsToRelease);
        
        if (success) {
            logger.info("Successfully released {} seats", seatsToRelease);
        } else {
            logger.warn("Failed to release {} seats", seatsToRelease);
        }
        
        return success;
    }

    /**
     * Check if seats are available (read-only check)
     */
    public boolean isAvailable(int trainId, TrainSeat.CoachType coachType, Date journeyDate, int requestedSeats) {
        // Check if availability exists, if not we need to initialize
        SeatAvailability availability = seatAvailabilityDAO.getAvailability(trainId, coachType, journeyDate);
        
        if (availability == null) {
            // Check train seat configuration exists
            TrainSeat config = trainSeatDAO.getSeatByTrainAndType(trainId, coachType);
            if (config == null) {
                logger.warn("No seat configuration for train: {}, coach: {}", trainId, coachType);
                return false;
            }
            // All seats are available (not initialized yet)
            return config.getTotalSeats() >= requestedSeats;
        }
        
        return availability.isAvailable(requestedSeats);
    }

    /**
     * Get available seats count for a specific coach type
     */
    public int getAvailableSeats(int trainId, TrainSeat.CoachType coachType, Date journeyDate) {
        int available = seatAvailabilityDAO.getAvailableSeats(trainId, coachType, journeyDate);
        
        if (available == 0) {
            // Check if we need to initialize
            TrainSeat config = trainSeatDAO.getSeatByTrainAndType(trainId, coachType);
            if (config != null && seatAvailabilityDAO.getAvailability(trainId, coachType, journeyDate) == null) {
                // Not initialized yet, all seats available
                return config.getTotalSeats();
            }
        }
        
        return available;
    }

    /**
     * Get availability for all coach types on a specific train and date
     */
    public List<SeatAvailability> getAvailabilityForTrain(int trainId, Date journeyDate) {
        // First ensure all coach types are initialized
        List<TrainSeat> configs = trainSeatDAO.getSeatsByTrainId(trainId);
        for (TrainSeat config : configs) {
            initializeAvailabilityIfNeeded(trainId, config.getCoachType(), journeyDate);
        }
        
        return seatAvailabilityDAO.getAvailabilityByTrainAndDate(trainId, journeyDate);
    }

    /**
     * Get availability for all coach types across all trains
     */
    public List<SeatAvailability> getAllAvailability() {
        return seatAvailabilityDAO.getAllAvailability();
    }

    /**
     * Get fare multiplier for calculating ticket price
     */
    public double getFareMultiplier(int trainId, TrainSeat.CoachType coachType) {
        return trainSeatDAO.getFareMultiplier(trainId, coachType);
    }

    /**
     * Calculate total fare including coach type multiplier
     */
    public double calculateFare(double baseFare, int trainId, TrainSeat.CoachType coachType, int numberOfSeats) {
        double multiplier = getFareMultiplier(trainId, coachType);
        return baseFare * multiplier * numberOfSeats;
    }

    /**
     * Get seat configuration for a train
     */
    public List<TrainSeat> getTrainSeatConfig(int trainId) {
        return trainSeatDAO.getSeatsByTrainId(trainId);
    }

    /**
     * Initialize availability record if it doesn't exist
     */
    @Transactional
    public boolean initializeAvailabilityIfNeeded(int trainId, TrainSeat.CoachType coachType, Date journeyDate) {
        SeatAvailability existing = seatAvailabilityDAO.getAvailability(trainId, coachType, journeyDate);
        if (existing != null) {
            return true; // Already initialized
        }
        
        // Get configuration
        TrainSeat config = trainSeatDAO.getSeatByTrainAndType(trainId, coachType);
        if (config == null) {
            logger.error("No seat configuration for train: {}, coach: {}", trainId, coachType);
            return false;
        }
        
        // Initialize availability
        boolean success = seatAvailabilityDAO.initializeAvailability(
                trainId, coachType, journeyDate, config.getTotalSeats());
        
        if (success) {
            logger.info("Initialized availability for train: {}, coach: {}, date: {} with {} seats", 
                       trainId, coachType, journeyDate, config.getTotalSeats());
        }
        
        return success;
    }

    /**
     * Initialize availability for all coach types of a train
     */
    @Transactional
    public void initializeAllCoachTypes(int trainId, Date journeyDate) {
        List<TrainSeat> configs = trainSeatDAO.getSeatsByTrainId(trainId);
        for (TrainSeat config : configs) {
            initializeAvailabilityIfNeeded(trainId, config.getCoachType(), journeyDate);
        }
    }
}
