package com.railway.booking.backend.service;

import com.railway.booking.backend.dao.BookingDAO;
import com.railway.booking.backend.dao.BookingLogDAO;
import com.railway.booking.backend.model.Booking;
import com.railway.booking.backend.model.TrainSeat;
import com.railway.booking.backend.util.DatabaseConnectionPool;
import com.railway.booking.backend.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * PRODUCTION-GRADE Concurrent Booking Service
 * Implements thread-safe booking with:
 * - Row-level database locking (SELECT FOR UPDATE)
 * - Atomic updates with row count verification
 * - Transaction isolation (SERIALIZABLE)
 * - Deadlock prevention ordering
 * - Application-level synchronization
 * 
 * This prevents overbooking under high concurrency.
 */
@Service
public class ConcurrentBookingService {
    private static final Logger logger = LoggerFactory.getLogger(ConcurrentBookingService.class);
    
    // Application-level lock for additional thread safety
    private final ReentrantLock bookingLock = new ReentrantLock(true); // fair lock
    
    // Maximum RAC bookings allowed per train/coach (configurable)
    private static final int MAX_RAC_PER_COACH = 10;
    
    private final BookingDAO bookingDAO;
    private final BookingLogDAO bookingLogDAO;
    private final TrainSeatService trainSeatService;
    private final BookingLifecycleService bookingLifecycleService;
    private final DynamicPricingService dynamicPricingService;
    
    public ConcurrentBookingService(BookingDAO bookingDAO, BookingLogDAO bookingLogDAO, 
                                   TrainSeatService trainSeatService, 
                                   BookingLifecycleService bookingLifecycleService,
                                   DynamicPricingService dynamicPricingService) {
        this.bookingDAO = bookingDAO;
        this.bookingLogDAO = bookingLogDAO;
        this.trainSeatService = trainSeatService;
        this.bookingLifecycleService = bookingLifecycleService;
        this.dynamicPricingService = dynamicPricingService;
    }

    /**
     * THREAD-SAFE BOOKING with 3-layer protection:
     * 1. Application-level lock (ReentrantLock)
     * 2. Database transaction with SERIALIZABLE isolation
     * 3. Row-level locking (SELECT FOR UPDATE) + Atomic update
     * 
     * DEADLOCK PREVENTION ORDER:
     * 1. Lock train_seats row
     * 2. Lock seat_availability row
     * 3. Decrease available_seats (atomic update)
     * 4. Create booking
     * 5. Commit
     * 
     * @param booking Booking details
     * @param coachType Coach type preference
     * @return Booking result: "SUCCESS:bookingId", "WAITING", or "FAILED:message"
     */
    public String bookTicket(Booking booking, TrainSeat.CoachType coachType) {
        // Application-level lock for ordering
        bookingLock.lock();
        
        Connection conn = null;
        String result = "FAILED:Unknown error";
        
        try {
            // Validation
            if (!ValidationUtils.isNotEmpty(booking.getPassengerName())) {
                return "FAILED:Passenger name is required";
            }
            if (!ValidationUtils.isValidAge(booking.getPassengerAge())) {
                return "FAILED:Invalid passenger age";
            }
            if (coachType == null) {
                coachType = TrainSeat.CoachType.GENERAL;
            }
            
            // STEP 1: Get connection with SERIALIZABLE isolation
            conn = DatabaseConnectionPool.getConnectionWithIsolation(
                    Connection.TRANSACTION_SERIALIZABLE);
            
            final int trainId = booking.getTrainId();
            final Date journeyDate = booking.getJourneyDate();
            final String coachTypeStr = coachType.name();
            
            // STEP 2: Lock train_seats row first (DEADLOCK PREVENTION ORDER)
            String lockTrainSql = "SELECT id, total_seats, fare_multiplier FROM train_seats " +
                                  "WHERE train_id = ? AND coach_type = ? FOR UPDATE";
            
            try (PreparedStatement ps = conn.prepareStatement(lockTrainSql)) {
                ps.setInt(1, trainId);
                ps.setString(2, coachTypeStr);
                ResultSet rs = ps.executeQuery();
                
                if (!rs.next()) {
                    conn.rollback();
                    return "FAILED:Coach type not available for this train";
                }
                
                double fareMultiplier = rs.getDouble("fare_multiplier");
                int totalSeats = rs.getInt("total_seats");
                
                // STEP 3: Check and lock seat_availability row
                String lockAvailabilitySql = "SELECT id, available_seats FROM seat_availability " +
                                              "WHERE train_id = ? AND coach_type = ? AND journey_date = ? " +
                                              "FOR UPDATE";
                
                int availabilityId = -1;
                int availableSeats = 0;
                
                try (PreparedStatement ps2 = conn.prepareStatement(lockAvailabilitySql)) {
                    ps2.setInt(1, trainId);
                    ps2.setString(2, coachTypeStr);
                    ps2.setDate(3, journeyDate);
                    ResultSet rs2 = ps2.executeQuery();
                    
                    if (rs2.next()) {
                        availabilityId = rs2.getInt("id");
                        availableSeats = rs2.getInt("available_seats");
                    } else {
                        // Initialize availability if not exists
                        String initSql = "INSERT INTO seat_availability (train_id, coach_type, journey_date, total_seats, available_seats) " +
                                        "VALUES (?, ?, ?, ?, ?)";
                        try (PreparedStatement ps3 = conn.prepareStatement(initSql)) {
                            ps3.setInt(1, trainId);
                            ps3.setString(2, coachTypeStr);
                            ps3.setDate(3, journeyDate);
                            ps3.setInt(4, totalSeats);
                            ps3.setInt(5, totalSeats);
                            ps3.executeUpdate();
                        }
                        availableSeats = totalSeats;
                    }
                }
                
                // STEP 4: Determine initial booking status using lifecycle service
                // Get current RAC count for this train/coach/date
                int racCount = getRACCount(conn, trainId, coachTypeStr, journeyDate);
                
                Booking.BookingStatus bookingStatus = bookingLifecycleService.determineInitialStatus(
                        availableSeats, racCount, MAX_RAC_PER_COACH);
                
                logger.info("Initial status determination: Available={}, RAC={}, MaxRAC={}, Status={}",
                           availableSeats, racCount, MAX_RAC_PER_COACH, bookingStatus);
                
                String seatNumber = null;
                boolean seatAllocated = false;
                
                // STEP 5: Handle seat allocation based on status
                if (bookingStatus == Booking.BookingStatus.CONFIRMED) {
                    // ATOMIC UPDATE - Decrease available_seats with verification
                    String atomicUpdateSql = "UPDATE seat_availability SET available_seats = available_seats - 1, " +
                                            "booked_seats = booked_seats + 1 " +
                                            "WHERE train_id = ? AND coach_type = ? AND journey_date = ? " +
                                            "AND available_seats >= 1";
                    
                    int rowsUpdated;
                    try (PreparedStatement ps4 = conn.prepareStatement(atomicUpdateSql)) {
                        ps4.setInt(1, trainId);
                        ps4.setString(2, coachTypeStr);
                        ps4.setDate(3, journeyDate);
                        rowsUpdated = ps4.executeUpdate();
                    }
                    
                    // CRITICAL: Verify atomic update succeeded
                    if (rowsUpdated == 0) {
                        conn.rollback();
                        logger.warn("Atomic seat update failed - seat taken by another transaction");
                        return "WAITING:Seat just became unavailable";
                    }
                    
                    seatNumber = generateSeatNumber(coachType, availableSeats);
                    seatAllocated = true;
                    
                } else if (bookingStatus == Booking.BookingStatus.RAC) {
                    // RAC: Shared berth - no full seat allocation
                    seatNumber = "RAC-" + (racCount + 1);
                    
                } else if (bookingStatus == Booking.BookingStatus.WAITING) {
                    // WAITING: No seat assigned, waiting list number
                    int waitingCount = getWaitingCount(conn, trainId, coachTypeStr, journeyDate);
                    seatNumber = "WL-" + (waitingCount + 1);
                }
                
                // STEP 6: Generate booking ID, PNR
                String bookingId = generateBookingId();
                String pnr = generateUniquePNR(conn, trainId);
                
                // Assign priority for FIFO ordering
                int priority = bookingLifecycleService.assignPriority();
                
                booking.setBookingId(bookingId);
                booking.setPnr(pnr);
                booking.setSeatNumber(seatNumber);
                booking.setCoachType(coachType);
                booking.setBookingStatus(bookingStatus);
                booking.setPriority(priority);
                
                // STEP 6a: Calculate dynamic fare with surge pricing
                // Formula: price = base_price × fare_multiplier × (1 + surge_factor)
                double baseFare = getTrainBaseFare(conn, trainId);
                int passengerCount = booking.getNumberOfPassengers() > 0 ? booking.getNumberOfPassengers() : 1;
                
                // Calculate occupancy and surge factor
                int totalSeatsInCoach = getTotalSeatsForCoach(conn, trainId, coachTypeStr, journeyDate);
                int bookedSeats = totalSeatsInCoach - availableSeats + (seatAllocated ? 1 : 0); // Include current booking
                double occupancy = totalSeatsInCoach > 0 ? (double) bookedSeats / totalSeatsInCoach : 0;
                double surgeFactor = calculateSurgeFactor(occupancy);
                
                // Calculate final fare
                double totalFare = baseFare * fareMultiplier * (1 + surgeFactor) * passengerCount;
                totalFare = Math.round(totalFare * 100.0) / 100.0; // Round to 2 decimal places
                
                booking.setTotalFare(BigDecimal.valueOf(totalFare));
                logger.info("Dynamic fare calculated: Base={} × Multiplier={} × Surge={:.2f} (Occupancy: {:.1%}) × Passengers={} = Total={}",
                           baseFare, fareMultiplier, surgeFactor, occupancy, passengerCount, totalFare);
                
                // STEP 6: Create booking
                boolean bookingCreated = createBookingWithConnection(conn, booking);
                
                if (!bookingCreated) {
                    // Rollback seat allocation
                    rollbackSeatAllocation(conn, trainId, coachTypeStr, journeyDate);
                    conn.rollback();
                    return "FAILED:Could not create booking record";
                }
                
                // STEP 7: COMMIT transaction
                conn.commit();
                logger.info("Booking {}: BookingID={}, PNR={}, Train: {}, Coach: {}, Seat: {}", 
                           bookingStatus, bookingId, pnr, trainId, coachType, seatNumber);
                
                // AUDIT LOG: Log the initial booking creation
                String logMessage = String.format("Booking created with %s status. Seat: %s, Coach: %s, Fare: ₹%.2f", 
                    bookingStatus, seatNumber, coachType, totalFare);
                bookingLogDAO.logStatusChange(bookingId, null, bookingStatus.name(), logMessage);
                
                result = "SUCCESS:" + bookingId + ":" + pnr + ":" + bookingStatus;
                
            } catch (SQLException e) {
                logger.error("Database error during booking", e);
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        logger.error("Rollback failed", ex);
                    }
                }
                result = "FAILED:Database error - " + e.getMessage();
            }
            
        } catch (SQLException e) {
            logger.error("Failed to obtain database connection", e);
            result = "FAILED:Connection error";
        } finally {
            // Always release application lock
            bookingLock.unlock();
            
            // Always close connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset for pool
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection", e);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Thread-safe cancellation with seat release
     */
    public String cancelTicket(String bookingId) {
        bookingLock.lock();
        
        Connection conn = null;
        String result = "FAILED";
        
        try {
            // Get connection with transaction support
            conn = DatabaseConnectionPool.getTransactionConnection();
            
            // Fetch booking details first
            String fetchSql = "SELECT train_id, coach_type, journey_date, status FROM bookings WHERE booking_id = ? FOR UPDATE";
            
            int trainId = 0;
            String coachTypeStr = null;
            Date journeyDate = null;
            String status = null;
            
            try (PreparedStatement ps = conn.prepareStatement(fetchSql)) {
                ps.setString(1, bookingId);
                ResultSet rs = ps.executeQuery();
                
                if (!rs.next()) {
                    conn.rollback();
                    return "FAILED:Booking not found";
                }
                
                trainId = rs.getInt("train_id");
                coachTypeStr = rs.getString("coach_type");
                journeyDate = rs.getDate("journey_date");
                status = rs.getString("status");
                
                if ("CANCELLED".equals(status)) {
                    conn.rollback();
                    return "FAILED:Booking already cancelled";
                }
            }
            
            // Release seats if booking was confirmed or RAC
            TrainSeat.CoachType coachType = null;
            if (coachTypeStr != null) {
                coachType = TrainSeat.CoachType.valueOf(coachTypeStr);
            }
            
            if (("CONFIRMED".equals(status) || "RAC".equals(status)) && coachTypeStr != null) {
                boolean released = releaseSeatsAtomic(conn, trainId, coachTypeStr, journeyDate, 1);
                if (!released) {
                    logger.warn("Could not release seats for cancelled booking: {}", bookingId);
                }
            }
            
            // Update booking status
            String updateSql = "UPDATE bookings SET status = 'CANCELLED', updated_at = NOW() WHERE booking_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, bookingId);
                int rows = ps.executeUpdate();
                
                if (rows > 0) {
                    conn.commit();
                    logger.info("Booking CANCELLED: {}", bookingId);
                    
                    // AUDIT LOG: Log the cancellation
                    bookingLogDAO.logStatusChange(bookingId, status, "CANCELLED", 
                        "Booking cancelled. Previous status: " + status + ". Seats released.");
                    
                    result = "SUCCESS";
                    
                    // TRIGGER PROMOTION: After cancellation, promote waiting/RAC bookings
                    if (coachType != null && ("CONFIRMED".equals(status) || "RAC".equals(status))) {
                        // Close current connection before promotion (different transaction)
                        conn.setAutoCommit(true);
                        conn.close();
                        conn = null;
                        
                        // Trigger promotion in background
                        logger.info("Triggering promotion for train: {}, date: {}, coach: {}", 
                                   trainId, journeyDate, coachType);
                        int promotions = bookingLifecycleService.handlePromotion(trainId, journeyDate, coachType);
                        logger.info("Promotion triggered: {} bookings promoted", promotions);
                    }
                } else {
                    conn.rollback();
                    result = "FAILED:Cancellation failed";
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error cancelling booking", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Rollback failed", ex);
                }
            }
            result = "FAILED:" + e.getMessage();
        } finally {
            bookingLock.unlock();
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection", e);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Create booking using provided connection (within transaction)
     */
    private boolean createBookingWithConnection(Connection conn, Booking booking) throws SQLException {
        String sql = "INSERT INTO bookings (booking_id, pnr, user_id, train_id, route_id, journey_date, " +
                      "passenger_name, passenger_age, passenger_gender, seat_number, coach_type, " +
                      "status, total_fare, number_of_passengers, priority) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, booking.getBookingId());
            ps.setString(2, booking.getPnr());
            ps.setInt(3, booking.getUserId());
            ps.setInt(4, booking.getTrainId());
            ps.setInt(5, booking.getRouteId());
            ps.setDate(6, booking.getJourneyDate());
            ps.setString(7, booking.getPassengerName());
            ps.setInt(8, booking.getPassengerAge());
            ps.setString(9, booking.getPassengerGender().name());
            ps.setString(10, booking.getSeatNumber());
            ps.setString(11, booking.getCoachType().name());
            ps.setString(12, booking.getBookingStatus().name());
            ps.setBigDecimal(13, booking.getTotalFare());
            ps.setInt(14, booking.getNumberOfPassengers());
            ps.setInt(15, booking.getPriority());
            
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Atomic rollback of seat allocation
     */
    private void rollbackSeatAllocation(Connection conn, int trainId, String coachType, Date journeyDate) {
        try {
            String sql = "UPDATE seat_availability SET available_seats = available_seats + 1, " +
                        "booked_seats = booked_seats - 1 " +
                        "WHERE train_id = ? AND coach_type = ? AND journey_date = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, trainId);
                ps.setString(2, coachType);
                ps.setDate(3, journeyDate);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error("Failed to rollback seat allocation", e);
        }
    }
    
    /**
     * Atomic seat release
     */
    private boolean releaseSeatsAtomic(Connection conn, int trainId, String coachType, Date journeyDate, int seats) {
        try {
            String sql = "UPDATE seat_availability SET available_seats = available_seats + ?, " +
                        "booked_seats = booked_seats - ? " +
                        "WHERE train_id = ? AND coach_type = ? AND journey_date = ? " +
                        "AND booked_seats >= ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, seats);
                ps.setInt(2, seats);
                ps.setInt(3, trainId);
                ps.setString(4, coachType);
                ps.setDate(5, journeyDate);
                ps.setInt(6, seats);
                
                int rows = ps.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            logger.error("Failed to release seats", e);
            return false;
        }
    }
    
    /**
     * Get count of RAC bookings for a train on a specific date
     */
    private int getRACCount(Connection conn, int trainId, String coachType, Date journeyDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE train_id = ? AND coach_type = ? AND journey_date = ? AND status = 'RAC'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainId);
            ps.setString(2, coachType);
            ps.setDate(3, journeyDate);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    /**
     * Get count of WAITING bookings for a train on a specific date
     */
    private int getWaitingCount(Connection conn, int trainId, String coachType, Date journeyDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE train_id = ? AND coach_type = ? AND journey_date = ? AND status = 'WAITING'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainId);
            ps.setString(2, coachType);
            ps.setDate(3, journeyDate);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    /**
     * Get train's base fare from trains table
     */
    private double getTrainBaseFare(Connection conn, int trainId) throws SQLException {
        String sql = "SELECT fare_per_seat FROM trains WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("fare_per_seat");
            }
        }
        logger.warn("Could not find base fare for train {}, using default 100.0", trainId);
        return 100.0; // Default base fare if train not found
    }
    
    /**
     * Get total seats for a specific coach on a journey date
     */
    private int getTotalSeatsForCoach(Connection conn, int trainId, String coachType, Date journeyDate) throws SQLException {
        String sql = "SELECT total_seats FROM seat_availability WHERE train_id = ? AND coach_type = ? AND journey_date = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainId);
            ps.setString(2, coachType);
            ps.setDate(3, journeyDate);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_seats");
            }
        }
        // Fallback to train_seats table
        String fallbackSql = "SELECT total_seats FROM train_seats WHERE train_id = ? AND coach_type = ?";
        try (PreparedStatement ps = conn.prepareStatement(fallbackSql)) {
            ps.setInt(1, trainId);
            ps.setString(2, coachType);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_seats");
            }
        }
        logger.warn("Could not find total seats for train={}, coach={}", trainId, coachType);
        return 100; // Default
    }
    
    /**
     * Calculate surge factor based on occupancy
     * Formula: surge = occupancy (capped at MAX_SURGE = 0.8)
     * 
     * Examples:
     * - 10% occupancy → 0.1 surge → 1.1x price
     * - 50% occupancy → 0.5 surge → 1.5x price  
     * - 90% occupancy → 0.8 surge (capped) → 1.8x price
     */
    private double calculateSurgeFactor(double occupancy) {
        final double MAX_SURGE = 0.8; // Cap at 80% to avoid extreme pricing
        final double MIN_SURGE = 0.0;
        
        double surge = Math.max(MIN_SURGE, Math.min(occupancy, MAX_SURGE));
        logger.debug("Calculated surge factor: {:.2f} (occupancy: {:.1%})", surge, occupancy);
        return surge;
    }
    
    /**
     * Generate unique booking ID
     */
    private String generateBookingId() {
        return "RB" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
    
    /**
     * Generate unique PNR with collision check
     * Industry-grade implementation for concurrency safety
     */
    private String generateUniquePNR(Connection conn, int trainId) throws SQLException {
        String pnr;
        int attempts = 0;
        final int MAX_ATTEMPTS = 5;

        do {
            pnr = com.railway.booking.backend.util.PNRGenerator.generatePNR(trainId);
            
            // Check if PNR exists within transaction
            String checkSql = "SELECT COUNT(*) FROM bookings WHERE pnr = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, pnr);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    // PNR is unique
                    break;
                }
            }
            attempts++;
        } while (attempts < MAX_ATTEMPTS);

        if (attempts >= MAX_ATTEMPTS) {
            logger.error("Failed to generate unique PNR after {} attempts", MAX_ATTEMPTS);
            // Fallback with user component for extra uniqueness
            pnr = com.railway.booking.backend.util.PNRGenerator.generatePNRWithUser(
                    trainId, (int) (System.currentTimeMillis() % 10000));
        }

        return pnr;
    }
    
    /**
     * Generate seat number with coach type prefix
     * Format: [CoachPrefix]-[SeatNumber]
     */
    private String generateSeatNumber(TrainSeat.CoachType coachType, int availableSeats) {
        String prefix;
        switch (coachType) {
            case GENERAL: prefix = "GEN"; break;
            case SL: prefix = "SL"; break;
            case SL_AC: prefix = "3A"; break;
            case AC_3TIER: prefix = "3AC"; break;
            case AC_2TIER: prefix = "2AC"; break;
            case AC_1TIER: prefix = "1AC"; break;
            case AC_CHAIR_CAR: prefix = "CC"; break;
            case EXECUTIVE_CHAIR_CAR: prefix = "EC"; break;
            case SLEEPER: prefix = "SL"; break;  // Legacy fallback
            case AC: prefix = "AC"; break;       // Legacy fallback
            default: prefix = "GEN";
        }
        return String.format("%s-%03d", prefix, availableSeats);
    }
    
    /**
     * Track booking by PNR
     * @param pnr The PNR to search
     * @return Booking object or null if not found
     */
    public Booking trackBooking(String pnr) {
        return bookingDAO.getBookingByPNR(pnr);
    }
}
