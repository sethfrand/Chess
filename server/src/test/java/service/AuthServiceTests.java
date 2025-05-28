package service;

import model.UserData;
import org.junit.jupiter.api.Test;
import dataaccess.*;
import handler.*;
import server.*;
import service.*;
import model.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTests {
    private AuthService authservice;
    private MySQLUserDAO userDAO;

    @BeforeEach
    void setup() throws DataAccessException {
        authservice = new AuthService();
        userDAO = new MySQLUserDAO();
        authservice.clear();
        userDAO.clear();
    }

    @Test
    void positiveLogIn() throws DataAccessException {
        userDAO.createUser(new UserData("user", "pass", "email"));
        AuthData authData = authservice.login("user", "pass");
        assertNotNull(authData);
        assertEquals("user", authData.getUsername());
    }

    @Test
    void negLogIn() throws DataAccessException {
        userDAO.createUser(new UserData("user", "pass", "email"));
        AuthData authData = authservice.login("user", "not_pass");
        assertNull(authData);
    }

    @Test
    void logoutPos() throws DataAccessException {
        userDAO.createUser(new UserData("user", "pass", "email"));
        AuthData authData = authservice.login("user", "pass");
        assertTrue(authservice.logout(authData.getAuthToken()));
    }

    @Test
    void logoutNeg() throws DataAccessException {
        assertFalse(authservice.logout("not a valid token, bob"));
    }

    @Test
    void getUserTokenPositive() throws DataAccessException {
        userDAO.createUser(new UserData("user", "pass", "email"));
        AuthData authData = authservice.login("user", "pass");
        String username = authservice.getUsernameForToken(authData.getAuthToken());
        authservice.clear();
        assertEquals("user", username);
    }

    @Test
    void getUserTokenNeg() throws DataAccessException {
        assertNull(authservice.getUsernameForToken("not a valid token for this user"));
    }

    @Test
    void clear() throws DataAccessException {
        userDAO.createUser(new UserData("user", "pass", "email"));
        AuthData authData = authservice.login("user", "pass");
        authservice.clear();
        assertNull(authservice.getUsernameForToken(authData.getAuthToken()));
    }
}

