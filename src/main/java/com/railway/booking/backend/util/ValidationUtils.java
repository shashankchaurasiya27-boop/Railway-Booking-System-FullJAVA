package com.railway.booking.backend.util;

import java.util.regex.Pattern;

public class ValidationUtils {

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Phone validation pattern (Indian format)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

    // Username validation pattern
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    /**
     * Validates email address
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates phone number
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validates username
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validates password strength
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        return true;
    }

    /**
     * Validates if string is not empty
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Validates age
     */
    public static boolean isValidAge(int age) {
        return age > 0 && age <= 120;
    }

    /**
     * Validates train number format
     */
    public static boolean isValidTrainNumber(String trainNumber) {
        if (trainNumber == null || trainNumber.trim().isEmpty()) {
            return false;
        }
        return trainNumber.matches("^[0-9]{4,5}$");
    }

    /**
     * Validates station code format
     */
    public static boolean isValidStationCode(String stationCode) {
        if (stationCode == null || stationCode.trim().isEmpty()) {
            return false;
        }
        return stationCode.matches("^[A-Z]{2,6}$");
    }

    /**
     * SQL injection prevention note:
     * This codebase uses JdbcTemplate with parameterized queries throughout all
     * DAOs.
     * Parameterized queries fully prevent SQL injection at the database driver
     * level.
     * A manual sanitizeInput() method is NOT needed and was removed because it
     * would
     * silently corrupt legitimate data (e.g., names like "O'Brien" or addresses
     * with quotes).
     */

    /**
     * Validates numeric input
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
