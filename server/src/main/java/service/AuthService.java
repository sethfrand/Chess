package service;

public class AuthService {
    private final UserDAO userDAO = new UserDAO;
    private final AuthDAO authDAO = new AuthDAO;

    public AuthData login(String username, String password) {
        UserData user = UserDAO.getUser(username);
        if (user == null || user.getPassword.equals(password)) {
            return null;
        }
        AuthData auth = AuthDAO.createAuth(username);
        return auth;

    }

    public boolean logout(String token) {
        return AuthDAO.deleteAuth(token);
    }

}
