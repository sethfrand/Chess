package chess.movecalculators;

import chess.ChessMove;
import chess.ChessBoard;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.HashSet;

public class MoveCalculator {
    public static HashSet<chess.ChessMove> sliding(chess.ChessBoard board, ChessPosition position, int[][] directions) {
        HashSet<ChessMove> validMove = new HashSet<>();
        ChessPiece piece = board.getPiece(position);

        if (piece == null) // base case for if the piece isnt there
        {
            return validMove;
        }

        for (int[] direction : directions) // check all directions
        {
            int rowInc = direction[0];
            int colInc = direction[1];

            // move diagonally until you reach the edge
            int currRow = position.getRow() + rowInc;
            int currCol = position.getColumn() + colInc;

            while (isValidMove(currRow, currCol)) //can move more than one space
            {
                ChessPosition newPos = new ChessPosition(currRow, currCol);
                ChessPiece atDest = board.getPiece(newPos);

                //If the square that the piece wants to move to is empty, update the position
                if (atDest == null) //this is a valid move
                {
                    validMove.add(new ChessMove(position, newPos, null));
                } else if (atDest.getTeamColor() != piece.getTeamColor()) {
                    validMove.add(new ChessMove(position, newPos, null));
                    break;
                } else {
                    break; //else the move is invalid
                }
                //incrementing the direction that we are moving
                currRow += rowInc;
                currCol += colInc;
            }
        }
        return validMove;
    }

    public static HashSet<ChessMove> single(chess.ChessBoard board, ChessPosition position, int[][] directions) {
        HashSet<ChessMove> validMove = new HashSet<>();
        ChessPiece piece = board.getPiece(position);

        if (piece == null) {
            return validMove;
        }
        for (int[] direction : directions) // check all possible moves
        {
            int rowInc = direction[0];
            int colInc = direction[1];

            int currRow = position.getRow() + rowInc;
            int currCol = position.getColumn() + colInc;

            if (isValidMove(currRow, currCol)) {
                ChessPosition newPos = new ChessPosition(currRow, currCol);
                ChessPiece atDest = board.getPiece(newPos);

                //If the square that the piece wants to move to is empty, update the position
                if (atDest == null) //this is a valid move
                {
                    validMove.add(new ChessMove(position, newPos, null));
                } else if (atDest.getTeamColor() != piece.getTeamColor()) {
                    validMove.add(new ChessMove(position, newPos, null)); //in the future this is where we would add a point to the other team
                }
            }
        }
        return validMove;

    }

    static boolean isValidMove(int row, int col) {
        return (row >= 1 && row <= 8 && col >= 1 && col <= 8);
    }
}
