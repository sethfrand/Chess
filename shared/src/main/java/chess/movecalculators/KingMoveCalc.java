package chess.movecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.HashSet;

public class KingMoveCalc {
    private static final int[][] DIRECTIONS =
            {
                    //*from perspective of white
                    {1, 0}, // up/forward
                    {0, 1}, // right
                    {0, -1}, // left
                    {-1, 0}, // down/backward
                    {1, 1}, // up right
                    {1, -1}, // up left
                    {-1, -1}, // down left
                    {-1, 1} // down right
            };

    public static HashSet<chess.ChessMove> getMoves(chess.ChessBoard board, ChessPosition position) {
        return MoveCalculator.single(board, position, DIRECTIONS);
    }//helper function that can check to see if the move is valid

    static boolean isValidMove(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }
}


