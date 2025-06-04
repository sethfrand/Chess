package ui;

import chess.ChessBoard;
import chess.ChessGame;
import dataaccess.DataAccessException;
import model.GameData;

import javax.swing.plaf.synth.SynthLookAndFeel;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class Client {
    private final Scanner scanner;
    private final ServerFacade facade;
    private String authToken;
    private String curUser;
    private ClientState state;
    private GameData curGame;
    private ChessGame.TeamColor team;

    private enum ClientState {
        LOGGED_OUT,
        LOGGED_IN,
        GAMING;
    }

    public Client(String serverURL) {
        this.scanner = new Scanner(System.in);
        this.state = ClientState.LOGGED_OUT;
        this.facade = new ServerFacade(serverURL);
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
            inGameCommmands(command, string);

        }
        //handle commands when logged in
    }

    private void inGameCommmands(String command, String[] string) throws Exception {
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

            if (facade.joinGame(gameID, color, authToken)) {
                curGame = facade.getGame(authToken);
                team = color.equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
                System.out.println("You have joined " + gameID + " successfully!, you will be playing as " + team);
                state = ClientState.GAMING;
                showBoard(team);
            } else {
                System.out.println("You have joined " + gameID + " unsuccessfully!");
            }
        } catch (Exception e) {
            System.out.println("gameID is invalid, enter a number");
        }
    }

    private void observeGame(String[] parts) throws DataAccessException {
        if (parts.length != 2) {
            System.out.println("incorrect arguments, please use observe <game_id> ");
            return;
        }

        try {
            int gameID = Integer.parseInt(parts[1]);
            if (facade.joinGame(gameID, null, authToken)) {
                System.out.println("now observing " + gameID);
                state = ClientState.GAMING;
            } else {
                System.out.println("failed to start observing " + gameID);
                showBoard(ChessGame.TeamColor.WHITE);
            }
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

    public static void main(String[] args) {
        String serverURL = "http://localhost:3456";
        if (args.length > 0) {
            serverURL = args[0];
        }
        Client client = new Client(serverURL);
        client.run();
    }


}
