package chess.movecalculators;

import chess.ChessMove;
import chess.ChessBoard;
import chess.ChessPosition;

import java.util.HashSet;

public class MoveCalculator {
    public static HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition position)
    {
        return null;
    }
    static boolean isValidMove(int row, int col)
    {
        return (row >= 1 && row <= 8 && col >= 1 && col <= 8);
    }
}
