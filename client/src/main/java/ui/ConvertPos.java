package ui;

import chess.ChessPosition;

import static ui.Client.isValidMove;

public class ConvertPos {
    public static ChessPosition convertPos(String from) {
        if (from == null || from.length() != 2) {
            return null;
        }
        char colChar = from.charAt(0);
        char rowChar = from.charAt(1);

        int col = colChar - 'a' + 1;
        int row = rowChar - '0';

        if (isValidMove(row, col)) {
            return null;
        }
        return new ChessPosition(row, col);
    }
}
