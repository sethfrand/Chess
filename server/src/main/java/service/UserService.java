package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.*;
import dataaccess.*;

public class UserService {
    //    private final UserDAO userDAO;
//    private final AuthDAO authDAO;
    private final MySQLUserDAO userDAO;
    private final MySQLAuthDAO authDAO;

    public UserService() throws DataAccessException {
        this.userDAO = new MySQLUserDAO();
        this.authDAO = new MySQLAuthDAO();
        userDAO.initialize();
        authDAO.initialize();
    }

    public AuthData register(UserData user) throws DataAccessException {

        if (user == null || user.getUserName() == null || user.getUserName().isEmpty()) {
            throw new DataAccessException("Invalid user data");
        }

        if (userDAO.getUser(user.getUserName()) != null) {
            return null;
        }

        userDAO.createUser(user);
        return authDAO.createAuth(user.getUserName());
    }

    public void clear() throws DataAccessException {
        userDAO.clear();
        authDAO.clear();
    }
}