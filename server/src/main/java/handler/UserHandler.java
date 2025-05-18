package handler;

import com.google.gson.Gson;
import service.AuthService;

public class UserHandler
{
    public class SessionHandler
    {
        private final Gson gson = new Gson();
        private final AuthService authService = new AuthService();


    }
}
