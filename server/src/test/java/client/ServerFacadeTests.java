package client;


import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;
import ui.ServerFacade;


import java.util.Collection;


public class ServerFacadeTests {


    private static Server server;
    private static ServerFacade facade;


    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);


    }


    @BeforeEach
    public void clearDatabase() throws Exception {
        // Clear the database before each test to ensure a fresh state
        try {
            facade.makeRequest("DELETE", "/db", null, null, null);
        } catch (Exception e) {
        }
    }


    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void registerPos() throws Exception {
        String authToken = facade.register("obi wan", "password", "email");
        Assertions.assertNotNull(authToken);
        Assertions.assertFalse((authToken.isEmpty()));
    }


    @Test
    public void registerNeg() throws Exception {
        Assertions.assertThrows(Exception.class, () -> {
            facade.register("obiwan", "pass", "email");
            facade.register("obiwan", "another", "email");
        });
    }


    @Test
    public void loginPos() throws Exception {
        facade.register("obi", "wan", "kenobi");


        String authToken = facade.login("obi", "wan");
        Assertions.assertNotNull(authToken);
        Assertions.assertFalse(authToken.isEmpty());
    }


    @Test
    public void loginNeg() throws Exception {
        Assertions.assertThrows(Exception.class, () ->
        {
            facade.login("someone", "wan");
        });
    }


    @Test
    public void logoutPos() throws Exception {
        String authToken = facade.register("obi", "wan", "kenobi");


        boolean logout = facade.logout(authToken);
        Assertions.assertTrue(logout);
    }


    @Test
    public void logoutNeg() throws Exception {
        Assertions.assertThrows(Exception.class, () ->
        {
            facade.logout("someone");
        });
    }


    @Test
    public void createGamePos() throws Exception {
        String authToken = facade.register("obi wan", "password", "email");
        int gameId = facade.createGame("game", authToken);
        Assertions.assertTrue(gameId != 0);
    }


    @Test
    public void createGameNeg() throws Exception {
        Assertions.assertThrows(Exception.class, () ->
        {
            facade.createGame("this", "0");
        });


    }


    @Test
    public void listGamePos() throws Exception {
        String authToken = facade.register("obi wan", "password", "email");


        Collection<GameData> games = facade.listGames(authToken);
        Assertions.assertNotNull(games);
        Assertions.assertTrue(games.isEmpty());


        facade.createGame("1game", authToken);
        games = facade.listGames(authToken);
        Assertions.assertEquals(1, games.size());
    }


    @Test
    public void listGameNeg() throws Exception {
        Assertions.assertThrows(Exception.class, () ->
        {
            facade.listGames("nogame");
        });
    }


    @Test
    public void joinGamePos() throws Exception {
        String authToken = facade.register("obi wan", "password", "email");
        int gameID = facade.createGame("1game", authToken);


        boolean join = facade.joinGame(gameID, "WHITE", authToken);
        Assertions.assertTrue(join);


    }


    @Test
    public void joinGameNeg() throws Exception {
        String authToken = facade.register("obi wan", "password", "email");
        int gameID = facade.createGame("1game", authToken);


        boolean join = facade.joinGame(gameID, "WHITE", authToken);
        Assertions.assertTrue(join);


        String authToken2 = facade.register("obi two", "password", "email");
        Assertions.assertThrows(Exception.class, () -> facade.joinGame(gameID, "WHITE", authToken2));
    }


    @Test
    public void getGamePos() throws Exception {
        String authToken = facade.register("obi wan", "password", "email");
        int gameID = facade.createGame("1game", authToken);


        GameData game = facade.getGame(gameID, authToken);
        Assertions.assertNotNull(game);
        Assertions.assertEquals(gameID, game.getGameID());


    }


    @Test
    public void getGameNeg() throws Exception {
        String authToken = facade.register("obi wan", "password", "email");
        int gameID = facade.createGame("1game", authToken);
        Assertions.assertThrows(Exception.class, () ->
        {
            facade.getGame(gameID, "not the token");
        });
    }


    @Test
    public void observe() throws Exception {
        String authToken1 = facade.register("obi wan", "password", "email");
        String authToken2 = facade.register("obi two", "password", "email2");


        int gameID = facade.createGame("Mustafar", authToken1);
        GameData game1 = facade.getGame(gameID, authToken1);
        GameData game2 = facade.getGame(gameID, authToken2);
        Assertions.assertNotNull(game2);
        Assertions.assertNotNull(game1);
        Assertions.assertEquals(game1.getGame(), game2.getGame());
        Assertions.assertEquals(game1.getGameName(), game2.getGameName());


    }
}
