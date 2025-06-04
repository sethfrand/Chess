package client;

import org.junit.jupiter.api.*;
import server.Server;
import ui.ServerFacade;

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

    }

    @Test
    public void logoutNeg() throws Exception {

    }

    @Test
    public void createGamePos() throws Exception {

    }

    @Test
    public void createGameNeg() throws Exception {

    }

    @Test
    public void listGamePos() throws Exception {

    }

    @Test
    public void listGameNeg() throws Exception {

    }

    @Test
    public void joinGamePos() throws Exception {

    }

    @Test
    public void joinGameNeg() throws Exception {

    }

    @Test
    public void getGamePos() throws Exception {

    }

    @Test
    public void getGameNeg() throws Exception {

    }

    @Test
    public void observe() throws Exception {

    }

    @Test
    public void fullTest() throws Exception {
        
    }
}
