package service;


import dataaccess.*;
import model.AuthData;
import model.UserData;

public class AuthService {
    // private final UserDAO userDAO = new UserDAO();
    //private final AuthDAO authDAO = new AuthDAO();
    private final MySQLAuthDAO authDAO;
    private final MySQLUserDAO userDAO;

    public AuthService() throws DataAccessException {
        this.userDAO = new MySQLUserDAO();
        this.authDAO = new MySQLAuthDAO();
        userDAO.initialize();
        authDAO.initialize();
    }

    public AuthData login(String username, String password) throws DataAccessException {
        UserData user = userDAO.verifyUser(username, password);
        if (user == null || !user.getPassword().equals(password)) {
            return null;
        }
        //return AuthDAO.createAuth(username);
        return authDAO.createAuth(username);


    }

    public boolean logout(String token) throws DataAccessException {
        //return AuthDAO.DeleteAuth(token);
        return authDAO.deleteAuth(token);
    }

    public String getUsernameForToken(String token) throws DataAccessException {
        return authDAO.getUsername(token);
    }

    public void clear() throws DataAccessException {
        authDAO.clear();
    }

}
