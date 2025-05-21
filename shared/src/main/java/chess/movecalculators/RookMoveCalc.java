package chess.movecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.movecalculators.MoveCalculator;

import java.util.HashSet;

public class RookMoveCalc {
    private static final int[][] DIRECTIONS =
            {
                    //*from perspective of white
                    {1, 0}, // up/forward
                    {0, 1}, // right
                    {0, -1}, // left
                    {-1, 0}, // down/backward
            };

    public static HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition position) {
        return MoveCalculator.sliding(board, position, DIRECTIONS);
    }

    //helper function that can check to see if the move is valid
    static boolean isValidMove(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }
}
