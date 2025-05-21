package dataAccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GameDAO {

    private static int nextID = 1;
    private static final Map<Integer, GameData> games = new HashMap<>();

    public static void clear() {
        nextID = 1;
        games.clear();
    }

    public int createGame(String gameName) {
        int gameID = nextID++;
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(gameID, null, null, gameName, game);
        games.put(gameID, gameData);

        return gameID;
    }

    public Collection<GameData> listGames() {
        return games.values();
    }

    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    public boolean takeColor(int gameID, String username, String playerColor) {
        GameData gameData = games.get(gameID);
        if (gameData == null) {
            return false;
        }
        if ("WHITE".equals(playerColor)) {
            if (gameData.getWhiteUsername() != null) {
                return false;
            }
            gameData.setWhiteUsername(username);
            return true;
        } else if ("BLACK".equals(playerColor)) {
            if (gameData.getBlackUsername() != null) {
                return false;
            }
            gameData.setBlackUsername(username);
            return true;
        }
        return false;
    }
}
