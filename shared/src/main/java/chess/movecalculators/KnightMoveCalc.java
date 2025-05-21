package chess.movecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.HashSet;

public class KnightMoveCalc {
    private static final int[][] DIRECTIONS =
            {
                    //*from perspective of white
                    {2, 1}, // up 2 right 1
                    {1, 2}, //  up 1 right 2
                    {-1, 2}, // down 1 right 2
                    {-2, 1}, //down 2 right 1

                    {-2, -1}, // down 2 left 1
                    {-1, -2}, // down 1 left 2
                    {1, -2}, // up 1 left 2
                    {2, -1} //down 2, left 1
            };

    public static HashSet<chess.ChessMove> getMoves(chess.ChessBoard board, ChessPosition position) {
        return MoveCalculator.single(board, position, DIRECTIONS);
    }

    static boolean isValidMove(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }
}
