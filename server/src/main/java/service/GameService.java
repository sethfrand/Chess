package service;

import dataAccess.GameDAO;
import model.GameData;

import java.util.Collection;

public class GameService {
    private final GameDAO gameDAO = new GameDAO();

    public int createGame(String gameName) {
        return gameDAO.createGame(gameName);
    }

    public Collection<GameData> listGames() {
        return gameDAO.listGames();
    }

    public boolean joinGame(String gameIDstr, String username, String playerColor) throws Exception {
        try {
            int gameID = Integer.parseInt(gameIDstr);
            GameData game = gameDAO.getGame(gameID);
            if (game == null) {
                throw new Exception("Game not found");
            }
            if (playerColor == null) {
                return true; // this means that there is no player associated with the desired color
            }

            if (!playerColor.equals("WHITE") && !playerColor.equals("BLACK")) {
                throw new Exception("Invalid player color");
            }

            return gameDAO.takeColor(gameID, username, playerColor);
        } catch (NumberFormatException e) {
            throw new Exception("Invalid game ID format");
        }
    }

    public void clear() {
        GameDAO.clear();
    }
}
