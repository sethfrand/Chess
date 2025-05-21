package handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dataAccess.DataAccessException;
import model.AuthData;
import model.UserData;
import service.GameService;
import spark.*;
import service.AuthService;
import service.UserService;

import java.util.Map;
import java.util.Set;

public class UserHandler {
    private final Gson gson = new Gson();
    private final AuthService authService = new AuthService();
    private final UserService userService = new UserService();
    private final GameService gameService = new GameService();

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
            LoginRequest loginRequest = gson.fromJson(request.body(), LoginRequest.class);

            // Check if the loginRequest is null or has null/empty fields
            if (loginRequest == null || loginRequest.username == null || loginRequest.password == null ||
                    loginRequest.username.trim().isEmpty() || loginRequest.password.trim().isEmpty()) {
                response.status(400);
                return gson.toJson(new ErrorResponse("Error: bad request"));
            }

            AuthData authData = authService.login(loginRequest.username, loginRequest.password);

            if (authData == null) {
                response.status(401);
                return gson.toJson(new ErrorResponse("Error: unauthorized"));
            }

            response.status(200);
            return gson.toJson(authData);
        } catch (JsonSyntaxException e) {
            response.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
        } catch (Exception e) {
            response.status(500);
            return gson.toJson(new ErrorResponse("Error: server error"));
        }
    }

    public Object logout(Request request, Response response) {
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