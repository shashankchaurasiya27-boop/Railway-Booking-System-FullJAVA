package com.railway.booking.backend.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root Controller providing API discovery and system status
 */
@RestController
public class RootRestController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> index() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("system", "Railway Booking System");
        response.put("status", "ONLINE");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("health", "GET /api/health");
        endpoints.put("detailed_health", "GET /api/health/detailed");
        endpoints.put("stations", "GET /api/trains/stations");
        endpoints.put("search_trains", "POST /api/trains/search");
        endpoints.put("login", "POST /api/auth/login");
        endpoints.put("register", "POST /api/auth/register");
        endpoints.put("user_bookings", "GET /api/bookings/user/{userId}");
        endpoints.put("payment_status", "GET /api/payments/booking/{bookingId}");
        response.put("available_endpoints", endpoints);

        return ResponseEntity.ok(response);
    }
}
