package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;
import java.util.Collection;

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

    private void verifyCreateAndGet(UserData user) throws DataAccessException {
        assertDoesNotThrow(() -> userDAO.createUser(user));

        UserData getUser = userDAO.getUser("Hello there");
        assertNotNull(getUser);
        assertEquals("Hello there", getUser.getUserName());
        assertEquals("this@email.com", getUser.getEmail());
        assertNotEquals("General Kenobi", getUser.getPassword());
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

    //userDAOs

    @Test
    void createUserPos() throws DataAccessException {
        UserData user = new UserData("Hello there", "General Kenobi", "this@email.com");
        verifyCreateAndGet(user);
    }


    @Test
    void createUserNeg() throws DataAccessException {
        UserData user1 = new UserData("Anakin Skywalker", "pass", "this@email.com");
        UserData user2 = new UserData("Anakin Skywalker", "not the younglings", "another@email.com");

        userDAO.createUser(user1);

        assertThrows(DataAccessException.class, () -> userDAO.createUser(user2));
    }

    @Test
    void createUserNegNull() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> userDAO.createUser(null));

        assertThrows(DataAccessException.class, () -> userDAO.createUser(new UserData(null, "r2-d2", "email@thegalazcticsenate.com")));
        assertThrows(DataAccessException.class, () -> userDAO.createUser(new UserData("C3-PO", null, "emailz@thegalazcticsenate.com")));
        assertThrows(DataAccessException.class, () -> userDAO.createUser(new UserData("gonk-droid", "r2-d2", null)));
    }

    @Test
    void getUserPos() throws DataAccessException {
        UserData user = new UserData("Hello there", "General Kenobi", "this@email.com");
        verifyCreateAndGet(user);

    }

    @Test
    void getUserNeg() throws DataAccessException {
        UserData getuser1 = userDAO.getUser("This is not the droid you are looking for");
        assertNull(getuser1);

        UserData getuser2 = userDAO.getUser(null);
        assertNull(getuser2);

        UserData getuser3 = userDAO.getUser("");
        assertNull((getuser3));

        UserData getuser4 = userDAO.getUser("           ");
        assertNull(getuser4);

    }

    @Test
    void verifyUserPos() throws DataAccessException {
        UserData user = new UserData("Hello there", "General Kenobi", "this@email.com");
        userDAO.createUser(user);

        UserData verified = userDAO.verifyUser("Hello there", "General Kenobi");
        assertNotNull(verified);

        assertEquals("Hello there", verified.getUserName());
    }

    @Test
    void verifyUserNeg() throws DataAccessException {
        UserData user = new UserData("Hello there", "General Kenobi", "this@email.com");
        userDAO.createUser(user);

        UserData wrongpass = userDAO.verifyUser("Hello there", "GeNErAL KUhNOObi");
        assertNull(wrongpass);
    }

    @Test
    void clearUsers() throws DataAccessException {
        userDAO.createUser(new UserData("C3-PO", "1", "emailz@thegalazcticsenate.com"));
        userDAO.createUser(new UserData("Jabba", "2", "emailz@thegalazcticsenate.com"));
        userDAO.createUser(new UserData("Luke", "3", "emailz@thegalazcticsenate.com"));

        assertDoesNotThrow(() -> userDAO.clear());
        assertNull(userDAO.getUser("C3-PO"));
        assertNull(userDAO.getUser("Jabba"));
        assertNull(userDAO.getUser("Luke"));
    }


    //SQLGameDAO tests

    @Test
    void createGamePos() throws DataAccessException {
        String gamename = "game1";
        int gameID = gameDAO.createGame(gamename);
        assertTrue(gameID > 0);

        GameData verified = gameDAO.getGame(gameID);
        assertNotNull(verified);
        assertNotNull(verified.getGame());
        assertNull(verified.getWhiteUsername());
        assertNull(verified.getBlackUsername());
        assertEquals(gameID, verified.getGameID());
        assertEquals(gamename, verified.getGameName());
    }

    @Test
    void createGameNeg() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(null));

        assertThrows(DataAccessException.class, () -> gameDAO.createGame(""));
        assertThrows(DataAccessException.class, () -> gameDAO.createGame("    "));
    }


    @Test
    void listGamesPos() throws DataAccessException {
        int gameID1 = gameDAO.createGame("Game1");
        int gameID2 = gameDAO.createGame("Game2");


        Collection<GameData> allgames = gameDAO.listGames();
        assertNotNull(allgames);
        assertEquals(2, allgames.size());
    }


    @Test
    void listGamesNeg() throws DataAccessException {
        Collection<GameData> allgames = gameDAO.listGames();
        assertNotNull(allgames);
        assertEquals(0, allgames.size());

    }


    @Test
    void getGamePos() throws DataAccessException {
        String gameName = "this test game";
        int gameID = gameDAO.createGame(gameName);

        GameData verify = gameDAO.getGame(gameID);
        assertNotNull(verify);
        assertEquals(gameID, verify.getGameID());
        assertEquals(gameName, verify.getGameName());
        assertNotNull(verify.getGame());

    }

    @Test
    void getGameNeg() throws DataAccessException {
        GameData game = gameDAO.getGame(42);
        assertNull(game);
    }

    @Test
    void updateGamePos() throws DataAccessException {
        int gameID = gameDAO.createGame("test");
        GameData ogGame = gameDAO.getGame(gameID);

        ChessGame updatedGame = new ChessGame();
        GameData update = new GameData(gameID, "white", "black", "test", updatedGame);
        boolean verify = gameDAO.updateGame(gameID, update);
        assertTrue(verify);
    }

    @Test
    void updateGameNeg() throws DataAccessException {
        ChessGame chess = new ChessGame();
        GameData ogGame = new GameData(42, "white", "black", "test", chess);
        boolean verify = gameDAO.updateGame(42, ogGame);
        assertFalse(verify);
    }


    @Test
    void takeWhitePos() throws DataAccessException {
        int gameID = gameDAO.createGame("test");
        boolean verify = gameDAO.takeColor(gameID, "chewy", "WHITE");
        assertTrue(verify);
    }

    @Test
    void takeBlackPos() throws DataAccessException {
        int gameID = gameDAO.createGame("test");
        boolean verify = gameDAO.takeColor(gameID, "chewy", "BLACK");
        assertTrue(verify);

    }

    @Test
    void takeWhiteNeg() throws DataAccessException {
        int gameID = gameDAO.createGame("test");

        gameDAO.takeColor(gameID, "user1", "WHITE");
        boolean verify = gameDAO.takeColor(gameID, "chewy", "WHITE");
        assertFalse(verify);

    }

    @Test
    void takeBlackNeg() throws DataAccessException {
        int gameID = gameDAO.createGame("test");

        gameDAO.takeColor(gameID, "user1", "BLACK");
        boolean verify = gameDAO.takeColor(gameID, "chewy", "BLACK");
        assertFalse(verify);

    }

    @Test
    void takeColorNoGame() throws DataAccessException {
        boolean verify = gameDAO.takeColor(42, "user1", "BLACK");
        assertFalse(verify);

    }

    @Test
    void gameClear() throws DataAccessException {
        gameDAO.createGame("game1");
        gameDAO.createGame("game2");
        gameDAO.createGame("game3");

        assertDoesNotThrow(() -> gameDAO.clear());
    }
}