package com.railway.service;

import com.railway.booking.backend.dao.UserDAO;
import com.railway.booking.backend.model.User;
import com.railway.booking.backend.service.UserService;
import com.railway.booking.backend.util.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 * Uses Mockito to mock UserDAO — no database required.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private UserService userService;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = new User();
        validUser.setUsername("testuser");
        validUser.setPassword("SecurePass1!");
        validUser.setEmail("test@example.com");
        validUser.setFirstName("Test");
        validUser.setLastName("User");
        validUser.setPhone("9876543210");
        validUser.setUserType(User.UserType.USER);
    }

    // ─── registerUser ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("registerUser: valid user → SUCCESS")
    void registerUser_validUser_returnsSuccess() {
        when(userDAO.isUsernameExists(anyString())).thenReturn(false);
        when(userDAO.isEmailExists(anyString())).thenReturn(false);
        when(userDAO.registerUser(any(User.class))).thenReturn(true);

        String result = userService.registerUser(validUser);

        assertEquals("SUCCESS", result);
        verify(userDAO, times(1)).registerUser(any(User.class));
    }

    @Test
    @DisplayName("registerUser: null user → error message")
    void registerUser_nullUser_returnsError() {
        String result = userService.registerUser(null);

        assertNotNull(result);
        assertNotEquals("SUCCESS", result);
        verifyNoInteractions(userDAO);
    }

    @Test
    @DisplayName("registerUser: duplicate username → error message")
    void registerUser_duplicateUsername_returnsError() {
        when(userDAO.isUsernameExists("testuser")).thenReturn(true);

        String result = userService.registerUser(validUser);

        assertNotEquals("SUCCESS", result);
        assertTrue(result.toLowerCase().contains("username"),
                "Error message should mention 'username', got: " + result);
        verify(userDAO, never()).registerUser(any());
    }

    @Test
    @DisplayName("registerUser: duplicate email → error message")
    void registerUser_duplicateEmail_returnsError() {
        when(userDAO.isUsernameExists(anyString())).thenReturn(false);
        when(userDAO.isEmailExists("test@example.com")).thenReturn(true);

        String result = userService.registerUser(validUser);

        assertNotEquals("SUCCESS", result);
        assertTrue(result.toLowerCase().contains("email"),
                "Error message should mention 'email', got: " + result);
        verify(userDAO, never()).registerUser(any());
    }

    @Test
    @DisplayName("registerUser: empty username → error message")
    void registerUser_emptyUsername_returnsError() {
        validUser.setUsername("");

        String result = userService.registerUser(validUser);

        assertNotEquals("SUCCESS", result);
        verifyNoInteractions(userDAO);
    }

    @Test
    @DisplayName("registerUser: invalid email format → error message")
    void registerUser_invalidEmail_returnsError() {
        validUser.setEmail("not-an-email");

        String result = userService.registerUser(validUser);

        assertNotEquals("SUCCESS", result);
        verifyNoInteractions(userDAO);
    }

    // ─── login ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login: correct BCrypt credentials → returns User")
    void login_correctCredentials_returnsUser() {
        String bcryptHash = PasswordHasher.hashPassword("SecurePass1!");
        validUser.setPassword(bcryptHash);

        when(userDAO.getUserByUsername("testuser")).thenReturn(validUser);

        User result = userService.login("testuser", "SecurePass1!");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("login: wrong password → returns null")
    void login_wrongPassword_returnsNull() {
        String bcryptHash = PasswordHasher.hashPassword("SecurePass1!");
        validUser.setPassword(bcryptHash);

        when(userDAO.getUserByUsername("testuser")).thenReturn(validUser);

        User result = userService.login("testuser", "WrongPassword");

        assertNull(result);
    }

    @Test
    @DisplayName("login: non-existent user → returns null")
    void login_nonExistentUser_returnsNull() {
        when(userDAO.getUserByUsername("ghost")).thenReturn(null);

        User result = userService.login("ghost", "anyPassword");

        assertNull(result);
    }
}
