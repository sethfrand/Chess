package handler;

import com.google.gson.Gson;
import spark.*;
import service.AuthService;

public class UserHandler {
    public class SessionHandler {
        private final Gson gson = new Gson();
        private final AuthService authService = new AuthService();

        public Object login(Request request, Response response) {
            var loginRequest = gson.fromJson(request.body(), loginRequest.class);
            var response = authService.login(loginRequest.username, loginRequest.password);
            if (response == null) {
                res.status(401);
                return "invalid";
            }

            res.status(200);
            res.type("application/json");
            return gson.toJson(response);
        }

        public Object logout(Request request, Response response) {
            var logoutRequest = gson.fromJson(request.body(), logoutRequest.class);
            var response = authService.logout(request.authToken);
            if (result) {
                res.status(200);
                return "";
            } else {
                res.status(401);
                return "Unauthorized";
            }

        }


    }
}
