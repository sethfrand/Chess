package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.*;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService() {
        this.userDAO = new UserDAO();
        this.authDAO = new AuthDAO();
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

    public void clear() {
        userDAO.clear();
        authDAO.clear();
    }
}