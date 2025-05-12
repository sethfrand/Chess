package chess.MoveCalculators;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessBoard;
import chess.ChessPosition;

import java.util.HashSet;

public class BishopMoveCalc {
    private static final int[][] Directions =
    {
            // *from perspective of white
            {1,1}, //up right
            {1,-1}, // up left
            {-1,1}, // down right
            {-1,-1} //down left
    };
    //Create check to see if position is valid. need to pass in the current row and column
    //update the current position
    public static HashSet<chess.ChessMove>getMoves(chess.ChessBoard board, ChessPosition position)
    {
        HashSet<ChessMove> validMove = new HashSet<>();
        ChessPiece piece  = board.getPiece(position);

        if(piece == null) // base case for if the piece isn't there
        {
            return validMove;
        }

        for (int []direction : Directions) // check diagonals bc bishop
        {
            int rowInc = direction[0];
            int colInc = direction[1];

            // move diagonally until you reach the edge
            int currRow = position.getRow() + rowInc;
            int currCol = position.getColumn() + colInc;

            while(isValidMove(currRow,currCol))
            {
                ChessPosition newPos = new ChessPosition(currRow, currCol);
                ChessPiece atDest = board.getPiece(newPos);

                //If the square that the piece wants to move to is empty, update the position
                if (atDest == null) //this is a valid move
                {
                    validMove.add(new ChessMove(position,newPos,null));
                }
                    else if(atDest.getTeamColor() != piece.getTeamColor())
                    {
                    validMove.add(new ChessMove(position, newPos, null));
                    break;
                    }
                    else
                    {
                        break; //else the move is invalid
                    }
                //incrementing the direction that we are moving
                currRow += rowInc;
                currCol+= colInc;
            }
        }
        return validMove;
    }
    //create helper function that can check to see if the move is valid
    static boolean isValidMove(int row, int col)
    {
        return (row >= 1 && row <= 8 && col >= 1 && col <= 8);
    }
}
