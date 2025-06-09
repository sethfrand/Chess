package server;

import WebSocket.WebSocketHandler;
import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.

        //This line initializes the server and can be removed once you have a functioning endpoint

        try {
            WebSocketHandler socketHandler = new WebSocketHandler();
            Spark.webSocket("/ws", socketHandler);

        } catch (Exception e) {
            System.out.println("initialization of socket failed " + e.getMessage());
        }

        new Routeregistar().initRout();
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
