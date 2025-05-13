package chess.movecalculators;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.ChessGame;

import java.util.HashSet;


public class PawnMoveCalc
{

    //Create check to see if position is valid. need to pass in the current row and column
    //update the current position
    public static HashSet<ChessMove> getMoves(chess.ChessBoard board, ChessPosition position) {
        HashSet<ChessMove> validMove = new HashSet<>();
        ChessPiece piece = board.getPiece(position);

        if (piece == null) // base case for if the piece isn't there
        {
            return validMove;
        }


        //check for team color to determine how the pawn will move,where it will start, and where it promotes as the others are assuming white, assume white
        int forwardMovement = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int promoRow = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 8 : 1;

        int currRow = position.getRow();
        int currCol = position.getColumn();

        //if the pawn wants to move one row
        int newRow = currRow + forwardMovement;
        if (isValidMove(newRow, currCol)) {
            ChessPosition newPos = new ChessPosition(newRow, currCol);
            ChessPiece atDest = board.getPiece(newPos);
            if (atDest == null) //check to see if the space is empty
            {
                if (newRow == promoRow) {
                    addPromoMoves(validMove, position, newPos);
                } else {
                    validMove.add(new ChessMove(position, newPos, null));
                }
                // check to see if the pawn can go another square (2 square initial move.)
                if (currRow == startRow) {
                    int twoSquareMove = currRow + (2 * forwardMovement);
                    if (isValidMove(twoSquareMove, currCol)) {
                        ChessPosition twoSquarePos = new ChessPosition(twoSquareMove, currCol);
                        if (board.getPiece(twoSquarePos) == null) {
                            validMove.add(new ChessMove(position, twoSquarePos, null));
                        }
                    }
                }
            }
        }

        //capturing pieces, this will have similar logic to the bishop
        for (int adjCol : new int[]{1, -1}) //checking both adjacent columns of the pawn to see if there is a piece to capture
        {
            int captureRow = currRow + forwardMovement;
            int captureCol = currCol + adjCol;

            //check to see if the move is valid first and then see if you can capture
            if (isValidMove(captureRow, captureCol)) {
                ChessPosition capturePos = new ChessPosition(captureRow, captureCol);
                ChessPiece capturePiece = board.getPiece(capturePos); //piece is captured at this position

                if (capturePiece != null && capturePiece.getTeamColor() != piece.getTeamColor()) //make sure isnt same colour
                {
                    //check for promotion
                    if (captureRow == promoRow) {
                        addPromoMoves(validMove, position, capturePos);
                    } else {
                        validMove.add(new ChessMove(position, capturePos, null));
                    }
                }
            }
        }
        return validMove;
    }
        // all promotion pieces
        static void addPromoMoves(HashSet<ChessMove>moves, ChessPosition start, ChessPosition end)
        {
            moves.add(new ChessMove(start,end,ChessPiece.PieceType.QUEEN));
            moves.add(new ChessMove(start,end, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(start, end,ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.KNIGHT));

        }
    //create helper function that can check to see if the move is valid
        static boolean isValidMove(int row, int col)
        {
            return row >= 1 && row <= 8 && col >= 1 && col <= 8;
        }
}
