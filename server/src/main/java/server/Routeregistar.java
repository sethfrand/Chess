package server;

import handler.*;
import com.google.gson.Gson;

import static spark.Spark.*;


public class Routeregistar {

    public void initRout() // this is to set up the routing for the server, we will use port 3456
    {
        Gson gson = new Gson();
        UserHandler userHandler = new UserHandler();

        post("/user", userHandler::register);
        post("/session", userHandler::login);
        delete("/session", userHandler::logout);
        get("/game", gameHandler::getGames);
        get("/game", gameHandler::joinGame);
        get("/game", gameHandler::createGame);

        delete("/db", userHandler::clear);
    }
}
