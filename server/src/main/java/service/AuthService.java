package service;


import dataAccess.UserDAO;
import dataAccess.AuthDAO;
import model.AuthData;
import model.UserData;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();
    private final AuthDAO authDAO = new AuthDAO();

    public AuthData login(String username, String password) {
        UserData user = UserDAO.getUser(username);
        if (user == null || !user.getPassword().equals(password)) {
            return null;
        }
        return AuthDAO.createAuth(username);

    }

    public boolean logout(String token) {
        return AuthDAO.DeleteAuth(token);
    }

}
