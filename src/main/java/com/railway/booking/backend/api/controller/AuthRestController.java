package com.railway.booking.backend.api.controller;

import com.railway.booking.backend.api.dto.request.LoginRequest;
import com.railway.booking.backend.api.dto.response.ErrorResponse;
import com.railway.booking.backend.model.User;
import com.railway.booking.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication operations
 */
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {
    private static final Logger logger = LoggerFactory.getLogger(AuthRestController.class);
    private final UserService userService;

    public AuthRestController(UserService userService) {
        this.userService = userService;
    }

    /**
     * User login endpoint
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        logger.info("Login attempt for user: {}", request.getUsername());

        try {
            User user = userService.login(request.getUsername(), request.getPassword());

            if (user == null) {
                logger.warn("Login failed for user: {}", request.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Invalid credentials", "Username or password is incorrect"));
            }

            // Create a simple session response (without full Session object)
            logger.info("Login successful for user: {}", request.getUsername());
            SimpleLoginResponse response = new SimpleLoginResponse(user);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Login error for user: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Server error", "An error occurred during login"));
        }
    }

    // Simple login response without session management
    private static class SimpleLoginResponse {
        private int userId;
        private String username;
        private String userType;

        public SimpleLoginResponse(User user) {
            this.userId = user.getUserId();
            this.username = user.getUsername();
            this.userType = user.getUserType().name();
        }

        public int getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getUserType() {
            return userType;
        }
    }
}
