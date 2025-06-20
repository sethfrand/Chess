package websocket;

import chess.*;
import com.google.gson.Gson;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.AuthService;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


@WebSocket
public class WebSocketHandler {
    private static final ConcurrentHashMap<Integer, CopyOnWriteArrayList<Session>> SESSIONS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, String> SESSION_TO_AUTH = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, Integer> SESSION_TO_GAME = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, Boolean> RESIGNED_GAMES = new ConcurrentHashMap<>();

    private final Gson gson = new Gson();
    private final GameService gameService;
    private final AuthService authService;

    public WebSocketHandler() throws Exception {
        this.gameService = new GameService();
        this.authService = new AuthService();

    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connectionMade(session, command);
                case LEAVE -> leave(session, command);
                case RESIGN -> resign(session, command);
                case MAKE_MOVE -> makeMove(session, command);

            }

        } catch (Exception e) {
            projectError(session, "Error processing " + e.getMessage());
        }
    }

    private void makeMove(Session session, UserGameCommand command) throws Exception {

        String authToken = command.getAuthToken();
        String username = authService.getUsernameForToken(authToken);

        if (username == null) {
            sendError(session, "provided auth token is invalid");
            return;
        }
        int gameID = command.getGameID();
        GameData game = gameService.getGame(gameID);

        if (game == null) {
            sendError(session, "no game exists");
            return;
        }

        ChessGame chessGame = game.getGame();
        if (chessGame.isInCheckmate(ChessGame.TeamColor.WHITE) || chessGame.isInStalemate(ChessGame.TeamColor.WHITE)
                || chessGame.isInCheckmate(ChessGame.TeamColor.BLACK) || chessGame.isInStalemate(ChessGame.TeamColor.BLACK)
                || RESIGNED_GAMES.getOrDefault(gameID, false)) {
            sendError(session, "game is already over, you can't make a move");
            return;
        }

        boolean isWhite = username.equals(game.getWhiteUsername());
        boolean isBlack = username.equals(game.getBlackUsername());

        if (!isWhite && !isBlack) {
            sendError(session, "Error: an observer cannot make a move ");
            return;
        }

        ChessGame.TeamColor nowTurn = chessGame.getTeamTurn();
        ChessGame.TeamColor playerColor = isWhite ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;

        ChessMove move = command.getMove();
        if (move == null) {
            sendError(session, " no move given ");
            return;
        }

        if (nowTurn != playerColor) {
            sendError(session, " not your turn ");
            return;
        }

        try {
            chessGame.makeMove(move);
            GameData updatedGame = new GameData(gameID, game.getWhiteUsername(), game.getBlackUsername(), game.getGameName(), chessGame);
            gameService.updateGame(gameID, updatedGame);

            LoadGameMessage load = new LoadGameMessage(chessGame);
            project(gameID, load);
            ChessGame.TeamColor oppoColor = (playerColor == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE);

            if (chessGame.isInCheckmate(oppoColor)) {
                String oppoUser = (oppoColor == ChessGame.TeamColor.WHITE) ? game.getWhiteUsername() : game.getBlackUsername();
                String mate = oppoUser + " is in checkmate! " + username + " wins the match! ";
                project(gameID, new NotificationMessage(mate));
            } else if (chessGame.isInStalemate(oppoColor)) {
                String stale = "stalemate, there is no winner";
                project(gameID, new NotificationMessage(stale));
            } else if (chessGame.isInCheck(oppoColor)) {
                String oppoUser = (oppoColor == ChessGame.TeamColor.WHITE) ? game.getWhiteUsername() : game.getBlackUsername();
                String check = oppoUser + " is in check ";
                project(gameID, new NotificationMessage(check));
            }

            String description = describeMove(move, chessGame);
            String notifyOthers = username + " just made a move. " + description;
            broadcastNotification(gameID, session, notifyOthers);

        } catch (InvalidMoveException e) {
            sendError(session, " invalid move " + e.getMessage());
        }

    }

    private String describeMove(ChessMove move, ChessGame game) {
        String startSquare = formatPos(move.getStartPosition());
        String endSquare = formatPos(move.getEndPosition());

        String type = "";
        try {
            if (game.getBoard().getPiece(move.getEndPosition()) != null) {
                type = game.getBoard().getPiece(move.getEndPosition()).getPieceType().toString().toLowerCase();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String moveDest = startSquare + " to " + endSquare;
        if (!type.isEmpty()) {
            moveDest = type + " to " + endSquare;
        }

        if (move.getPromotionPiece() != null) {
            moveDest += " (promoted to " + move.getPromotionPiece().toString().toLowerCase() + ")! ";
        }
        return moveDest;
    }

    private String formatPos(ChessPosition position) {
        int row = position.getRow();
        int col = position.getColumn();

        char letter = (char) +('a' + col - 1);
        return letter + String.valueOf(row);
    }

    private void project(int gameID, ServerMessage messege) {
        CopyOnWriteArrayList<Session> gameSession = SESSIONS.get(gameID);
        if (gameSession != null) {
            for (Session session : gameSession) {
                if (session.isOpen()) {
                    sendMessage(session, messege);
                }
            }
        }
    }

    private void resign(Session session, UserGameCommand command) throws Exception {
        String authToken = command.getAuthToken();
        String username = authService.getUsernameForToken(authToken);

        if (username == null) {
            sendError(session, "invalid auth token");
            return;
        }
        int gameID = command.getGameID();
        GameData game;

        try {
            game = gameService.getGame(gameID);
        } catch (Exception e) {
            sendError(session, "Could not load game");
            return;
        }
        boolean isWhite = Objects.equals(username, game.getWhiteUsername());
        boolean isBlack = Objects.equals(username, game.getBlackUsername());

        if (!isWhite && !isBlack) {
            sendError(session, "you aren't a player you can't resign");
            return;
        }

        ChessGame chessGame = game.getGame();
        boolean resigned = game.getWhiteUsername() == null || game.getBlackUsername() == null;
        boolean gameOver = chessGame.isInCheckmate(ChessGame.TeamColor.BLACK) || chessGame.isInCheckmate(ChessGame.TeamColor.WHITE)
                || chessGame.isInStalemate(ChessGame.TeamColor.WHITE) || chessGame.isInStalemate(ChessGame.TeamColor.BLACK)
                || resigned;

        if (gameOver) {
            sendError(session, "Error: game is already over");
            return;
        }

        RESIGNED_GAMES.put(gameID, true);

        String newWhite = game.getWhiteUsername();
        String newBlack = game.getBlackUsername();

        if (isWhite) {
            newWhite = null;
        } else {
            newBlack = null;
        }
        GameData updated = new GameData(gameID, newWhite, newBlack, game.getGameName(), game.getGame());

        try {
            gameService.updateGame(gameID, updated);
        } catch (Exception e) {
            sendError(session, "could not update game");
            return;
        }

        String resignedMessege = username + "resigned from the game";
        NotificationMessage notify = new NotificationMessage(resignedMessege);
        project(gameID, notify);


        //RESIGNED_GAMES.remove(gameID);
        removeSessionFromGame(gameID, session);
        SESSION_TO_AUTH.remove(session);
        SESSION_TO_GAME.remove(session);

    }

    private void leave(Session session, UserGameCommand command) throws Exception {
        String authToken = command.getAuthToken();
        String username = authService.getUsernameForToken(authToken);
        int gameID = command.getGameID();

        if (username != null) {

            GameData game = gameService.getGame(gameID);
            if (game != null) {
                String newWhite = game.getWhiteUsername();
                String newBlack = game.getBlackUsername();
                Boolean player = false;

                if (username.equals(game.getWhiteUsername())) {
                    newWhite = null;
                    player = true;
                }

                if (username.equals(game.getBlackUsername())) {
                    newBlack = null;
                    player = true;
                }

                if (player) {
                    GameData updated = new GameData(gameID, newWhite, newBlack, game.getGameName(), game.getGame());
                    gameService.updateGame(gameID, updated);
                    RESIGNED_GAMES.remove(gameID);
                }
            }
            removeSessionFromGame(gameID, session);
            SESSION_TO_AUTH.remove(session);
            SESSION_TO_GAME.remove(session);
            String leaving = username + " left the game";
            broadcastNotification(gameID, session, leaving);

        }

    }

    private void projectError(Session session, String s) {
        ErrorMessage error = new ErrorMessage(s);
        sendMessage(session, error);
    }


    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
        System.out.println("Websocket connected " + session.getRemoteAddress());
    }

    @OnWebSocketClose
    public void onClose(Session session, int status, String why) {
        String authToken = SESSION_TO_AUTH.remove(session);
        Integer gameId = SESSION_TO_GAME.remove(session);

        if (gameId != null) {
            removeSessionFromGame(gameId, session);

            try {
                String user = authService.getUsernameForToken(authToken);
                if (user != null) {
                    broadcastNotification(gameId, session, user + " left the game");
                }

            } catch (Exception e) {
                System.out.println("disconnected incrrectly " + e.getMessage());
            }
        }
        System.out.println("Websocket disconnected from " + session.getRemoteAddress());
    }


    private void connectionMade(Session session, UserGameCommand command) throws Exception {
        try {
            String authToken = command.getAuthToken();
            String username = authService.getUsernameForToken(authToken);
            if (username == null) {
                sendError(session, "invalid authentication token provided");
                return;
            }

            int gameID = command.getGameID();
            GameData gameData = gameService.getGame(gameID);

            if (gameData == null) {
                sendError(session, "game not found");
                return;
            }

            SESSION_TO_AUTH.put(session, authToken);
            SESSION_TO_GAME.put(session, gameID);
            addSession(gameID, session);
            LoadGameMessage loadMessage = new LoadGameMessage(gameData.getGame());
            sendMessage(session, loadMessage);

            String gameRole = getUserRole(username, gameData);
            String role = "observer".equals(gameRole) ? "an observer " : "a player";
            String playerColor = username.equals(gameData.getWhiteUsername()) ? "white" : "black";
            String notification = username + " has joined the game as a " + role + " playing as " + playerColor;

            broadcastNotification(gameID, session, notification);

        } catch (Exception e) {
            sendError(session, "There was an error connecting to the game " + e.getMessage());
        }
    }

    private String getUserRole(String username, GameData gameData) {
        if (username.equals(gameData.getWhiteUsername()) || username.equals(gameData.getBlackUsername())) {
            return "player";
        } else {
            return "observer";
        }
    }

    private void sendError(Session session, String errorMessage) {
        ErrorMessage error = new ErrorMessage(errorMessage);
        sendMessage(session, error);

    }

    private void addSession(int gameID, Session session) {
        SESSIONS.computeIfAbsent(gameID, i -> new CopyOnWriteArrayList<>()).add(session);
    }

    private void broadcastNotification(Integer gameId, Session excludeSession, String message) {
        NotificationMessage notification = new NotificationMessage(message);
        CopyOnWriteArrayList<Session> gameSession = SESSIONS.get(gameId);
        if (gameSession != null) {
            for (Session session : gameSession) {
                if (session != excludeSession) {
                    sendMessage(session, notification);
                }
            }
        }
    }

    private void sendMessage(Session session, ServerMessage message) {
        try {
            if (session.isOpen()) {
                String json = gson.toJson(message);
                session.getRemote().sendString(json);
            }
        } catch (IOException e) {
            System.out.println("messege sending error " + e.getMessage());
        }
    }

    private void removeSessionFromGame(Integer gameId, Session session) {
        CopyOnWriteArrayList<Session> gameSessions = SESSIONS.get(gameId);
        if (gameSessions != null) {
            gameSessions.remove(session);
            if (gameSessions.isEmpty()) {
                SESSIONS.remove(gameId);
            }

        }

    }
}
