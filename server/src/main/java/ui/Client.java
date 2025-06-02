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

    public ChessClient(String serverURL) {
        this.scanner = new Scanner(System.in);
        this.state = ClientState.LOGGED_OUT;
        this.facade = new ServerFacade(serverURL);
    }

    void run() {
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

        //handle commands when logged in
    }
}
