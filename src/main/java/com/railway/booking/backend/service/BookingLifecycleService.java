package com.railway.booking.backend.service;

import com.railway.booking.backend.dao.BookingDAO;
import com.railway.booking.backend.dao.BookingLogDAO;
import com.railway.booking.backend.model.Booking;
import com.railway.booking.backend.model.TrainSeat;
import com.railway.booking.backend.util.DatabaseConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * BOOKING LIFECYCLE SERVICE
 * 
 * Manages complete booking state machine with promotion rules:
 * - PENDING → CONFIRMED / RAC / WAITING
 * - RAC → CONFIRMED (when seats free up)
 * - WAITING → RAC → CONFIRMED (chain promotion)
 * - CANCELLED (terminal state)
 * 
 * Production-grade features:
 * - Concurrency-safe promotions with row-level locking
 * - FIFO ordering with priority
 * - Automatic promotion chain on cancellations
 * - Transaction-safe state transitions
 */
@Service
public class BookingLifecycleService {
    private static final Logger logger = LoggerFactory.getLogger(BookingLifecycleService.class);
    
    // Application-level lock for promotion operations
    private final ReentrantLock promotionLock = new ReentrantLock(true);
    
    private final BookingDAO bookingDAO;
    private final BookingLogDAO bookingLogDAO;
    private final SeatAvailabilityService seatAvailabilityService;
    
    public BookingLifecycleService(BookingDAO bookingDAO, BookingLogDAO bookingLogDAO, 
                                   SeatAvailabilityService seatAvailabilityService) {
        this.bookingDAO = bookingDAO;
        this.bookingLogDAO = bookingLogDAO;
        this.seatAvailabilityService = seatAvailabilityService;
    }

    /**
     * CENTRAL PROMOTION ENGINE
     * 
     * Handles promotion chain when seats become available:
     * 1. RAC → CONFIRMED (if seats available)
     * 2. WAITING → RAC
     * 
     * Should be called after every cancellation to fill freed seats.
     * 
     * @param trainId Train ID
     * @param journeyDate Journey date
     * @param coachType Coach type
     * @return Number of promotions executed
     */
    public int handlePromotion(int trainId, Date journeyDate, TrainSeat.CoachType coachType) {
        promotionLock.lock();
        Connection conn = null;
        int promotionsExecuted = 0;
        
        try {
            conn = DatabaseConnectionPool.getConnectionWithIsolation(
                    Connection.TRANSACTION_SERIALIZABLE);
            
            String coachTypeStr = coachType.name();
            
            // STEP 1: Check available seats in transaction
            int availableSeats = getAvailableSeats(conn, trainId, coachTypeStr, journeyDate);
            logger.info("Handling promotion for Train: {}, Date: {}, Coach: {}, Available: {}", 
                       trainId, journeyDate, coachType, availableSeats);
            
            // Keep promoting while seats are available
            while (availableSeats > 0) {
                boolean promoted = false;
                
                // STEP 2: Try to promote RAC → CONFIRMED
                Booking racBooking = getNextRACForPromotion(conn, trainId, journeyDate, coachTypeStr);
                
                if (racBooking != null) {
                    logger.info("Promoting RAC booking {} to CONFIRMED", racBooking.getBookingId());
                    
                    // Assign seat and confirm
                    boolean confirmed = promoteRACToConfirmed(conn, racBooking);
                    if (confirmed) {
                        promotionsExecuted++;
                        availableSeats--;
                        promoted = true;
                        
                        // Log promotion
                        logger.info("RAC booking {} promoted to CONFIRMED with seat {}", 
                                   racBooking.getBookingId(), racBooking.getSeatNumber());
                        
                        // AUDIT LOG: RAC → CONFIRMED
                        bookingLogDAO.logStatusChange(conn, racBooking.getBookingId(), 
                            "RAC", "CONFIRMED", 
                            "Promoted from RAC to CONFIRMED. Seat assigned due to cancellation.");
                    }
                }
                
                // STEP 3: If no RAC, try WAITING → RAC (or direct to CONFIRMED if seats)
                if (!promoted) {
                    Booking waitingBooking = getNextWaitingForPromotion(conn, trainId, journeyDate, coachTypeStr);
                    
                    if (waitingBooking != null) {
                        if (availableSeats > 0) {
                            // Direct promotion to CONFIRMED
                            logger.info("Promoting WAITING booking {} directly to CONFIRMED", 
                                       waitingBooking.getBookingId());
                            
                            boolean confirmed = promoteWaitingToConfirmed(conn, waitingBooking);
                            if (confirmed) {
                                promotionsExecuted++;
                                availableSeats--;
                                promoted = true;
                                
                                // AUDIT LOG: WAITING → CONFIRMED
                                bookingLogDAO.logStatusChange(conn, waitingBooking.getBookingId(), 
                                    "WAITING", "CONFIRMED", 
                                    "Promoted directly from WAITING to CONFIRMED. Seat available.");
                            }
                        } else {
                            // No seats, promote to RAC
                            logger.info("Promoting WAITING booking {} to RAC", 
                                       waitingBooking.getBookingId());
                            
                            boolean racAssigned = promoteWaitingToRAC(conn, waitingBooking);
                            if (racAssigned) {
                                promotionsExecuted++;
                                promoted = true;
                                
                                // AUDIT LOG: WAITING → RAC
                                bookingLogDAO.logStatusChange(conn, waitingBooking.getBookingId(), 
                                    "WAITING", "RAC", 
                                    "Promoted from WAITING to RAC. Shared berth assigned.");
                            }
                        }
                    }
                }
                
                // If no promotions possible, break
                if (!promoted) {
                    break;
                }
                
                // Refresh available seats count
                availableSeats = getAvailableSeats(conn, trainId, coachTypeStr, journeyDate);
            }
            
            // Commit all promotions
            conn.commit();
            logger.info("Promotion complete. Total promotions: {}", promotionsExecuted);
            
        } catch (SQLException e) {
            logger.error("Error during promotion handling", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Rollback failed", ex);
                }
            }
        } finally {
            promotionLock.unlock();
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection", e);
                }
            }
        }
        
        return promotionsExecuted;
    }
    
    /**
     * Determine initial booking status based on availability
     * 
     * @param availableSeats Available seats
     * @param racCount Current RAC count (optional)
     * @param maxRAC Maximum RAC allowed
     * @return Appropriate booking status
     */
    public Booking.BookingStatus determineInitialStatus(int availableSeats, int racCount, int maxRAC) {
        if (availableSeats > 0) {
            return Booking.BookingStatus.CONFIRMED;
        } else if (racCount < maxRAC) {
            return Booking.BookingStatus.RAC;
        } else {
            return Booking.BookingStatus.WAITING;
        }
    }
    
    /**
     * Assign priority for FIFO ordering
     * Uses timestamp-based priority for fair ordering
     * 
     * @return Priority value (lower = higher priority)
     */
    public int assignPriority() {
        // Use current time in millis (chronological ordering)
        return (int) (System.currentTimeMillis() / 1000);
    }
    
    /**
     * Get available seats count within transaction
     */
    private int getAvailableSeats(Connection conn, int trainId, String coachType, Date journeyDate) 
            throws SQLException {
        String sql = "SELECT available_seats FROM seat_availability " +
                    "WHERE train_id = ? AND coach_type = ? AND journey_date = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainId);
            ps.setString(2, coachType);
            ps.setDate(3, journeyDate);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("available_seats");
            }
        }
        return 0;
    }
    
    /**
     * Get next RAC booking for promotion with row lock
     */
    private Booking getNextRACForPromotion(Connection conn, int trainId, Date journeyDate, String coachType) 
            throws SQLException {
        String sql = "SELECT b.*, t.train_name, t.train_number, u.username, " +
                    "CONCAT(s1.station_name, ' to ', s2.station_name) as route_description " +
                    "FROM bookings b " +
                    "JOIN trains t ON b.train_id = t.id " +
                    "JOIN routes r ON b.route_id = r.id " +
                    "JOIN stations s1 ON r.source_station_id = s1.id " +
                    "JOIN stations s2 ON r.destination_station_id = s2.id " +
                    "JOIN users u ON b.user_id = u.id " +
                    "WHERE b.train_id = ? AND b.journey_date = ? AND b.status = 'RAC' " +
                    "AND b.coach_type = ? " +
                    "ORDER BY b.priority ASC, b.created_at ASC " +
                    "LIMIT 1 FOR UPDATE";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainId);
            ps.setDate(2, journeyDate);
            ps.setString(3, coachType);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapBookingFromResultSet(rs);
            }
        }
        return null;
    }
    
    /**
     * Get next WAITING booking for promotion with row lock
     */
    private Booking getNextWaitingForPromotion(Connection conn, int trainId, Date journeyDate, String coachType) 
            throws SQLException {
        String sql = "SELECT b.*, t.train_name, t.train_number, u.username, " +
                    "CONCAT(s1.station_name, ' to ', s2.station_name) as route_description " +
                    "FROM bookings b " +
                    "JOIN trains t ON b.train_id = t.id " +
                    "JOIN routes r ON b.route_id = r.id " +
                    "JOIN stations s1 ON r.source_station_id = s1.id " +
                    "JOIN stations s2 ON r.destination_station_id = s2.id " +
                    "JOIN users u ON b.user_id = u.id " +
                    "WHERE b.train_id = ? AND b.journey_date = ? AND b.status = 'WAITING' " +
                    "AND b.coach_type = ? " +
                    "ORDER BY b.priority ASC, b.created_at ASC " +
                    "LIMIT 1 FOR UPDATE";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainId);
            ps.setDate(2, journeyDate);
            ps.setString(3, coachType);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapBookingFromResultSet(rs);
            }
        }
        return null;
    }
    
    /**
     * Promote RAC booking to CONFIRMED
     */
    private boolean promoteRACToConfirmed(Connection conn, Booking booking) throws SQLException {
        // Update status and assign full seat
        String sql = "UPDATE bookings SET booking_status = 'CONFIRMED', updated_at = NOW() " +
                    "WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, booking.getBookingId());
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Promote WAITING booking directly to CONFIRMED
     */
    private boolean promoteWaitingToConfirmed(Connection conn, Booking booking) throws SQLException {
        String sql = "UPDATE bookings SET booking_status = 'CONFIRMED', updated_at = NOW() " +
                    "WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, booking.getBookingId());
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Promote WAITING booking to RAC
     */
    private boolean promoteWaitingToRAC(Connection conn, Booking booking) throws SQLException {
        String sql = "UPDATE bookings SET status = 'RAC', updated_at = NOW() " +
                    "WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, booking.getBookingId());
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Map booking from ResultSet
     */
    private Booking mapBookingFromResultSet(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setBookingId(rs.getString("booking_id"));
        booking.setPnr(rs.getString("pnr"));
        booking.setUserId(rs.getInt("user_id"));
        booking.setTrainId(rs.getInt("train_id"));
        booking.setRouteId(rs.getInt("route_id"));
        booking.setJourneyDate(rs.getDate("journey_date"));
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            booking.setBookingStatus(Booking.BookingStatus.valueOf(statusStr));
        }
        
        booking.setTotalFare(rs.getBigDecimal("total_fare"));
        booking.setBookingDate(rs.getTimestamp("booking_date"));
        booking.setNumberOfPassengers(rs.getInt("number_of_passengers"));
        booking.setPriority(rs.getInt("priority"));
        booking.setUpdatedAt(rs.getTimestamp("updated_at"));
        
        String coachTypeStr = rs.getString("coach_type");
        if (coachTypeStr != null) {
            booking.setCoachType(TrainSeat.CoachType.valueOf(coachTypeStr));
        }
        
        booking.setTrainName(rs.getString("train_name"));
        booking.setTrainNumber(rs.getString("train_number"));
        booking.setUserName(rs.getString("username"));
        booking.setRouteDescription(rs.getString("route_description"));
        
        return booking;
    }
}
