package ui;

public class prompts {
    public static void printPrompt(ClientState state, String curUser) {
        if (state == ClientState.LOGGED_OUT) {
            System.out.print("Logged out....");
            System.out.println(" ");
        } else {
            System.out.println("[" + curUser + "].....");
        }
    }

    public static void loginHelp() {
        System.out.println("The available commands are..");
        System.out.println(" create <game_name> --This creates a new game");
        System.out.println(" join <game_id> <WHITE|BLACK> -- This will let you join a game");
        System.out.println(" list - This will last all the games");
        System.out.println(" observe <game_id> -- this allows you to observe a game");
        System.out.println(" logout -- This will log you out");
        System.out.println(" help -- this will display (this) help menu again ");
    }

    public static void logOutHelp() {
        System.out.println("The available commands are..");
        System.out.println("  register <username> <password> <email> -- This will register you as a user");
        System.out.println("  help -- this will display (this) help menu again");
        System.out.println("  login <username> <password> -- this will log you in ");
        System.out.println("  quit -- this will kill the program");
    }

    public static void inGameHelp() {
        System.out.println("The available commands are..");
        System.out.println("resign");
        System.out.println("redraw");
        System.out.println("leave");
        System.out.println("move");
        System.out.println("highlight");
    }

    public enum ClientState {
        LOGGED_OUT,
        LOGGED_IN,
        GAMING
    }
}
