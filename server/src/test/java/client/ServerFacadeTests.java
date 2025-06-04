package client;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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

}
