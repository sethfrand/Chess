package service;


import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import model.AuthData;
import model.UserData;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();
    private final AuthDAO authDAO = new AuthDAO();

    public AuthData login(String username, String password) {
        UserData user = userDAO.getUser(username);
        if (user == null || !user.getPassword().equals(password)) {
            return null;
        }
        //return AuthDAO.createAuth(username);
        return authDAO.createAuth(username);


    }

    public boolean logout(String token) {
        //return AuthDAO.DeleteAuth(token);
        return AuthDAO.deleteAuth(token);
    }

    public String getUsernameForToken(String token) {
        return authDAO.getUserName(token);
    }

    public void clear() {
        authDAO.clear();
    }

}
