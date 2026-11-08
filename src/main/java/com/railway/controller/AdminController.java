package com.railway.controller;

import com.railway.booking.backend.model.Train;
import com.railway.booking.backend.dao.TrainDAO;
import com.railway.booking.backend.service.BookingService;
import com.railway.booking.backend.service.UserService;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AdminController {
    private final UserService userService;
    private final BookingService bookingService;
    private final TrainDAO trainDAO;

    public AdminController(UserService userService, BookingService bookingService, TrainDAO trainDAO) {
        this.userService = userService;
        this.bookingService = bookingService;
        this.trainDAO = trainDAO;
    }

    public List<Train> getAllTrains() {
        return trainDAO.getAllTrains();
    }

    public List<com.railway.booking.backend.model.Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    public List<com.railway.booking.backend.model.User> getAllUsers() {
        return userService.getAllUsers();
    }

    public List<com.railway.booking.backend.model.Payment> getAllPayments() {
        // Would require PaymentDAO or similar
        return null;
    }
}
