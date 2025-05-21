package dataAccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthDAO {
    private static final Map<String, String> authTokens = new HashMap<>();

    public static AuthData createAuth(String username) {
        String authToken = UUID.randomUUID().toString();
        //this will create the random string to be the auth token
        authTokens.put(authToken, username);
        return new AuthData(authToken, username);
    }

    public String getUserName(String authToken) {
        return authTokens.get(authToken);
    }

    public static Boolean DeleteAuth(String authToken) {
        if (authTokens.containsKey(authToken)) {
            authTokens.remove(authToken);
            return true;
        }
        return false;
    }

    public void clear() {
        authTokens.clear();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
