package com.railway.booking.backend.util;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for secure password hashing using BCrypt
 */
public class PasswordHasher {
    private static final Logger logger = LoggerFactory.getLogger(PasswordHasher.class);
    
    // BCrypt work factor (number of rounds) - higher is more secure but slower
    // 12 is a good balance between security and performance
    private static final int BCRYPT_ROUNDS = 12;
    
    /**
     * Hash a plain text password using BCrypt
     * 
     * @param plainTextPassword The password to hash
     * @return The hashed password
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.isEmpty()) {
            logger.warn("Attempted to hash null or empty password");
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        try {
            String hashedPassword = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
            logger.debug("Password hashed successfully");
            return hashedPassword;
        } catch (Exception e) {
            logger.error("Error hashing password", e);
            throw new RuntimeException("Failed to hash password", e);
        }
    }
    
    /**
     * Verify a plain text password against a hashed password
     * 
     * @param plainTextPassword The password to verify
     * @param hashedPassword The hashed password to check against
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || plainTextPassword.isEmpty()) {
            logger.warn("Attempted to verify null or empty password");
            return false;
        }
        
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            logger.warn("Attempted to verify against null or empty hash");
            return false;
        }
        
        try {
            boolean matches = BCrypt.checkpw(plainTextPassword, hashedPassword);
            logger.debug("Password verification result: {}", matches);
            return matches;
        } catch (Exception e) {
            logger.error("Error verifying password", e);
            return false;
        }
    }
    
    /**
     * Check if a password needs to be rehashed (e.g., if work factor has changed)
     * 
     * @param hashedPassword The hashed password to check
     * @return true if the password should be rehashed
     */
    public static boolean needsRehash(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            return true;
        }
        
        try {
            // Extract the work factor from the hash
            String[] parts = hashedPassword.split("\\$");
            if (parts.length < 4) {
                return true;
            }
            
            int currentRounds = Integer.parseInt(parts[2]);
            return currentRounds < BCRYPT_ROUNDS;
        } catch (Exception e) {
            logger.warn("Could not determine if password needs rehash", e);
            return true;
        }
    }
    
    /**
     * Check if a string is a valid BCrypt hash
     * 
     * @param hash The string to check
     * @return true if it's a valid BCrypt hash
     */
    public static boolean isBCryptHash(String hash) {
        if (hash == null || hash.isEmpty()) {
            return false;
        }
        
        // BCrypt hashes start with $2a$, $2b$, or $2y$
        return hash.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }
}
