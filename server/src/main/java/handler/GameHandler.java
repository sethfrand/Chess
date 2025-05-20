package handler;

import com.google.gson.Gson;
import service.AuthService;
import service.GameService;
import spark.Request;
import spark.Response;

import java.util.Collection;

public class GameHandler {
    private final Gson gson = new Gson();
    private final GameService gameService = new GameService();
    private final AuthService authService = new AuthService();


    public Object getGames(Request request, Response response) {
        String authToken = request.headers("authorization");

        if (authToken == null || authToken.isEmpty()) {
            response.status(401);
            return gson.toJson(new ErrorResponse("Error: bad request"));

        }
        String username = authService.getUsernameForToken(authToken);
        if (username == null) {
            response.status(401);
            return gson.toJson(new ErrorResponse("Error: unauthorzied"));
        }
        Collection<GameData> games = gameService.listGames();
        
    }

    public Object joinGame(Request request, Response response) {
        JoinGameRequest joinRequest = gson.fromJson(request.body(), JoinGameRequest.class);
        response.status(200);
        return "";
    }


    public Object createGame(Request request, Response response) {
        CreateGameRequest createRequest = gson.fromJson(request.body(), CreateGameRequest.class);
        response.status(200);
        return "";
    }


    private static class JoinGameRequest {
        String PlayerColor;
        String gameID;
    }

    private static class CreateGameRequest {
        String gameNames;
    }

    private static class ListGamesResponse {

    }

    private static class GameCreatedResponse {
        String gameID;

        GameCreatedResponse(String gameID) {
            this.gameID = gameID;
        }
    }
}
