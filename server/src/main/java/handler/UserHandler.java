package handler;

import com.google.gson.Gson;
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
                response.status(200);
                return gson.toJson(new ErrorResponse("Error: bad request"));
            }
            UserData user = new UserData(registerRequest.username, registerRequest.password, registerRequest.email);
            AuthData authData = UserService.register(user);

            if (authData != null) {
                response.status(200);
                return gson.toJson(authData);
            } else {
                response.status(403);
                return gson.toJson(new ErrorResponse("Error: already taken "));
            }
        } catch (RuntimeException e) {
            response.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));

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

    }

    public Object login(Request request, Response response) {
        LoginRequest loginRequest = gson.fromJson(request.body(), LoginRequest.class);
        AuthData authdata = authService.login(loginRequest.username, loginRequest.password);

        if (loginRequest.username == null || loginRequest.password == null) {
            response.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
        }

        if (authdata == null) {
            response.status(401);
            return gson.toJson(new ErrorResponse("Error: unauthorized"));
        }

        response.status(200);
        response.type("application/json");
        return gson.toJson(authdata);
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
            UserService.clear();
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
    }

    private static class RegisterRequest {
        static String username;
        static String password;
        String email;
    }
}
