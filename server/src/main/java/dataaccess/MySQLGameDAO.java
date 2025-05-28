package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.SQLException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class MySQLGameDAO {
    private final Gson gson = new Gson();

    public MySQLGameDAO() {
    }

    public void initialize() throws DataAccessException {
        configureDatabase();
    }

    private final String[] create =
            {
                    "CREATE TABLE IF NOT EXISTS games(" +
                            "gameID int NOT NULL AUTO_INCREMENT," +
                            "whiteUsername varchar(255)," +
                            "blackUsername varchar(255)," +
                            "gameName varchar(255) NOT NULL," +
                            "game TEXT NOT NULL," +
                            "PRIMARY KEY (gameID))"
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
            throw new DataAccessException("Unable to create database");
        }
    }

    public int createGame(String gameName) throws DataAccessException {
        if (gameName == null || gameName.trim().isEmpty()) {
            throw new DataAccessException("game name cannot be empty or null");
        }

        var statement = "INSERT INTO games (gameName, game) VALUES(?,?)";
        ChessGame game = new ChessGame();
        String jsonGame = gson.toJson(game);

        try (var connection = DatabaseManager.getConnection();
             var preparedStatement = connection.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, gameName);
            preparedStatement.setString(2, jsonGame);
            preparedStatement.executeUpdate();

            try (var rs = preparedStatement.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            throw new DataAccessException("Failed to generate game ID");


        } catch (SQLException e) {
            throw new DataAccessException("error creating game");

        }
    }

    public Collection<GameData> allGames() throws DataAccessException {
        var result = new ArrayList<GameData>();
        try (var connection = DatabaseManager.getConnection()) {
            var sqlStatement = "SELECT gameID, whiteUsername, blackUsername, gameName, game from games";
            try (var preparedStatment = connection.prepareStatement(sqlStatement)) {
                try (var rs = preparedStatment.executeQuery()) {
                    while (rs.next()) {
                        int gameId = rs.getInt("gameID");
                        String whiteUsername = rs.getString("whiteUsername");
                        String blackUsername = rs.getString("blackUsername");
                        String gameName = rs.getString("gameName");
                        String gameJson = rs.getString("game");


                        ChessGame game = gson.fromJson(gameJson, ChessGame.class);
                        result.add(new GameData(gameId, whiteUsername, blackUsername, gameName, game));


                    }
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("error listing all games");
        }
        return result;
    }

    public GameData getGame(int gameID) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            var sqlStatement = "SELECT gameID, whiteUsername, blackUsername, gameName, game from games WHERE gameID = ?";
            try (var preparedStatment = connection.prepareStatement(sqlStatement)) {
                preparedStatment.setInt(1, gameID);
                try (var rs = preparedStatment.executeQuery()) {
                    if (rs.next()) {
                        String whiteUsername = rs.getString("whiteUsername");
                        String blackUsername = rs.getString("blackUsername");
                        String gameName = rs.getString("gameName");
                        String gameJson = rs.getString("game");

                        ChessGame game = gson.fromJson(gameJson, ChessGame.class);
                        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("error getting desired game");
        }
        return null;
    }

    public Boolean updateGame(int gameID, GameData gameData) throws DataAccessException {
        var sqlStatement = "UPDATE games SET whiteUsername = ?, blackUsername = ?, game = ? WHERE gameID =?";
        String jsonGame = gson.toJson(gameData.getGame());
        try (var connection = DatabaseManager.getConnection();
             var preparedStatement = connection.prepareStatement(sqlStatement)) {
            preparedStatement.setString(1, gameData.getWhiteUsername());
            preparedStatement.setString(2, gameData.getBlackUsername());
            preparedStatement.setString(3, jsonGame);
            preparedStatement.setInt(4, gameID);


            int rows = preparedStatement.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game");
        }
    }

    public Boolean join(int gameID, String username, String playColor) throws DataAccessException {
        GameData curGame = getGame(gameID);
        if (curGame == null) {
            return false;
        }

        String newWhiteUsername = curGame.getWhiteUsername();
        String newBlackUsername = curGame.getBlackUsername();


        if ("WHITE".equals(playColor)) {
            if (curGame.getWhiteUsername() != null) {
                return false;
            }
        }
        newWhiteUsername = username;

        if ("BLACK".equals(playColor)) {
            if (curGame.getBlackUsername() != null) {
                return false;
            }
            newBlackUsername = username;
        } else {
            return false;
        }

        GameData updatedGame = new GameData(
                curGame.getGameID(), curGame.getWhiteUsername(), curGame.getBlackUsername(),
                curGame.getGameName(), curGame.getGame());

        return updateGame(gameID, updatedGame);
    }

    public void clear() throws DataAccessException {
        var sqlStatement = "truncate TABLE games";
        try (var connection = DatabaseManager.getConnection()) {
            var preparedStatement = connection.prepareStatement(sqlStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to clear the games");
        }
    }
}
