package handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import service.GameService;
import spark.*;
import service.AuthService;
import service.UserService;

import java.util.Set;

public class UserHandler {
    private final Gson gson = new Gson();
    //    private final AuthService authService = new AuthService();
//    private final UserService userService = new UserService();
//    private final GameService gameService = new GameService();
    private final AuthService authService;
    private final UserService userService;
    private final GameService gameService;

    public UserHandler() {
        try {
            this.authService = new AuthService();
            this.userService = new UserService();
            this.gameService = new GameService();
        } catch (DataAccessException e) {
            throw new RuntimeException("service initialization failed");
        }
    }

    public Object register(Request request, Response response) {
        try {
            RegisterRequest registerRequest = gson.fromJson(request.body(), RegisterRequest.class);

            if (registerRequest == null || registerRequest.username == null
                    || registerRequest.password == null
                    || registerRequest.email == null
                    || registerRequest.username.isEmpty()
                    || registerRequest.password.isEmpty()
                    || registerRequest.email.isEmpty()) {
                response.status(400);
                return gson.toJson(new ErrorResponse("Error: bad request"));
            }
            UserData user = new UserData(registerRequest.username, registerRequest.password, registerRequest.email);
            AuthData authData = userService.register(user);

            if (authData != null) {
                response.status(200);
                return gson.toJson(authData);
            } else {
                response.status(403);
                return gson.toJson(new ErrorResponse("Error: already taken"));
            }
        } catch (RuntimeException | DataAccessException e) {
            response.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
        }
    }

    public Object login(Request request, Response response) {
        try {
            JsonObject loginRequestJson = gson.fromJson(request.body(), JsonObject.class);
            if (loginRequestJson == null) {
                response.status(400);
                return gson.toJson(new ErrorResponse("Error: bad request"));
            }

            // Ensure only "username" and "password" fields are present
            Set<String> keys = loginRequestJson.keySet();
            if (!keys.equals(Set.of("username", "password"))) {
                response.status(400);
                return gson.toJson(new ErrorResponse("Error: bad request"));
            }

            String username = loginRequestJson.has("username") ? loginRequestJson.get("username").getAsString() : null;
            String password = loginRequestJson.has("password") ? loginRequestJson.get("password").getAsString() : null;

            if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
                response.status(400);
                return gson.toJson(new ErrorResponse("Error: bad request"));
            }

            AuthData authData = authService.login(username, password);
            if (authData == null) {
                response.status(401);
                return gson.toJson(new ErrorResponse("Error: unauthorized"));
            }

            response.status(200);
            return gson.toJson(authData);
        } catch (Exception e) {
            response.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
        }
    }

    public Object logout(Request request, Response response) throws DataAccessException {
        String authToken = request.headers("Authorization");

        if (authToken == null || authToken.isEmpty()) {
            response.status(401);
            return gson.toJson(new ErrorResponse("Error: unauthorized"));
        }
        boolean result = authService.logout(authToken);
        if (result) {
            response.status(200);
            return "";
        } else {
            response.status(401);
            return gson.toJson(new ErrorResponse("Error: unauthorized"));
        }
    }

    public Object clear(Request request, Response response) {
        try {
            userService.clear();
            authService.clear();
            gameService.clear();
            response.status(200);
            return "";
        } catch (Exception e) {
            response.status(500);
            return gson.toJson(new ErrorResponse("Error: server error"));
        }
    }

    private static class ErrorResponse {
        String message;

        ErrorResponse(String message) {
            this.message = message;
        }
    }

    private static class SuccessfulResponse {
        String message;

        SuccessfulResponse(String message) {
            this.message = message;
        }
    }

    private static class LoginRequest {
        String username;
        String password;
    }

    private static class RegisterRequest {
        String username;
        String password;
        String email;
    }
}