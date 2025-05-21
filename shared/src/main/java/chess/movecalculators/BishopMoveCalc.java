package chess.movecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.HashSet;

public class BishopMoveCalc {
    private static final int[][] DIRECTIONS =
            {
                    // *from perspective of white
                    {1, 1}, //up right
                    {1, -1}, // up left
                    {-1, 1}, // down right
                    {-1, -1} //down left
            };

    //Create check to see if position is valid. need to pass in the current row and column
    //update the current position
    public static HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition position) {
        return MoveCalculator.sliding(board, position, DIRECTIONS);
    }

    //helper function that can check to see if the move is valid
    static boolean isValidMove(int row, int col) {
        return MoveCalculator.isValidMove(row, col);
    }
}
