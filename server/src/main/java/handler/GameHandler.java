package handler;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;

public class GameHandler {
    private final Gson gson = new Gson();


    public Object getGames(Request request, Response response) {
        response.status(200);
        return gson.toJson(new ListGamesResponse());
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
