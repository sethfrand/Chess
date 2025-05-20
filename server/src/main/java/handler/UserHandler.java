package handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import model.AuthData;
import spark.*;
import service.AuthService;

public class UserHandler {
    private final Gson gson = new Gson();
    private final AuthService authService = new AuthService();

    public Object register(Request request, Response response) {
        try {
            RegisterRequeset registerRequest = gson.fromJson(request.body(), RegisterRequeset.class);
            response.status(200);
            return gson.toJson(new SuccessfulResponse("new user registered"));
        } catch (JsonSyntaxException e) {
            response.status(401);
            return gson.toJson(new ErrorResponse("error registering the new user"));

        }

    }

    public Object login(Request request, Response response) {
        LoginRequest loginRequest = gson.fromJson(request.body(), LoginRequest.class);
        AuthData authdata = authService.login(loginRequest.username, loginRequest.password);
        if (authdata == null) {
            response.status(401);
            return gson.toJson(new ErrorResponse("unsucccessful login"));
        }

        response.status(200);
        response.type("application/json");
        return gson.toJson(authdata);
    }

    public Object logout(Request request, Response response) {
        String authToken = request.headers("Authorization");
        boolean result = authService.logout(authToken);
        if (result) {
            response.status(200);
            return "";
        } else {
            response.status(401);
            return gson.toJson(new ErrorResponse("Unauthorized"));
        }
    }

    public Object clear(Request request, Response response) {
        try {
            response.status(200);
            return gson.toJson(new SuccessfulResponse("database cleared"));
        } catch (Exception e) {
            response.status(401);
            return gson.toJson(new ErrorResponse("error encounted clearing the database"));
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

    private static class RegisterRequeset {
        String username;
        String password;
        String email;
    }
}
