package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthDAO {
    private static final Map<String, String> AuthTokens = new HashMap<>();

    public AuthData createAuth(String username) {
        String authToken = UUID.randomUUID().toString();
        //this will create the random string to be the auth token
        AuthTokens.put(authToken, username);
        return new AuthData(authToken, username);
    }

    public String getUserName(String authToken) {
        return AuthTokens.get(authToken);
    }

    public static Boolean deleteAuth(String authToken) {
        if (AuthTokens.containsKey(authToken)) {
            AuthTokens.remove(authToken);
            return true;
        }
        return false;
    }

    public void clear() {
        AuthTokens.clear();
    }

}
