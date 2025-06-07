package WebSocket;

import chess.ChessGame;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


@WebSocket
public class WebSocketHandler {
    private static final ConcurrentHashMap<Integer, CopyOnWriteArrayList<Session>> sessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, String> sessionToAuth = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, Integer> sessionToGame = new ConcurrentHashMap<>();

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
                || chessGame.isInCheckmate(ChessGame.TeamColor.BLACK) || chessGame.isInStalemate(ChessGame.TeamColor.BLACK)) {
            sendError(session, "game is already over, you can't make a move");
            return;
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
        GameData game = gameService.getGame(gameID);

        if (game == null) {
            sendError(session, "game does not exist");
            return;
        }

        if (!username.equals(game.getWhiteUsername()) && !username.equals(game.getBlackUsername())) {
            sendError(session, "you are not a player, you cant resign");
            return;
        }

        ChessGame chessGame = game.getGame();

        if (chessGame.isInCheckmate(ChessGame.TeamColor.WHITE) || chessGame.isInStalemate(ChessGame.TeamColor.WHITE)
                || chessGame.isInCheckmate(ChessGame.TeamColor.BLACK) || chessGame.isInStalemate(ChessGame.TeamColor.BLACK)) {
            sendError(session, "game is already over, you can't resign");
            return;
        }

        String resigned = "the player " + username + " has resigned from the game";
        broadcastNotification(gameID, session, resigned);

        NotificationMessage notify = new NotificationMessage("you have resigned from " + session);
        sendMessage(session, notify);

    }

    private void leave(Session session, UserGameCommand command) throws Exception {
        String authToken = command.getAuthToken();
        String username = authService.getUsernameForToken(authToken);
        int gameID = command.getGameID();

        if (username != null) {
            removeSessionFromGame(gameID, session);
            sessionToAuth.remove(session);
            sessionToGame.remove(session);
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
        String authToken = sessionToAuth.remove(session);
        Integer gameId = sessionToGame.remove(session);

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

            sessionToAuth.put(session, authToken);
            sessionToGame.put(session, gameID);
            addSession(gameID, session);
            LoadGameMessage loadMessage = new LoadGameMessage(gameData.getGame());
            sendMessage(session, loadMessage);

            String gameRole = getUserRole(username, gameData);
            String role = "observer".equals(gameRole) ? "an observer " : "a player";
            String notification = username + " has joined the game as a " + role;

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
        sessions.computeIfAbsent(gameID, i -> new CopyOnWriteArrayList<>()).add(session);
    }

    private void broadcastNotification(Integer gameId, Session ExcludeSession, String message) {
        NotificationMessage notification = new NotificationMessage(message);
        CopyOnWriteArrayList<Session> gameSession = sessions.get(gameId);
        if (gameSession != null) {
            for (Session session : gameSession) {
                if (session != ExcludeSession) {
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
        CopyOnWriteArrayList<Session> gameSessions = sessions.get(gameId);
        if (gameSessions != null) {
            gameSessions.remove(session);
            if (gameSessions.isEmpty()) {
                sessions.remove(gameId);
            }

        }

    }
}
