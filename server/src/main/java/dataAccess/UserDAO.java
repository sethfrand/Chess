package dataAccess;

import java.util.Collection;

public class UserDAO {

    private final Collection<String, UserData> users = new Collection<>();

    public void createUser(UserData user) {
        users.put(user.getUsername(), user);
    }

    public UserData getUser(String userName) {
        return users.get(userName);
    }

    public void clear() {
        users.clear();
    }
}
