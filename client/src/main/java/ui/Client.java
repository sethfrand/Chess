package ui;

import chess.*;
import model.GameData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import java.util.Collection;
import java.util.Scanner;

public class Client implements WebSocket.Listener {
    private final Scanner scanner;
    private final ServerFacade facade;
    private String authToken;
    private String curUser;
    private ClientState state;
    private GameData curGame;
    private ChessGame.TeamColor team;
    private WebSocket webSocket;
    private final Gson gson = new Gson();

    public Client(String serverURL) {
        this.scanner = new Scanner(System.in);
        this.state = ClientState.LOGGED_OUT;
        this.facade = new ServerFacade(serverURL);
    }

    public static void main(String[] args) {
        String serverURL = "http://localhost:3456";
        if (args.length > 0) {
            serverURL = args[0];
        }
        Client client = new Client(serverURL);
        client.run();
    }

    private void connectWebsocket() throws Exception {
        String socketURL = facade.getServerUrl().replace("http://", "ws://") + "/ws";
        URI uri = URI.create(socketURL);

        HttpClient client = HttpClient.newHttpClient();
        webSocket = client.newWebSocketBuilder()
                .buildAsync(uri, this)
                .join();
    }

    public void webSocketDisconnect() {
        try {
            if (webSocket != null) {
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Closing connection");
            }
        } catch (Exception e) {
            System.out.println("Error disconnecting socket. " + e.getMessage());
        }
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println("WebSocket connection opened");
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        handleSocket(data.toString());
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        System.out.println("Socket closed: " + reason);
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.out.println("Error with socket: " + error.getMessage());
        WebSocket.Listener.super.onError(webSocket, error);
    }

    void handleSocket(String messege) {
        try {
            ServerMessage serverMessage = gson.fromJson(messege, ServerMessage.class);

            switch (serverMessage.getServerMessageType()) {
                case LOAD_GAME -> loadGame(gson.fromJson(messege, LoadGameMessage.class));
                case NOTIFICATION -> notification(gson.fromJson(messege, NotificationMessage.class));
                case ERROR -> handleError(gson.fromJson(messege, ErrorMessage.class));
            }
        } catch (Exception e) {
            System.out.println("error handling socket message " + messege);
        }
    }

    private void handleError(ErrorMessage message) {
        System.out.println("Error... " + message.getErrorMessage());
    }

    private void notification(NotificationMessage message) {
        System.out.println(".... " + message.getMessage());
    }

    public void loadGame(LoadGameMessage messege) {
        if (messege.getGame() != null) {
            curGame = new GameData(curGame.getGameID(), curGame.getWhiteUsername(), curGame.getBlackUsername(),
                    curGame.getGameName(), messege.getGame());
            showBoard(team != null ? team : ChessGame.TeamColor.WHITE);
        }
    }

    public void sendCommand(UserGameCommand command) {
        try {
            if (webSocket != null) {
                String json = gson.toJson(command);
                webSocket.sendText(json, true);
            }
        } catch (Exception e) {
            System.out.println("Error sending the command " + e.getMessage());
        }
    }

    public void run() {
        System.out.println("Welcome to the Chess game");
        System.out.print("Type 'help' to get a list of commands!");
        System.out.println();

        while (true) {
            printPrompt();
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("quit")) {
                System.out.println("Quitting");
                break;
            }
            try {
                processCommand(input);
            } catch (Exception e) {
                System.out.println("Error " + e.getMessage());
            }
        }
    }

    void printPrompt() {
        if (state == ClientState.LOGGED_OUT) {
            System.out.print("Logged out....");
            System.out.println(" ");
        } else {
            System.out.println("[" + curUser + "].....");
        }
    }

    void processCommand(String input) throws Exception {
        String[] string = input.split(" ");
        String command = string[0].toLowerCase();

        //handle commands when logged out
        if (state == ClientState.LOGGED_OUT) {
            logOutCommands(command, string);
        } else if (state == ClientState.LOGGED_IN) {
            logInCommands(command, string);
        } else {
            inGameCommands(command, string);
        }
        //handle commands when logged in
    }

    private void inGameCommands(String command, String[] string) throws Exception {
        switch (command) {
            case ("help") -> inGameHelp();
            case ("redraw") -> redoBoard();
            case ("leave") -> exitGame();
            case ("move") -> makeMove(string);
            case ("resign") -> resignGame();
            case ("highlight") -> highlightSquares(string);
            default -> System.out.println("command " + command + " unknown, type 'help' for a list of commands");
        }
    }

    private void makeMove(String[] parts) throws Exception {
        if (parts.length != 3) {
            System.out.println("incorrect arguments, please use move <from> <to>");
            System.out.println("and example could be move <e3> <e4>");
            return;
        }
        try {
            String from = parts[1].toLowerCase();
            String to = parts[2].toLowerCase();

            ChessPosition fromPos = convertPos(from);
            ChessPosition toPos = convertPos(to);

            if (fromPos == null || toPos == null) {
                System.out.println("invalid, please use the correct format like 'e1' or 'e2' ");
                return;
            }


            ChessPiece piece = curGame.getGame().getBoard().getPiece(fromPos);
            ChessPiece.PieceType promoPiece = null;


            if (piece != null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                boolean promo = false;
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE && toPos.getRow() == 8) {
                    promo = true;
                }
                if (piece.getTeamColor() == ChessGame.TeamColor.BLACK && toPos.getRow() == 1) {
                    promo = true;
                }
                if (promo) {
                    promoPiece = getPromoPiece();
                    if (promoPiece == null) {
                        System.out.println("invalid promo piece");
                    }
                }
            }


            ChessMove move = new ChessMove(fromPos, toPos, null);
            UserGameCommand moveCommand = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, curGame.getGameID(), move);
            sendCommand(moveCommand);

        } catch (Exception e) {
            System.out.println("Error making a move " + e.getMessage());
        }
    }

    private ChessPiece.PieceType getPromoPiece() {
        System.out.println("Choose a piece to Promote");
        System.out.println("Queen");
        System.out.println("Rook");
        System.out.println("Bishop");
        System.out.println("Knight");
        System.out.println("Please enter the first letter of the piece you would like, if no piece is chosen, a queen will be given");

        String newPiece = scanner.nextLine().trim().toUpperCase();

        return switch (newPiece) {
            case "Q" -> ChessPiece.PieceType.QUEEN;
            case "R" -> ChessPiece.PieceType.ROOK;
            case "K" -> ChessPiece.PieceType.KNIGHT;
            case "B" -> ChessPiece.PieceType.BISHOP;
            default -> ChessPiece.PieceType.QUEEN;
        };

    }

    static boolean isValidMove(int row, int col) {
        return (col < 1 || col > 8 || row < 1 || row > 8);
    }

    private ChessPosition convertPos(String from) {
        if (from == null || from.length() != 2) {
            return null;
        }
        char colChar = from.charAt(0);
        char rowChar = from.charAt(1);

        int col = colChar - 'a' + 1;
        int row = rowChar - '0';

        if (isValidMove(row, col)) {
            return null;
        }
        return new ChessPosition(row, col);
    }

    private void highlightSquares(String[] parts) {
        if (parts.length != 2) {
            System.out.println("incorrect arguments, use correct format: highlight <position>");
            System.out.println("Example: highlight e4");
            return;
        }
        try {
            String posString = parts[1].toLowerCase();
            ChessPosition square = convertPos(posString);

            if (square == null) {
                System.out.println("invalid format, use correct format");
                System.out.println("Example: e4");
                return;
            }
            ChessPiece piece = curGame.getGame().getBoard().getPiece(square);
            if (piece == null) {
                System.out.println("There is no piece at " + posString);
                return;
            }
            Collection<ChessMove> moves = curGame.getGame().validMoves(square);
            if (moves.isEmpty()) {
                System.out.println("no valid moves for piece at " + square);
            }

            System.out.println("valid moves for " + piece.getPieceType() + " located on " + square + "");
            for (ChessMove move : moves) {
                String moveStr = positiontoString(move.getEndPosition());
                System.out.println(moveStr + " ");
            }
            System.out.println();
            showBrightBoard(team != null ? team : ChessGame.TeamColor.WHITE, square, moves);
        } catch (Exception e) {
            System.out.println("Error highlighting moves " + e.getMessage());
        }
    }

    private void showBrightBoard(ChessGame.TeamColor perspective, ChessPosition square, Collection<ChessMove> moves) {
        if (curGame != null && curGame.getGame() != null) {
            ChessBoard board = curGame.getGame().getBoard();
            System.out.println();
            Board.showBrightBoard(board, perspective, square, moves);
        }
    }

    private String positiontoString(ChessPosition endPosition) {
        char col = (char) ('a' + endPosition.getColumn() - 1);
        return "" + col + endPosition.getRow();
    }

    private void redoBoard() {
        if (curGame != null) {
            showBoard(team != null ? team : ChessGame.TeamColor.WHITE);
        } else {
            System.out.println("no game to show");
        }
    }

    private void exitGame() {
        if (curGame != null) {
            UserGameCommand leave = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, curGame.getGameID(), null);
            sendCommand(leave);

            webSocketDisconnect();
            curGame = null;
            team = null;
            state = ClientState.LOGGED_IN;
            System.out.println("leaving the game....");
        }
    }

    private void logOutCommands(String command, String[] string) throws Exception {
        switch (command) {
            case ("help") -> logOutHelp();
            case ("login") -> login(string);
            case ("register") -> register(string);
            default -> System.out.println("command " + command + " unknown, type 'help' for a list of commands");
        }
    }

    private void logInCommands(String command, String[] string) throws Exception {
        switch (command) {
            case ("help") -> loginHelp();
            case ("logout") -> logout();
            case ("create") -> createGame(string);
            case ("list") -> listGames();
            case ("join") -> joinGame(string);
            case ("observe") -> observeGame(string);
            default -> System.out.println("command " + command + "unknown, type 'help' for a list of commands");
        }
    }

    private void login(String[] parts) throws Exception {
        if (parts.length != 3) {
            System.out.println("incorrect arguments, please use login <username> <password>");
            return;
        }
        String username = parts[1];
        String password = parts[2];
        authToken = facade.login(username, password);
        if (authToken != null) {
            curUser = username;
            state = ClientState.LOGGED_IN;
            System.out.println("logging in as " + username);
        } else {
            System.out.println("failed login");
        }
    }

    private void register(String[] parts) throws Exception {
        if (parts.length != 4) {
            System.out.println("incorrect arguments, please use register <username> <password> <email> ");
            return;
        }
        String username = parts[1];
        String password = parts[2];
        String email = parts[3];
        authToken = facade.register(username, password, email);
        if (authToken != null) {
            curUser = username;
            state = ClientState.LOGGED_IN;
            System.out.println("registered and logging in as " + username);
        } else {
            System.out.println("failed registration ");
        }
    }

    private void logout() throws Exception {
        if (facade.logout(authToken)) {
            authToken = null;
            curUser = null;
            state = ClientState.LOGGED_OUT;
        } else {
            System.out.println("logout failed ");
        }
    }

    private void createGame(String[] parts) throws Exception {
        if (parts.length < 2) {
            System.out.println("incorrect arguments, please use create <game_name");
            return;
        }
        String gameName = String.join("", java.util.Arrays.copyOfRange(parts, 1, parts.length));
        int gameID = facade.createGame(gameName, authToken);

        if (gameID > 0) {
            System.out.println("game with " + gameID + " created. Feel free to join!");
        } else {
            System.out.println("game creation failed ");
        }
    }

    private void listGames() throws Exception {
        var games = facade.listGames(authToken);

        if (games.isEmpty()) {
            System.out.println("no games");
            return;
        }
        System.out.println("Available games");
        System.out.println("ID | GAME NAME | WHITE PLAYER | BLACK PLAYER");
        for (var game : games) {
            System.out.printf("%d  |     %s    |      %s      | %s%n",
                    game.getGameID(), game.getGameName(),
                    game.getWhiteUsername() != null ? game.getWhiteUsername() : "none",
                    game.getBlackUsername() != null ? game.getBlackUsername() : "none");
        }
    }

    private void joinGame(String[] parts) throws Exception {
        if (parts.length != 3) {
            System.out.println("incorrect arguments, please use join <game_id> <WHITE> or <BLACK>");
            return;
        }
        try {
            int gameID = Integer.parseInt(parts[1]);
            String color = parts[2].toUpperCase();
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                System.out.println("color must be WHITE or BLACK");
                return;
            }
            GameData gameData = facade.getGame(gameID, authToken);
            if (gameData == null) {
                System.out.println("No game");
                return;
            }
            if (color.equals("WHITE") && gameData.getWhiteUsername() != null) {
                System.out.println("The white user is already taken");
                return;
            }
            if (color.equals("BLACK") && gameData.getBlackUsername() != null) {
                System.out.println("The black user is already taken");
                return;
            }
            {
                if (facade.joinGame(gameID, color, authToken)) {
                    curGame = gameData;
                    team = color.equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
                    System.out.println("You have joined " + gameID + " successfully!, you will be playing as " + team);
                    state = ClientState.GAMING;
                    try {
                        connectWebsocket();
                        UserGameCommand join = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
                        sendCommand(join);
                    } catch (Exception e) {
                        System.out.println("Error connecting to the game " + e.getMessage());
                    }
                    showBoard(team);
                } else {
                    System.out.println("You have joined " + gameID + " unsuccessfully!, one of the colors may be taken");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("gameID is invalid, enter a number ");
        } catch (Exception e) {
            System.out.println("error joining game ");
        }
    }

    private void observeGame(String[] parts) {
        if (parts.length != 2) {
            System.out.println("incorrect arguments, please use observe <game_id> ");
            return;
        }
        try {
            int gameID = Integer.parseInt(parts[1]);

            curGame = facade.getGame(gameID, authToken);
            System.out.println("now observing " + gameID);
            state = ClientState.GAMING;
            team = null;
            try {
                connectWebsocket();
                UserGameCommand connect = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
                sendCommand(connect);
            } catch (Exception e) {
                System.out.println("Error connecting to game to observe " + e.getMessage());
            }
            showBoard(ChessGame.TeamColor.WHITE);
        } catch (Exception e) {
            System.out.println("gameID is invalid, enter a number");
        }
    }

    public void resignGame() {
        System.out.println("Please type 'yes' or 'y' to confirm your resignation from the game ");
        try {
            UserGameCommand resign = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, curGame.getGameID());
            sendCommand(resign);
            System.out.println("Resigning from the game");
        } catch (Exception e) {
            System.out.println("Error resigning from game " + e.getMessage());
        }
    }

    private void loginHelp() {
        System.out.println("The available commands are..");
        System.out.println(" create <game_name> --This creates a new game");
        System.out.println(" join <game_id> <WHITE|BLACK> -- This will let you join a game");
        System.out.println(" list - This will last all the games");
        System.out.println(" observe <game_id> -- this allows you to observe a game");
        System.out.println(" logout -- This will log you out");
        System.out.println(" help -- this will display (this) help menu again ");
    }

    private void logOutHelp() {
        System.out.println("The available commands are..");
        System.out.println("  register <username> <password> <email> -- This will register you as a user");
        System.out.println("  help -- this will display (this) help menu again");
        System.out.println("  login <username> <password> -- this will log you in ");
        System.out.println("  quit -- this will kill the program");
    }

    private void inGameHelp() {
        System.out.println("The available commands are..");
        System.out.println("resign");
        System.out.println("redraw");
        System.out.println("leave");
        System.out.println("move");
        System.out.println("highlight");
    }

    private void showBoard(ChessGame.TeamColor team) {
        if (curGame != null && curGame.getGame() != null) {
            ChessBoard board = curGame.getGame().getBoard();
            System.out.println();
            Board.printBoard(board, team);
        } else {
            System.out.println("No board to show");
        }
    }

    private enum ClientState {
        LOGGED_OUT,
        LOGGED_IN,
        GAMING
    }
}
