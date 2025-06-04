package ui;

import chess.ChessBoard;
import chess.ChessGame;
import static ui.EscapeSequences.*;

public class Board {
    """
    have board size
    characters available on the sides of the boards




    set to see if it will be from perspective of white or black

"""


    private static final int BOARD_SIZE = 8;
    private static final String SIDE_CHARS = "  a b c d e f g h  ";


    public static void printBoard(ChessBoard board, ChessGame.TeamColor perspective)
    {
        if(perspective == ChessGame.TeamColor.WHITE)
        {
            printWhiteBoard(board);
        }else{
            printBlackBoard(board);
        }
    }

    public static void printWhiteBoard(ChessBoard board)
    {
        System.out.print(ERASE_SCREEN);

        System.out.println(SET_BG_COLOR_BLACK + SET_BG_COLOR_WHITE + SIDE_CHARS + RESET_BG_COLOR);

        for (int row = 1; row <= 8; row++ )
        {
            printWhitekRow(board, row);
        }

        System.out.println(SET_BG_COLOR_BLACK + SET_BG_COLOR_WHITE + SIDE_CHARS + RESET_BG_COLOR);
        System.out.println();
    }

    public static void printBlackBoard(ChessBoard board)
    {
        System.out.print(ERASE_SCREEN);

        String reverseSideChars = "  h g f e d c b a  ";
        System.out.println(SET_BG_COLOR_BLACK + SET_BG_COLOR_WHITE + reverseSideChars + RESET_BG_COLOR);
        for (int row = 1; row <=8; row++)
        {
            printBlackRow(board, row);
        }
        System.out.println(SET_BG_COLOR_BLACK + SET_BG_COLOR_WHITE + SIDE_CHARS + RESET_BG_COLOR);
        System.out.println();
    }

}
