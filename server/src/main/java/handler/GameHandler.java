package handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.GameData;
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
            return gson.toJson(new ErrorResponse("Error: unauthorized"));
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
//            JoinGameRequest joinRequest = gson.fromJson(request.body(), JoinGameRequest.class);
//            if (joinRequest == null) {
//                response.status(400);
//                return gson.toJson(new ErrorResponse("Error: bad request"));
            JsonObject joinJsonRequest = gson.fromJson(request.body(), JsonObject.class);

            if (joinJsonRequest == null || !joinJsonRequest.has("gameID")
                    || !joinJsonRequest.has("playerColor")) {
                response.status(400);
                return gson.toJson(new ErrorResponse("Error: bad request"));
            }

            String gameStrID = joinJsonRequest.get("gameID").getAsString();
            String playerColor = null;

            if (joinJsonRequest.has("playerColor")) {
                playerColor = joinJsonRequest.get("playerColor").getAsString().toUpperCase();
                if (!playerColor.equals("WHITE") && !playerColor.equals("BLACK")) {
                    response.status(400);
                    return gson.toJson(new ErrorResponse("Error: bad request"));
                }
            }

            boolean result = gameService.joinGame(gameStrID, username, playerColor);
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
            response.status(401);
            return gson.toJson(new ErrorResponse("Error: unauthorized"));
        }
        String username = authService.getUsernameForToken(authToken);
        if (username == null) {
            response.status(401);
            return gson.toJson(new ErrorResponse("Error: unauthorized"));
        }
        try {
            CreateGameRequest createRequest = gson.fromJson(request.body(), CreateGameRequest.class);

            if (createRequest.gameName == null || createRequest.gameName.isEmpty()) {
                response.status(400);
                return gson.toJson(new ErrorResponse("Error: bad request"));
            }
            int gameID = gameService.createGame(createRequest.gameName);
            response.status(200);
            return gson.toJson(new GameIDRegistration(gameID));
        } catch (Exception e) {
            response.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
        }
    }


    private static class JoinGameRequest {
        String playerColor;
        String gameID;
    }

    private static class CreateGameRequest {
        String gameName;
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
