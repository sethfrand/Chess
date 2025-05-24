package dataaccess;

import model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.sql.*;

public class MySQLUserDAO {
    public MySQLUserDAO() throws SQLException {
        configureDatabase();

    }

    private final String[] create =
            {
                   "CREATE TABLE IF NOT EXIST users(" +
                           "'username' varchar(255) not null," +
                           "'password' varchar(255) not null," +
                           "'email' varchar (255) not null," +
                           "PRIMARY KEY ('username')" +
                           ")"

            }

    private void configureDatabase() throws DataAccessException, SQLException {
        DatabaseManager.createDatabase();
        try (var connect = DatabaseManager.getConnection())
        {
            for (var table : create)
            {
                var prep = connect.prepareStatement(table);
                {
                    prep.executeQuery();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to connect");
        }


    }

    public void createUser(UserData user) throws DataAccessException, SQLException {
        var sqlStatement = "Insert into users (username, password, email) VALUES(?,?,?)";

        String hashedPass = BCrypt.hashpw(user.getPassword(),BCrypt.gensalt());

        try(var connection = DatabaseManager.getConnection();
        var preparedStatement = connection.prepareStatement(sqlStatement)){
            preparedStatement.setString(1, user.getUserName());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());

            preparedStatement.executeQuery();
        }catch (SQLException e)
        {
            if(e.getErrorCode() == 1062)
            {
                throw new DataAccessException("This user already exists");
            }
            throw new DataAccessException("unable to create user");
        }


    }
}
