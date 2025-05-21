package handler;

import com.google.gson.Gson;
import dataAccess.DataAccessException;
import model.AuthData;
import model.UserData;
import spark.*;
import service.AuthService;
import service.UserService;

public class UserHandler {
    private final Gson gson = new Gson();
    private final AuthService authService = new AuthService();
    private final UserService userService = new UserService();

    public Object register(Request request, Response response) {

        try {
            RegisterRequest registerRequest = gson.fromJson(request.body(), RegisterRequest.class);

            if (registerRequest.username == null || registerRequest.password == null || registerRequest.email == null) {
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
                return gson.toJson(new ErrorResponse("Error: already taken "));
            }
        } catch (RuntimeException | DataAccessException e) {
            response.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));

        }
    }

//        UserData user = new UserData();
//        UserData.setUserName = (RegisterRequeset.username);
//        UserData.setPassword = (RegisterRequeset.password);
//
//        AuthData authData = UserService.register(user);
//
//        if (authData != null) {
//            response.status(200);
//            return gson.toJson(authData);
//        } else {
//            response.status(401);
//            return gson.toJson(new ErrorResponse("Error"));
//        }
//        try {
//            RegisterRequeset registerRequest = gson.fromJson(request.body(), RegisterRequeset.class);
//            response.status(200);
//            return gson.toJson(new SuccessfulResponse("new user registered"));
//        } catch (JsonSyntaxException e) {
//            response.status(401);
//            return gson.toJson(new ErrorResponse("error registering the new user"));
//
//        }


    public Object login(Request request, Response response) {
        try {
            LoginRequest loginRequest = gson.fromJson(request.body(), LoginRequest.class);

            if (loginRequest == null || loginRequest.username == null || loginRequest.password == null) {
                response.status(400);
                return gson.toJson(new ErrorResponse("Error: bad request"));
            }

            AuthData authdata = authService.login(loginRequest.username, loginRequest.password);

            if (authdata == null) {
                response.status(401);
                return gson.toJson(new ErrorResponse("Error: unauthorized"));
            }

            response.status(200);
            response.type("application/json");
            return gson.toJson(authdata);
        } catch (Exception e) {
            response.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
        }
    }

    public Object logout(Request request, Response response) {
        String authToken = request.headers("Authorization");

        if (authToken == null || authToken.isEmpty()) {
            response.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
        }
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
            userService.clear();
            authService.clear();
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
        String email;
    }

    private static class RegisterRequest {
        String username;
        String password;
        String email;
    }
}
