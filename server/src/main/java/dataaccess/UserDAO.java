package dataaccess;

import java.util.HashMap;
import java.util.Map;

import model.UserData;

public class UserDAO {

    private static final Map<String, UserData> Users = new HashMap<>();

    public void createUser(UserData user) {
        Users.put(user.getUserName(), user);
    }

    public UserData getUser(String userName) {
        return Users.get(userName);
    }

    public void clear() {
        Users.clear();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
