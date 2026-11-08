package com.railway.booking.backend.api.controller;

import com.railway.booking.backend.api.dto.request.BookingRequest;
import com.railway.booking.backend.api.dto.response.BookingResponse;
import com.railway.booking.backend.api.dto.response.ErrorResponse;
import com.railway.booking.backend.model.Booking;
import com.railway.booking.backend.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for booking operations
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingRestController {
    private static final Logger logger = LoggerFactory.getLogger(BookingRestController.class);
    private final BookingService bookingService;

    public BookingRestController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Create booking endpoint
     * POST /api/bookings
     */
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        logger.info("Creating booking for user {} on train {}", request.getUserId(), request.getTrainId());

        // Validate required passenger fields
        if (request.getPassengerName() == null || request.getPassengerName().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Validation Error", "passengerName is required"));
        }
        if (request.getPassengerAge() <= 0 || request.getPassengerAge() > 120) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Validation Error", "passengerAge must be between 1 and 120"));
        }
        if (request.getTotalFare() == null || request.getTotalFare().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Validation Error", "totalFare must be a positive value"));
        }

        try {
            // Parse journey date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date journeyDate = new Date(dateFormat.parse(request.getJourneyDate()).getTime());

            // Build booking from real request data
            Booking booking = new Booking();
            booking.setUserId(request.getUserId());
            booking.setTrainId(request.getTrainId());
            booking.setRouteId(request.getRouteId());
            booking.setJourneyDate(journeyDate);
            booking.setNumberOfPassengers(request.getNumberOfPassengers());
            booking.setPassengerName(request.getPassengerName());
            booking.setPassengerAge(request.getPassengerAge());
            booking.setTotalFare(request.getTotalFare());

            // Map gender if provided
            if (request.getPassengerGender() != null && !request.getPassengerGender().isBlank()) {
                try {
                    booking.setPassengerGender(Booking.Gender.valueOf(request.getPassengerGender().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ErrorResponse("Validation Error",
                                    "passengerGender must be one of: MALE, FEMALE, OTHER"));
                }
            }

            String result = bookingService.createBooking(booking);

            if (result == null || !result.startsWith("SUCCESS:")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Booking failed",
                                result != null ? result : "Unable to create booking"));
            }

            String bookingId = result.substring("SUCCESS:".length());
            Booking createdBooking = bookingService.getBookingById(bookingId);

            if (createdBooking == null) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new SimpleBookingResponse(bookingId, "Booking created successfully"));
            }

            logger.info("Booking created successfully: {}", bookingId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new BookingResponse(createdBooking));

        } catch (ParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid Date", "journeyDate must be in yyyy-MM-dd format"));
        } catch (Exception e) {
            logger.error("Booking creation error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Server error", "An error occurred during booking: " + e.getMessage()));
        }
    }

    /**
     * Get user bookings endpoint
     * GET /api/bookings/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserBookings(@PathVariable int userId) {
        logger.info("Fetching bookings for user {}", userId);

        try {
            List<Booking> bookings = bookingService.getUserBookings(userId);
            List<BookingResponse> response = bookings.stream()
                    .map(BookingResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching user bookings", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Server error", "An error occurred while fetching bookings"));
        }
    }

    /**
     * Cancel booking endpoint
     * PUT /api/bookings/{bookingId}/cancel
     */
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable String bookingId) {
        logger.info("Cancelling booking: {}", bookingId);

        try {
            String result = bookingService.cancelBooking(bookingId);

            if (result == null || !result.equals("SUCCESS")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Booking not found",
                                result != null ? result : "Unable to find or cancel booking"));
            }

            return ResponseEntity.ok(new CancelResponse("Booking cancelled successfully", bookingId, "CANCELLED"));

        } catch (Exception e) {
            logger.error("Booking cancellation error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Server error", "An error occurred during cancellation"));
        }
    }

    // Helper classes for responses
    private static class SimpleBookingResponse {
        private String bookingId;
        private String message;

        public SimpleBookingResponse(String bookingId, String message) {
            this.bookingId = bookingId;
            this.message = message;
        }

        public String getBookingId() {
            return bookingId;
        }

        public String getMessage() {
            return message;
        }
    }

    private static class CancelResponse {
        private String message;
        private String bookingId;
        private String status;

        public CancelResponse(String message, String bookingId, String status) {
            this.message = message;
            this.bookingId = bookingId;
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public String getBookingId() {
            return bookingId;
        }

        public String getStatus() {
            return status;
        }
    }
}
