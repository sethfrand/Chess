package ui;

import java.util.Scanner;

public class Client {
    private final Scanner scanner;
    private final ServerFacade facade;
    private String authToken;
    private String curUser;
    private ClientState state;

    private enum ClientState {
        LOGGED_OUT,
        LOGGED_IN;
    }

    public Client(String serverURL) {
        this.scanner = new Scanner(System.in);
        this.state = ClientState.LOGGED_OUT;
        this.facade = new ServerFacade(serverURL);
    }

    public void run() {
        System.out.println("Welcome to the Chess game");
        System.out.print("Type 'help' to get a list of commands!");

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
                System.out.println("Error");
            }
        }
    }

    void printPrompt() {
        if (state == ClientState.LOGGED_OUT) {
            System.out.println("Logged out....");
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
        } else {
            logInCommands(command, string);
        }
        //handle commands when logged in
    }


    private void logOutCommands(String command, String[] string) throws Exception {
        switch (command) {
            case ("help") -> logOutHelp();
            break;
            case ("login") -> login(string);
            break;
            case ("register") -> register(string);
            break;
            default -> System.out.println("command " + command + "unknown, type 'help' for a list of commands");
        }
    }

    private void logInCommands(String command, String[] string) throws Exception {
        switch (command) {
            case ("help") -> loginHelp();
            break;
            case ("logout") -> logout();
            break;
            case ("create") -> createGame();
            break;
            case ("list") -> listGames();
            break;
            case ("join") -> joinGame();
            break;
            case ("observe") -> observeGame();
            break;
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

    private void register(String[] parts) {
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
            curUser == null;
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
            System.out.println("game with " + gameID + "created. Feel free to join!");
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
            System.out.printf("%d | %s | %s | %sn",
                    game.getGameID(), game.getGameName(),
                    game.getWhiteUsername() != null ? game.getWhiteUsername() : "none",
                    game.getBlackUsername() != null ? game.getBlackUsername() : "none");
        }
    }

    
}
