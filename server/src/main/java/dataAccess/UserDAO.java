package dataAccess;

import java.util.HashMap;
import java.util.Map;

import model.UserData;

public class UserDAO {

    private static final Map<String, UserData> users = new HashMap<>();

    public void createUser(UserData user) {
        users.put(user.getUserName(), user);
    }

    public UserData getUser(String userName) {
        return users.get(userName);
    }

    public void clear() {
        users.clear();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
