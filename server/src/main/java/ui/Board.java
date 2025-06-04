package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import static ui.EscapeSequences.*;

public class Board {

    private static final int BOARD_SIZE = 8;
    private static final String SIDE_CHARS = "  a b c d e f g h  ";


    public static void printBoard(ChessBoard board, ChessGame.TeamColor perspective) {
        if (perspective == ChessGame.TeamColor.WHITE) {
            printWhiteBoard(board);
        } else {
            printBlackBoard(board);
        }
    }

    public static void printWhiteBoard(ChessBoard board) {
        System.out.print(ERASE_SCREEN);

        System.out.println(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE + SIDE_CHARS + RESET_BG_COLOR);

        for (int row = 8; row >= 1; row--) {
            printWhiteRow(board, row);
        }

        System.out.println(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE + SIDE_CHARS + RESET_BG_COLOR);
        System.out.println();
    }

    private static void printWhiteRow(ChessBoard board, int row) {
        System.out.print(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE + " " + row + " " + RESET_BG_COLOR);

        for (int col = 1; col <= 8; col++) {
            ChessPosition position = new ChessPosition(row, col);
            printEachSquare(board, position, row, col);
        }
    }

    public static void printBlackBoard(ChessBoard board) {
        System.out.print(ERASE_SCREEN);

        String reverseSideChars = "  h g f e d c b a  ";
        System.out.println(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE + reverseSideChars + RESET_BG_COLOR);
        for (int row = 1; row <= 8; row++) {
            printBlackRow(board, row);
        }
        System.out.println(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE + reverseSideChars + RESET_BG_COLOR);
        System.out.println();
    }

    private static void printBlackRow(ChessBoard board, int row) {
        System.out.print(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE + " " + row + " " + RESET_BG_COLOR);
//do the opposite of printing white rows
        for (int col = 8; col >= 1; col--) {
            ChessPosition position = new ChessPosition(row, col);
            printEachSquare(board, position, row, col);
        }
    }

    private static void printEachSquare(ChessBoard board, ChessPosition position, int row, int col) {
        boolean isWhite = (row + col) % 2 == 0;
        String color = isWhite ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;

        ChessPiece piece = board.getPiece(position);
        String pieceChar = getPieceChar(piece);

        System.out.print(color + SET_TEXT_COLOR_BLACK + pieceChar + RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    private static String getPieceChar(ChessPiece piece) {
        if (piece == null) {
            return EMPTY; // if the piece doesnt exist, dont show anything
        }
        ChessPiece.PieceType type = piece.getPieceType();
        ChessGame.TeamColor team = piece.getTeamColor();

        if (team == ChessGame.TeamColor.WHITE) {
            return switch (type) {
                case ROOK -> WHITE_ROOK;
                case KNIGHT -> WHITE_KNIGHT;
                case BISHOP -> WHITE_BISHOP;
                case QUEEN -> WHITE_QUEEN;
                case KING -> WHITE_KING;
                case PAWN -> WHITE_PAWN;
            };
        } else {
            return switch (type) {
                case KING -> BLACK_KING;
                case QUEEN -> BLACK_QUEEN;
                case BISHOP -> BLACK_BISHOP;
                case KNIGHT -> BLACK_KNIGHT;
                case ROOK -> BLACK_ROOK;
                case PAWN -> BLACK_PAWN;
            };

        }
    }

}
