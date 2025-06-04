package ui;

import com.google.gson.Gson;
import model.GameData;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collection;


public class ServerFacade {
    private final String serverUrl;
    private final Gson gson;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
        this.gson = new Gson();
    }

    public String register(String username, String password, String email) throws Exception {
        var path = "/user";
        var request = new RegisterRequest(username, password, email);

        var response = makeRequest("POST", path, request, null, AuthResponse.class);
        return response.authToken();
    }

    public <T> T makeRequest(String method, String path, Object request, String authToken, Class<T> responseClass) throws Exception {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            writeHeads(http, authToken);
            writeBody(request, http);
            http.connect();

            return readBody(http, responseClass);
        } catch (Exception e) {
            throw new Exception("request failed");
        }
    }

    private void writeHeads(HttpURLConnection http, String authToken) {
        if (authToken != null) {
            http.addRequestProperty("Authorization", authToken);
        }
        http.addRequestProperty("Content-Type", "application/json");
    }

    private void writeBody(Object request, HttpURLConnection http) throws Exception {
        if (request != null) {
            String requestData = gson.toJson(request);
            try (OutputStream body = http.getOutputStream()) {
                body.write(requestData.getBytes());
            }
        }
    }

    public String login(String username, String password) throws Exception {
        var path = "/session";
        var request = new LoginRequest(username, password);

        var response = makeRequest("POST", path, request, null, AuthResponse.class);
        return response.authToken();
    }

    public boolean logout(String authtoken) throws Exception {
        var path = "/session";
        makeRequest("DELETE", path, null, authtoken, null);
        return true;
    }

    public int createGame(String gameName, String authToken) throws Exception {
        var path = "/game";
        var request = new CreateGameRec(gameName);

        var response = makeRequest("POST", path, request, authToken, CreateGameResponse.class);
        return response.gameID;
    }

    public boolean joinGame(int gameID, String playerColor, String authToken) throws Exception {
        var path = "/game";
        var request = new JoinGameRequest(String.valueOf(gameID), playerColor);

        makeRequest("PUT", path, request, authToken, null);
        return true;
    }


    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws Exception {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream bodyres = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(bodyres);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private void notSuccessful(HttpURLConnection http) throws Exception {
        var status = http.getResponseCode();
        if (!successful(status)) {
            String error = "request failed";

            try (InputStream bodyRes = http.getErrorStream()) {
                if (bodyRes != null) {
                    InputStreamReader reader = new InputStreamReader(bodyRes);
                    var responseError = gson.fromJson(reader, ErrorResponse.class);
                    if (responseError != null && responseError.message() != null) {
                        error = responseError.message();
                    }
                }
            } catch (Exception e) {
                System.out.println("error");
            }
            throw new Exception(error);
        }
    }

    private boolean successful(int status) {
        return status / 100 == 2;
    }

    public Collection<GameData> listGames(String authToken) throws Exception {
        var path = "/game";
        var response = makeRequest("GET", path, null, authToken, ListGamesResponse.class);
        return response.games();
    }


    private record RegisterRequest(String username, String password, String email) {
    }

    private record LoginRequest(String username, String password) {
    }

    private record AuthResponse(String authToken, String username) {
    }

    private record JoinGameRequest(String gameID, String playerColor) {
    }

    private record CreateGameRec(String gameName) {
    }

    private record CreateGameResponse(int gameID) {
    }

    private record ErrorResponse(String message) {
    }

    private record ListGamesResponse(Collection<GameData> games) {
    }
}

