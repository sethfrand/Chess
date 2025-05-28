package service;

import model.UserData;
import org.junit.jupiter.api.Test;
import dataaccess.*;
import model.*;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;


public class UserServiceTests {
    private UserService userService;

    @BeforeEach
    void setUp() throws DataAccessException {
        userService = new UserService();
        userService.clear();
    }

    @Test
    void posRegister() throws DataAccessException {
        AuthData authData = userService.register(new UserData("this user", "this pass", "this email"));
        assertNotNull(authData);
        assertEquals("this user", authData.getUsername());
    }

    @Test
    void negRegisterbcDuplicates() throws DataAccessException {
        userService.register(new UserData("this user", "this pass", "this email"));
        AuthData another = userService.register(new UserData("this user", "this pass", "this email"));
        assertNull(another);
    }

    @Test
    void negInvalidUser() {
        assertThrows(DataAccessException.class, () ->
        {
            userService.register(new UserData(null, "this pass", "this email"));
        });
    }

    @Test
    void clear() throws DataAccessException {
        userService.register(new UserData("this name", "this pass", "an email"));
        userService.clear();
        assertDoesNotThrow(() ->
        {
            AuthData authData = userService.register(new UserData("this name", "this pass", "an email"));
            assertNotNull(authData);
        });
    }
}