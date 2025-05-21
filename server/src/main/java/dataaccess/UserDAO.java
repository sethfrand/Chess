package dataaccess;

import java.util.HashMap;
import java.util.Map;

import model.UserData;

public class UserDAO {

    private static final Map<String, UserData> USERS = new HashMap<>();

    public void createUser(UserData user) {
        USERS.put(user.getUserName(), user);
    }

    public UserData getUser(String userName) {
        return USERS.get(userName);
    }

    public void clear() {
        USERS.clear();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
