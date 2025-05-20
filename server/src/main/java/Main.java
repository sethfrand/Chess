import chess.*;
import server.Routeregistar;
import server.Server;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);
        Server server = new Server();
        int port = server.run(3456);
        //new Routeregistar().initRout();
        System.out.println("Running on port" + port);
    }
}