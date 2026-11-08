package com.railway.service;

import com.railway.booking.backend.dao.BookingDAO;
import com.railway.booking.backend.dao.TrainDAO;
import com.railway.booking.backend.model.Booking;
import com.railway.booking.backend.model.Train;
import com.railway.booking.backend.model.TrainSeat;
import com.railway.booking.backend.service.BookingService;
import com.railway.booking.backend.service.ConcurrentBookingService;
import com.railway.booking.backend.service.SeatAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookingService.
 * Uses Mockito to mock DAOs and delegated services — no database required.
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingDAO bookingDAO;

    @Mock
    private TrainDAO trainDAO;

    @Mock
    private SeatAvailabilityService seatAvailabilityService;

    @Mock
    private ConcurrentBookingService concurrentBookingService;

    @InjectMocks
    private BookingService bookingService;

    private Booking validBooking;
    private Train availableTrain;

    @BeforeEach
    void setUp() {
        availableTrain = new Train();
        availableTrain.setTrainId(101);
        availableTrain.setTotalSeats(100);

        validBooking = new Booking();
        validBooking.setUserId(1);
        validBooking.setTrainId(101);
        validBooking.setRouteId(1);
        validBooking.setJourneyDate(Date.valueOf(LocalDate.now().plusDays(1)));
        validBooking.setNumberOfPassengers(1);
        validBooking.setPassengerName("Rahul Sharma");
        validBooking.setPassengerAge(28);
        validBooking.setTotalFare(new BigDecimal("500.00"));
    }

    // ─── createBooking ────────────────────────────────────────────────────────

    @Test
    @DisplayName("createBooking: seats available → SUCCESS with UUID-based booking ID")
    void createBooking_seatsAvailable_returnsSuccess() {
        when(concurrentBookingService.bookTicket(any(Booking.class), any(TrainSeat.CoachType.class)))
                .thenReturn("SUCCESS:RB1A2B3C4D5E6F:PNR1234567890:CONFIRMED");

        String result = bookingService.createBooking(validBooking);

        assertTrue(result.startsWith("SUCCESS:"), "Expected SUCCESS prefix, got: " + result);
        String bookingId = result.substring("SUCCESS:".length());
        assertTrue(bookingId.matches("RB[A-F0-9]{12}"),
                "Booking ID should match UUID format, got: " + bookingId);
    }

    @Test
    @DisplayName("createBooking: two concurrent bookings → different IDs (collision safety)")
    void createBooking_twoBookings_differentIds() {
        when(concurrentBookingService.bookTicket(any(Booking.class), any(TrainSeat.CoachType.class)))
                .thenReturn("SUCCESS:RB111111111111:PNR1:CONFIRMED", "SUCCESS:RB222222222222:PNR2:CONFIRMED");

        String result1 = bookingService.createBooking(validBooking);
        validBooking.setBookingId(null);
        String result2 = bookingService.createBooking(validBooking);

        assertTrue(result1.startsWith("SUCCESS:"));
        assertTrue(result2.startsWith("SUCCESS:"));
        assertNotEquals(result1, result2, "Two bookings must have different IDs");
    }

    @Test
    @DisplayName("createBooking: no seats available → waiting list message")
    void createBooking_noSeatsAvailable_returnsWaitingMessage() {
        when(concurrentBookingService.bookTicket(any(Booking.class), any(TrainSeat.CoachType.class)))
                .thenReturn("WAITING:WL-5");

        String result = bookingService.createBooking(validBooking);

        assertFalse(result.startsWith("SUCCESS:"), "Should not succeed when no seats available");
        assertTrue(result.toLowerCase().contains("waiting"), "Should mention waiting list");
    }

    @Test
    @DisplayName("createBooking: train not found → error message")
    void createBooking_trainNotFound_returnsError() {
        when(concurrentBookingService.bookTicket(any(Booking.class), any(TrainSeat.CoachType.class)))
                .thenReturn("FAILED:Train not found or inactive");

        String result = bookingService.createBooking(validBooking);

        assertFalse(result.startsWith("SUCCESS:"));
        assertTrue(result.toLowerCase().contains("train"));
    }

    @Test
    @DisplayName("createBooking: empty passenger name → validation error")
    void createBooking_emptyPassengerName_returnsValidationError() {
        when(concurrentBookingService.bookTicket(any(Booking.class), any(TrainSeat.CoachType.class)))
                .thenReturn("FAILED:Passenger name is required");

        validBooking.setPassengerName("");

        String result = bookingService.createBooking(validBooking);

        assertFalse(result.startsWith("SUCCESS:"));
        assertTrue(result.toLowerCase().contains("passenger"));
    }

    @Test
    @DisplayName("createBooking: invalid passenger age (0) → validation error")
    void createBooking_invalidAge_returnsValidationError() {
        when(concurrentBookingService.bookTicket(any(Booking.class), any(TrainSeat.CoachType.class)))
                .thenReturn("FAILED:Invalid passenger age");

        validBooking.setPassengerAge(0);

        String result = bookingService.createBooking(validBooking);

        assertFalse(result.startsWith("SUCCESS:"));
    }

    // ─── cancelBooking ────────────────────────────────────────────────────────

    @Test
    @DisplayName("cancelBooking: valid booking → SUCCESS")
    void cancelBooking_validBooking_returnsSuccess() {
        when(concurrentBookingService.cancelTicket("RBABC123DEF456")).thenReturn("SUCCESS");

        String result = bookingService.cancelBooking("RBABC123DEF456");

        assertEquals("SUCCESS", result);
    }

    @Test
    @DisplayName("cancelBooking: booking not found → error message")
    void cancelBooking_notFound_returnsError() {
        when(concurrentBookingService.cancelTicket("INVALID")).thenReturn("FAILED:Booking not found");

        String result = bookingService.cancelBooking("INVALID");

        assertNotEquals("SUCCESS", result);
        assertTrue(result.toLowerCase().contains("not found"));
    }

    @Test
    @DisplayName("cancelBooking: already cancelled → error message")
    void cancelBooking_alreadyCancelled_returnsError() {
        when(concurrentBookingService.cancelTicket("RBABC123DEF456")).thenReturn("FAILED:Booking already cancelled");

        String result = bookingService.cancelBooking("RBABC123DEF456");

        assertNotEquals("SUCCESS", result);
        assertTrue(result.toLowerCase().contains("cancel"));
    }
}
