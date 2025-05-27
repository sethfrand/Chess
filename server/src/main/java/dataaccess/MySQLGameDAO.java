package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.*;

import java.sql.SQLException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.sql.Statement;

public class MySQLGameDAO {
    public MySQLGameDAO() throws DataAccessException {
        configureDatabase();

    }

    private final String[] create =
            {
                    "CREATE TABLE IF NOT EXISTS games(" +
                            "gameID int NOT NULL AUTO_INCREMENT," +
                            "WhiteUsername varchar(255) not null," +
                            "BlackUsername varchar(255)," +
                            "gameName varchar(255)," +
                            "game TEXT NOT NULL," +
                            "PRIMARY KEY (gameID)" +
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

    public int createGame(String gameName) throws DataAccessException {
        if (gameName == null || gameName.trim().isEmpty()) {
            throw new DataAccessException("Name of game cannot be empty or null");
        }

        ChessGame game = new ChessGame();
        var statement = "INSERT INTO games (WhiteUsername, BlackUsername, gameName, game) VALUES(?,?,?,?)";
        try (var connection = DatabaseManager.getConnection();
             var preparedStatement = connection.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, null);
            preparedStatement.setString(2, null);
            preparedStatement.setString(3, gameName);
            preparedStatement.setString(4, new Gson().toJson(game));

            preparedStatement.executeUpdate();

            try (var rs = preparedStatement.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            throw new DataAccessException("Failed to generate gameID");
        } catch (SQLException e) {
            throw new DataAccessException("Error creating game");
        }
    }

    public Collection<GameData> listGames() throws DataAccessException {
        var result = new ArrayList<GameData>();
        try (var connection = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, WhiteUsername, BlackUserName,gameName,game FROM games";
            try (var preparedStatement = connection.prepareStatement(statement);
                 var rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    result.add(readGame(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing games");
        }
        return result;
    }

    public GameData getGame(int gameID) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, WhiteUsername, BlackUserName,gameName,game FROM games WHERE gameID = ?";
            try (var preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setInt(1, gameID);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        return readGame(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting game");
        }
        return null;
    }

    public Boolean setColor(int gameID, String username, String playerColor) throws DataAccessException {
        if (username == null || username.trim().isEmpty() || playerColor == null) {
            return false;
        }

        GameData gameData = getGame(gameID);
        if (gameData == null) {
            return false;
        }

        String colName;
        String currPlayer;

        if ("WHITE".equals(playerColor)) {
            colName = "whiteUsername";
            currPlayer = gameData.getWhiteUsername();
        } else if ("BLACK".equals(playerColor)) {
            colName = "blackUsername";
            currPlayer = gameData.getBlackUsername();
        } else {
            return false;
        }

        if (currPlayer != null) {
            return false; //this means that the color is already taken
        }

        var updateStatement = STR."UPDATE game SET \{colName}? WHERE gameID = ?";
        try (var connection = DatabaseManager.getConnection();
             var preparedStatement = connection.prepareStatement(updateStatement)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, String.valueOf(gameID));

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating the game");
        }
    }

    public void clear() throws DataAccessException {
        var sqlStatement = "truncate TABLE games";
        try (var connection = DatabaseManager.getConnection()) {
            var preparedStatement = connection.prepareStatement(sqlStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to clear games table");
        }

    }

    private GameData readGame(ResultSet rs) throws DataAccessException {
        int gameID = rs.getInt("gameID");
        String whiteUsername = rs.getString("whiteUsername");
        String blackUsername = rs.getString("blackUsername");
        String gameName = rs.getString("gameName");
        String gameJson = rs.getString("game");

        ChessGame game = new Gson().fromJson(gameJson, ChessGame.class);
        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);

    }
}