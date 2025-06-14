package ui;


import chess.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static ui.EscapeSequences.*;


public class Board {


    private static final int BOARD_SIZE = 8;
    private static final String SIDE_CHARS = "    a  b  c  d  e  f  g  h    ";


    public static void printBoard(ChessBoard board, ChessGame.TeamColor perspective) {
        if (perspective == ChessGame.TeamColor.WHITE) {
            printWhiteBoard(board);
        } else {
            printBlackBoard(board);
        }
    }


    public static void printWhiteBoard(ChessBoard board) {
        System.out.print(ERASE_SCREEN);


        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + SIDE_CHARS + RESET_BG_COLOR);


        for (int row = 8; row >= 1; row--) {
            printWhiteRow(board, row, null, null);
        }


        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + SIDE_CHARS + RESET_BG_COLOR);
        System.out.println();
    }

    private static void printWhiteHighlightBoard(ChessBoard board, ChessPosition square, Set<ChessPosition> highlights) {
        System.out.print(ERASE_SCREEN);

        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + SIDE_CHARS + RESET_BG_COLOR);
        for (int row = 8; row >= 1; row--) {
            printWhiteRow(board, row, square, highlights);
        }
        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + SIDE_CHARS + RESET_BG_COLOR);
        System.out.println();
    }


    private static void printWhiteRow(ChessBoard board, int row, ChessPosition square, Set<ChessPosition> highlight) {
        System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " " + row + " " + RESET_BG_COLOR);


        for (int col = 1; col <= 8; col++) {
            ChessPosition position = new ChessPosition(row, col);
            printEachSquare(board, position, row, col, square, highlight);
        }
        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " " + row + " " + RESET_BG_COLOR);
    }


    public static void printBlackBoard(ChessBoard board) {
        System.out.print(ERASE_SCREEN);


        String reverseSideChars = "    h  g  f  e  d  c  b  a    ";
        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + reverseSideChars + RESET_BG_COLOR);
        for (int row = 1; row <= 8; row++) {
            printBlackRow(board, row, null, null);
        }
        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + reverseSideChars + RESET_BG_COLOR);
        System.out.println();
    }

    private static void printBlackHighlightBoard(ChessBoard board, ChessPosition square, Set<ChessPosition> highlights) {
        System.out.print(ERASE_SCREEN);


        String reverseSideChars = "    h  g  f  e  d  c  b  a    ";
        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + reverseSideChars + RESET_BG_COLOR);
        for (int row = 1; row <= 8; row++) {
            printBlackRow(board, row, square, highlights);
        }
        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + reverseSideChars + RESET_BG_COLOR);
        System.out.println();
    }


    private static void printBlackRow(ChessBoard board, int row, ChessPosition square, Set<ChessPosition> highlights) {
        System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " " + row + " " + RESET_BG_COLOR);
//do the opposite of printing white rows
        for (int col = 8; col >= 1; col--) {
            ChessPosition position = new ChessPosition(row, col);
            printEachSquare(board, position, row, col, square, highlights);
        }
        System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " " + row + " " + RESET_BG_COLOR);
    }


    private static void printEachSquare(ChessBoard board, ChessPosition position, int row, int col, ChessPosition square,
                                        Set<ChessPosition> highlights) {
        boolean isWhite = (row + col) % 2 == 0;
        String color;

        if (square != null && position.equals(square)) {
            color = SET_BG_COLOR_BLUE;
        } else if (highlights != null && highlights.contains(position)) {
            color = SET_BG_COLOR_YELLOW;
        } else {
            color = isWhite ? SET_BG_COLOR_DARK_GREY : SET_BG_COLOR_LIGHT_GREY;
        }

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


    public static void showBrightBoard(ChessBoard board, ChessGame.TeamColor perspective, ChessPosition square, Collection<ChessMove> moves) {
        Set<ChessPosition> highlights = new HashSet<>();
        if (moves != null) {
            for (ChessMove move : moves) {
                highlights.add(move.getEndPosition());
            }
        }
        if (perspective == ChessGame.TeamColor.WHITE) {
            printWhiteHighlightBoard(board, square, highlights);
        } else {
            printBlackHighlightBoard(board, square, highlights);
        }
    }

}
