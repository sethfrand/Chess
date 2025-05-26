package dataaccess;

import model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLUserDAO {
    public MySQLUserDAO() throws DataAccessException {
        configureDatabase();

    }

    private final String[] create =
            {
                    "CREATE TABLE IF NOT EXISTS users(" +
                            "username varchar(255) not null," +
                            "password varchar(255) not null," +
                            "email varchar (255) not null," +
                            "PRIMARY KEY (username)" +
                            ")"

            };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var connect = DatabaseManager.getConnection()) {
            for (var table : create) {
                try (var prep = connect.prepareStatement(table)) {
                    prep.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to connect");
        }
        
    }

    public void createUser(UserData user) throws DataAccessException {

        if (user == null || user.getPassword() == null || user.getUserName() == null || user.getEmail() == null) {
            throw new DataAccessException("Invalid data provided");
        }
        var sqlStatement = "Insert into users (username, password, email) VALUES(?,?,?)";
        String hashedPass = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

        try (var connection = DatabaseManager.getConnection();
             var preparedStatement = connection.prepareStatement(sqlStatement)) {
            preparedStatement.setString(1, user.getUserName());
            preparedStatement.setString(2, hashedPass);
            preparedStatement.setString(3, user.getEmail());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DataAccessException("This user already exists");
            }
            throw new DataAccessException("unable to create user");
        }


    }


    public UserData getUser(String user) throws DataAccessException {
        if (user == null || user.trim().isEmpty()) {
            return null;
        }
        try (var connection = DatabaseManager.getConnection()) {
            var sqlStatement = "Select username, password, email FROM users WHERE username = ?";
            try (var preparedStatement = connection.prepareStatement(sqlStatement)) {
                preparedStatement.setString(1, user);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        return readUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("unable to read data");
        }
        return null;
    }


    public UserData verifyUser(String userName, String password) throws DataAccessException {
        UserData user = getUser(userName);
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    public void clear() throws DataAccessException {
        var sqlStatement = "truncate TABLE users";
        try (var connection = DatabaseManager.getConnection()) {
            var preparedStatement = connection.prepareStatement(sqlStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to clear the users");
        }
        ;

    }


    private UserData readUser(ResultSet rs) throws SQLException {
        var userName = rs.getString("username");
        var pass = rs.getString("password");
        var email = rs.getString("email");
        return new UserData(userName, pass, email);

    }
}
