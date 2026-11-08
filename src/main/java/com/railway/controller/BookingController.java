package com.railway.controller;

import com.railway.booking.backend.model.Booking;
import com.railway.booking.backend.service.BookingService;
import com.railway.booking.backend.util.Constants;

import javax.swing.*;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Create a new booking and provide user feedback
    public String createBooking(Booking booking) {
        String result = bookingService.createBooking(booking);
        if (result.startsWith("SUCCESS")) {
            JOptionPane.showMessageDialog(null, Constants.SUCCESS_BOOKING);
            // Return the generated Booking ID
            return result.split(":")[1];
        } else {
            JOptionPane.showMessageDialog(null, result);
            return null;
        }
    }

    // Fetch all bookings for a user
    public List<Booking> getUserBookings(int userId) {
        return bookingService.getUserBookings(userId);
    }

    // Cancel an existing booking
    public String cancelBooking(String bookingId) {
        String result = bookingService.cancelBooking(bookingId);
        if ("SUCCESS".equals(result)) {
            JOptionPane.showMessageDialog(null, Constants.SUCCESS_CANCELLATION);
        } else {
            JOptionPane.showMessageDialog(null, result);
        }
        return result;
    }
    
    // Get booking by ID
    public Booking getBookingById(String bookingId) {
        return bookingService.getBookingById(bookingId);
    }
}
