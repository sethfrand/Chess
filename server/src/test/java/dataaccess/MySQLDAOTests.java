package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MySQLDAOTests {

    private MySQLUserDAO userDAO;
    private MySQLAuthDAO authDAO;
    private MySQLGameDAO gameDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        this.authDAO = new MySQLAuthDAO();
        this.gameDAO = new MySQLGameDAO();
        this.userDAO = new MySQLUserDAO();

        authDAO.initialize();
        userDAO.initialize();
        gameDAO.initialize();

        authDAO.clear();
        gameDAO.clear();
        userDAO.clear();
    }

    @AfterEach
    void tearDown() throws DataAccessException {
        authDAO.clear();
        gameDAO.clear();
        userDAO.clear();
    }

    //authDAO tests
    @Test
    void createAuthPos() throws DataAccessException {
        String username = "test";
        AuthData authData = authDAO.createAuth(username);
        assertNotNull(authData);
        assertNotNull(authData.getAuthToken());
        assertEquals(username, authData.getUsername());
        assertFalse(authData.getAuthToken().isEmpty());
    }

    @Test
    void createAuthNeg() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(null));
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(""));
        assertThrows(DataAccessException.class, () -> authDAO.createAuth("   "));
    }


    @Test
    void getUsernamePos() throws DataAccessException {
        String user = "user";
        AuthData authData = authDAO.createAuth(user);

        String checkUser = authDAO.getUsername(authData.getAuthToken());
        assertEquals(user, checkUser);
    }

    @Test
    void getUsernameNeg() throws DataAccessException {
        String noToken = authDAO.getUsername("this is not the auth token you are looking for");
        assertNull(noToken);

        String tokenNull = authDAO.getUsername(null);
        assertNull((tokenNull));

        String blankToken = authDAO.getUsername("");
        assertNull(blankToken);

    }

    @Test
    void deleteAuthPos() throws DataAccessException {
        String user = "Obi-Wan Kanobi";
        AuthData authData = authDAO.createAuth(user);

        Boolean delete = authDAO.deleteAuth(authData.getAuthToken());
        assertTrue(delete);
    }

    @Test
    void deleteAuthNeg() throws DataAccessException {

        Boolean result = authDAO.deleteAuth("Anakin Skywalker");
        assertFalse(result);
    }

    @Test
    void clearAuth() throws DataAccessException {
        authDAO.createAuth("banana");
        authDAO.createAuth("apple");
        authDAO.createAuth("Darth Sideous");

        assertDoesNotThrow(() -> authDAO.clear());
    }


}