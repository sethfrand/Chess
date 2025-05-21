package handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
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
        response.status(200);
        return gson.toJson(new ListGameResponse(games));
    }

    public Object joinGame(Request request, Response response) {
        String authToken = request.headers("authorization");

        if (authToken == null || authToken.isEmpty()) {
            response.status(401);
            return gson.toJson(new ErrorResponse("Error: unauthorized"));
        }
        String username = authService.getUsernameForToken(authToken);
        if (username == null) {
            response.status(401);
            return gson.toJson(new ErrorResponse("Error: unauthorized"));
        }
        try {
            JoinGameRequest joinRequest = gson.fromJson(request.body(), JoinGameRequest.class);
            if (joinRequest == null) {
                response.status(400);
                return gson.toJson(new ErrorResponse("Error: bad request"));
            }

            boolean result = gameService.joinGame(joinRequest.gameID, username, joinRequest.PlayerColor);
            if (result) {
                response.status(200);
                return "";
            } else {
                response.status(403);
                return gson.toJson(new ErrorResponse("Error: already taken"));
            }
        } catch (Exception e) {
            response.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
        }
    }

    public Object createGame(Request request, Response response) {

        String authToken = request.headers("authorization");

        if (authToken == null || authToken.isEmpty()) {
            response.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
        }
        String username = authService.getUsernameForToken(authToken);
        if (username == null) {
            response.status(401);
            return gson.toJson(new ErrorResponse("Error: unauthorzied"));
        }
        try {
            CreateGameRequest createRequest = gson.fromJson(request.body(), CreateGameRequest.class);

            if (createRequest.gameNames == null || createRequest.gameNames.isEmpty()) {
                response.status(400);
                return gson.toJson(new ErrorResponse("Error: bad request"));
            }
            int gameID = gameService.createGame(createRequest.gameNames);
            response.status(200);
            return gson.toJson(new GameIDRegistration(gameID));
        } catch (Exception e) {
            response.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
        }
    }


    private static class JoinGameRequest {
        String PlayerColor;
        String gameID;
    }

    private static class CreateGameRequest {
        String gameNames;
    }

    private static class ListGameResponse {
        Collection<GameData> games;

        ListGameResponse(Collection<GameData> games) {
            this.games = games;
        }

    }

    private static class GameIDRegistration {
        int gameID;

        GameIDRegistration(int gameID) {
            this.gameID = gameID;
        }
    }

    private static class ErrorResponse {
        String message;

        ErrorResponse(String message) {
            this.message = message;
        }
    }
}
