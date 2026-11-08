package com.railway.booking.backend.api.controller;

import com.railway.booking.backend.api.dto.request.TrainSearchRequest;
import com.railway.booking.backend.api.dto.response.ErrorResponse;
import com.railway.booking.backend.api.dto.response.TrainSearchResponse;
import com.railway.booking.backend.dao.TicketDAO;
import com.railway.booking.backend.model.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * REST Controller for train search operations
 */
@RestController
@RequestMapping("/api/trains")
public class TrainRestController {
    private static final Logger logger = LoggerFactory.getLogger(TrainRestController.class);
    private final TicketDAO ticketDAO;

    public TrainRestController(TicketDAO ticketDAO) {
        this.ticketDAO = ticketDAO;
    }

    /**
     * Search trains endpoint
     * POST /api/trains/search
     */
    @PostMapping("/search")
    public ResponseEntity<?> searchTrains(@RequestBody TrainSearchRequest request) {
        logger.info("Train search: {} to {} on {}",
                request.getSourceStation(), request.getDestinationStation(), request.getJourneyDate());

        try {
            // Parse journey date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date journeyDate = new Date(dateFormat.parse(request.getJourneyDate()).getTime());

            // Search trains
            List<Ticket> tickets = ticketDAO.searchTrains(
                    request.getSourceStation(),
                    request.getDestinationStation(),
                    journeyDate);

            logger.info("Found {} trains", tickets.size());
            return ResponseEntity.ok(new TrainSearchResponse(tickets));

        } catch (Exception e) {
            logger.error("Train search error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Server error",
                            "An error occurred during train search: " + e.getMessage()));
        }
    }

    /**
     * Get all stations endpoint
     * GET /api/trains/stations
     */
    @GetMapping("/stations")
    public ResponseEntity<?> getAllStations() {
        try {
            List<String> stations = ticketDAO.getAllStations();
            return ResponseEntity.ok(stations);
        } catch (Exception e) {
            logger.error("Error fetching stations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Server error", "An error occurred while fetching stations"));
        }
    }
}
