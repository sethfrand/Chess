package ui;

import chess.ChessBoard;
import chess.ChessGame;
//import dataaccess.DataAccessException;
import model.GameData;

import java.net.URI;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import websocket.commands.UserGameCommand;
import websocket.messages.*;


import java.util.Scanner;

public class Client extends WebSocketAdapter {
    private final Scanner scanner;
    private final ServerFacade facade;
    private String authToken;
    private String curUser;
    private ClientState state;
    private GameData curGame;
    private ChessGame.TeamColor team;

    private WebSocketClient client;
    private Session session;

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
        if (client == null) {
            client = new WebSocketClient();
            client.start();
        }

        String socketURL = facade.getServerUrl().replace("https://", "ws://") + "/ws";
        URI uriServer = new URI(socketURL);

        session = client.connect(this, uriServer).get();
    }

    public void webSocketDisconnect() {
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
            if (client != null) {
                client.stop();
                client = null;
            }
        } catch (Exception e) {
            System.out.println("Error disconnecting socket. " + e.getMessage());
        }
    }

    @Override
    public void onWebSocketText(String message) {
        handleSocket(message);
    }

    @Override
    public void onWebSocketClose(int session, String why) {
        System.out.println("Socket closed " + why);
    }

    @Override
    public void onWebSocketError(Throwable why) {
        System.out.println("Error with socket " + why.getMessage());
    }


    void handleSocket(String messege) {
        try {
            ServerMessage serverMessage = gson.fromJson(messege, ServerMessage.class);

            switch (serverMessage.getServerMessageType()) {
                case LOAD_GAME -> loadGame(gson.fromJson(messege, LoadGameMessage.class));
                case NOTIFICATION -> notification(gson.fromJson(messege, LoadGameMessage.class));
                case ERROR -> handleError(gson.fromJson(messege, LoadGameMessage.class));
            }
        } catch (Exception e) {
            System.out.println("error handling socket message " + messege);
        }
    }

    public void loadGame(LoadGameMessage messege) {
        if (messege.getGame() != null) {
            curGame = new GameData(curGame.getGameID(), curGame.getWhiteUsername(), curGame.getBlackUsername(),
                    curGame.getGameName(), curGame.getGame());
            showBoard(team != null ? team : ChessGame.TeamColor.WHITE);
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
                System.out.println("Error" + e.getMessage());
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
            case ("move") -> System.out.println("Doing this later");
            case ("resign") -> System.out.print("Nah dude, doing this later");
            case ("highlight") -> System.out.println("this will highlight all the moves that the player can make");
            default -> System.out.println("command " + command + " unknown, type 'help' for a list of commands");
        }
    }

    private void redoBoard() {
        System.out.println("Implementing later");
    }

    private void exitGame() {
        curGame = null;
        team = null;
        state = ClientState.LOGGED_OUT;
        System.out.println("leaving the game....");
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
            System.out.println("failed registration");
        }
    }

    private void logout() throws Exception {
        if (facade.logout(authToken)) {
            authToken = null;
            curUser = null;
            state = ClientState.LOGGED_OUT;
        } else {
            System.out.println("logout failed");
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
            System.out.println("game creation failed");
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
            System.out.printf("%d | %s | %s | %s%n",
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
                    showBoard(team);
                } else {
                    System.out.println("You have joined " + gameID + " unsuccessfully!, one of the colors may be taken");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("gameID is invalid, enter a number");
        } catch (Exception e) {
            System.out.println("error joining game");
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
            showBoard(ChessGame.TeamColor.WHITE);
        } catch (Exception e) {
            System.out.println("gameID is invalid, enter a number");
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
