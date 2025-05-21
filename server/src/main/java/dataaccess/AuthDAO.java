package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthDAO {
    private static final Map<String, String> AUTH_TOKENS = new HashMap<>();

    public AuthData createAuth(String username) {
        String authToken = UUID.randomUUID().toString();
        //this will create the random string to be the auth token
        AUTH_TOKENS.put(authToken, username);
        return new AuthData(authToken, username);
    }

    public String getUserName(String authToken) {
        return AUTH_TOKENS.get(authToken);
    }

    public static Boolean deleteAuth(String authToken) {
        if (AUTH_TOKENS.containsKey(authToken)) {
            AUTH_TOKENS.remove(authToken);
            return true;
        }
        return false;
    }

    public void clear() {
        AUTH_TOKENS.clear();
    }

}
