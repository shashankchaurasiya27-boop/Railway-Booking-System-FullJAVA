package com.railway.booking.backend.service;

import com.railway.booking.backend.dao.BookingDAO;
import com.railway.booking.backend.dao.TrainDAO;
import com.railway.booking.backend.model.Booking;
import com.railway.booking.backend.model.Train;
import com.railway.booking.backend.model.TrainSeat;
import com.railway.booking.backend.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Booking service with Java 21 Virtual Thread support for blocking I/O
 * operations.
 * Now integrated with SeatAvailabilityEngine for real-time seat tracking.
 */
@Service
@Transactional
public class BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final BookingDAO bookingDAO;
    private final TrainDAO trainDAO;
    private final SeatAvailabilityService seatAvailabilityService;
    private final ConcurrentBookingService concurrentBookingService;

    public BookingService(BookingDAO bookingDAO, TrainDAO trainDAO, 
                         SeatAvailabilityService seatAvailabilityService,
                         ConcurrentBookingService concurrentBookingService) {
        this.bookingDAO = bookingDAO;
        this.trainDAO = trainDAO;
        this.seatAvailabilityService = seatAvailabilityService;
        this.concurrentBookingService = concurrentBookingService;
    }

    /**
     * Create new booking with PRODUCTION-GRADE concurrency control.
     * 
     * 3-LAYER PROTECTION:
     * 1. Application-level lock (ReentrantLock) - orders concurrent requests
     * 2. Database transaction with SERIALIZABLE isolation - prevents dirty reads
     * 3. Row-level locking (SELECT FOR UPDATE) + Atomic update - prevents overbooking
     * 
     * DEADLOCK PREVENTION ORDER:
     * 1. Lock train_seats row
     * 2. Lock seat_availability row  
     * 3. Atomic decrement of available_seats
     * 4. Create booking
     * 5. Commit
     */
    public String createBooking(Booking booking, TrainSeat.CoachType coachType) {
        // Delegate to ConcurrentBookingService for thread-safe booking
        String result = concurrentBookingService.bookTicket(booking, coachType);
        
        // Parse result from ConcurrentBookingService
        if (result.startsWith("SUCCESS:")) {
            // Extract booking ID from result format: SUCCESS:bookingId:pnr:status
            String[] parts = result.split(":");
            String bookingId = parts.length > 1 ? parts[1] : "UNKNOWN";
            logger.info("Booking created successfully via ConcurrentBookingService: {}", bookingId);
            return "SUCCESS:" + bookingId;
        } else if (result.startsWith("WAITING")) {
            logger.info("Booking added to waiting list: {}", result);
            return result;
        } else if (result.startsWith("FAILED:")) {
            logger.warn("Booking failed: {}", result);
            return result.substring(7); // Remove "FAILED:" prefix
        } else {
            logger.error("Unexpected booking result: {}", result);
            return "Booking failed. Please try again.";
        }
    }

    /**
     * Legacy createBooking method (defaults to GENERAL coach type)
     */
    public String createBooking(Booking booking) {
        return createBooking(booking, TrainSeat.CoachType.GENERAL);
    }

    /**
     * Cancel booking with PRODUCTION-GRADE concurrency control.
     * 
     * THREAD-SAFE CANCELLATION:
     * 1. Application-level lock ensures ordered cancellation processing
     * 2. Row-level locking (SELECT FOR UPDATE) on booking record
     * 3. Atomic seat release with booked_seats verification
     * 4. Automatic promotion chain triggers (RAC/WAITING → CONFIRMED)
     * 
     * Returns: "SUCCESS" or "FAILED:message"
     */
    public String cancelBooking(String bookingId) {
        // Delegate to ConcurrentBookingService for thread-safe cancellation
        String result = concurrentBookingService.cancelTicket(bookingId);
        
        if ("SUCCESS".equals(result)) {
            logger.info("Booking cancelled successfully via ConcurrentBookingService: {}", bookingId);
            return "SUCCESS";
        } else if (result.startsWith("FAILED:")) {
            logger.warn("Cancellation failed for booking {}: {}", bookingId, result);
            return result.substring(7); // Remove "FAILED:" prefix
        } else {
            logger.error("Unexpected cancellation result for booking {}: {}", bookingId, result);
            return result;
        }
    }

    /**
     * Get bookings by user ID.
     */
    public List<Booking> getUserBookings(int userId) {
        return bookingDAO.getBookingsByUserId(userId);
    }

    /**
     * Get all bookings (Admin).
     */
    public List<Booking> getAllBookings() {
        return bookingDAO.getAllBookings();
    }

    /**
     * Get booking by ID.
     */
    public Booking getBookingById(String bookingId) {
        return bookingDAO.getBookingById(bookingId);
    }

    /**
     * Get seat availability for a train on a specific date
     */
    public List<com.railway.booking.backend.model.SeatAvailability> getSeatAvailability(int trainId, java.sql.Date journeyDate) {
        return seatAvailabilityService.getAvailabilityForTrain(trainId, journeyDate);
    }

    /**
     * Track booking by PNR (Passenger Name Record)
     * Industry-grade PNR lookup for ticket tracking
     *
     * @param pnr The PNR to search
     * @return Booking object or null if not found
     */
    public Booking trackBooking(String pnr) {
        if (pnr == null || pnr.trim().isEmpty()) {
            logger.warn("PNR is null or empty");
            return null;
        }

        // Validate PNR format
        if (!com.railway.booking.backend.util.PNRGenerator.isValidPNR(pnr)) {
            logger.warn("Invalid PNR format: {}", pnr);
            return null;
        }

        // Delegate to ConcurrentBookingService for PNR lookup
        Booking booking = concurrentBookingService.trackBooking(pnr);

        if (booking != null) {
            logger.info("Booking found for PNR {}: BookingID={}, Status={}",
                       pnr, booking.getBookingId(), booking.getBookingStatus());
        } else {
            logger.info("No booking found for PNR: {}", pnr);
        }

        return booking;
    }

}
