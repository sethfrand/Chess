package dataaccess;

import model.*;

import javax.xml.crypto.Data;
import java.sql.SQLException;
import java.sql.*;
import java.util.UUID;

public class MySQLAuthDAO {
    public MySQLAuthDAO() throws DataAccessException {
        configureDatabase();

    }

    private final String[] create =
            {
                    "CREATE TABLE IF NOT EXISTS auth(" +
                            "token varchar(255) not null," +
                            "username varchar(255) not null," +
                            "PRIMARY KEY (token)" +
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

    public AuthData createAuth(String username) throws DataAccessException {
        if (username == null || username.trim().isEmpty()) {
            throw new DataAccessException("Username cannot be null or empty");
        }

        String authToken = UUID.randomUUID().toString();
        var statement = "INSERT INTO auth (token, username) VALUES(?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {

            ps.setString(1, authToken);
            ps.setString(2, username);
            ps.executeUpdate();

            return new AuthData(authToken, username);
        } catch (SQLException ex) {
            throw new DataAccessException("Error creating auth token", ex);
        }
    }

    public String getUsername(String authtoken) throws DataAccessException {
        if (authtoken == null || authtoken.trim().isEmpty()) {
            return null;
        }
        try (var connection = DatabaseManager.getConnection()) {
            var statement = "SELECT username FROM auth WHERE token = ?";
            try (var preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setString(1, authtoken);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("username");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Can't retrieve username");
        }
        return null;
    }

    public Boolean deleteAuth(String authToken) throws DataAccessException {
        if (authToken == null || authToken.trim().isEmpty()) {
            return null;
        }

        var statement = "DELETE FROM auth WHERE token = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {

            ps.setString(1, authToken);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            throw new DataAccessException("Error deleting auth token", ex);
        }
    }

    public void clear() throws DataAccessException {
        var sqlStatement = "truncate TABLE auth";
        try (var connection = DatabaseManager.getConnection()) {
            var preparedStatement = connection.prepareStatement(sqlStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to clear the table");
        }

    }
}
