package chess;

import javax.swing.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor teamTurn;
    private ChessBoard board;

    public ChessGame() {
        teamTurn = TeamColor.WHITE; // White traditionally goes first in chess
        board = new ChessBoard();
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn()
    {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition)
    {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null){return null;};

        Collection<ChessMove> possibleMoves = piece.pieceMoves(board,startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : possibleMoves)
        {
            ChessBoard altBoard = new ChessBoard();

            copyBoard(board, altBoard);

            makeTempMove(altBoard,move);

            //make temp move to see if its valid
                //its valid if the king is not in check
        }

        //function to see if the king is in check
            //we'll have to know where the king is
                //maybe loop through the board to find the king??



            //make the move on this temproary board and see if the king is in check, if it is then this is not a valid move
        return validMoves;
    }

    //function to create the alternate board that we will be working with.
    //probably do a for loop and create the board to be a match.
    private void copyBoard(ChessBoard sourceBoard, ChessBoard destBoard)
    {
        for (int row = 1; row <= 8; row ++)
        {
            for (int col = 1; col <= 8; col++)
            {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = sourceBoard.getPiece(position);
                destBoard.addPiece(position,piece);
            }
        }
    }

    private void makeTempMove(ChessBoard board,ChessMove move)
    {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        board.addPiece(move.getEndPosition(),piece);
        board.addPiece(move.getStartPosition(),null); //a workaround so that we don't need to make a removePiece method
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board)
    {
       this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard()
    {
        return board;
    }
}
