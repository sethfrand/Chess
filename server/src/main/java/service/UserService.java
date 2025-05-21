package service;

import dataAccess.AuthDAO;
import dataAccess.UserDAO;
import model.*;

public class UserService {
    private static final UserDAO userDAO = new UserDAO();
    private final AuthDAO authDAO = new AuthDAO();

    public static AuthData register(UserData user) {
        if (UserDAO.getUser(user.getUserName()) != null) {
            return null; //this means that the user alreadt exists int he system
        }
        userDAO.createUser(user); //create the user

        return AuthDAO.createAuth(user.getUserName());
    }

    public static void clear() {
        userDAO.clear();
    }
}
