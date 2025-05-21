package chess.movecalculators;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.ChessGame;

import java.util.HashSet;


public class PawnMoveCalc {

    //Create check to see if position is valid. need to pass in the current row and column
    //update the current position
    public static HashSet<ChessMove> getMoves(chess.ChessBoard board, ChessPosition position) {
        HashSet<ChessMove> validMove = new HashSet<>();
        ChessPiece piece = board.getPiece(position);

        if (piece == null) // base case for if the piece isn't there
        {
            return validMove;
        }

        //check for team color to determine how the pawn will move,where it will start,
        // and where it promotes as the others are assuming white, assume white
        int forwardMovement = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int promoRow = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 8 : 1;

        int currRow = position.getRow();
        int currCol = position.getColumn();

        addForwardMoves(board, validMove, position, currRow, currCol, forwardMovement, startRow, promoRow);

        addCaptureMoves(board, validMove, position, piece, currRow, currCol, forwardMovement, promoRow);

        return validMove;
    }

    private static void addForwardMoves(chess.ChessBoard board, HashSet<ChessMove> validMove,
                                        ChessPosition position, int currRow, int currCol,
                                        int forwardMovement, int startRow, int promoRow) {
        // One square forward move
        int newRow = currRow + forwardMovement;

        if (isValidMove(newRow, currCol)) {
            ChessPosition newPos = new ChessPosition(newRow, currCol);
            ChessPiece atDest = board.getPiece(newPos);

            if (atDest == null) { // Check if the space is empty
                // Handle promotion if reached promotion row
                if (newRow == promoRow) {
                    addPromoMoves(validMove, position, newPos);
                } else {
                    validMove.add(new ChessMove(position, newPos, null));

                    // Handle two square move if it is possible
                    addTwoSquareMove(board, validMove, position, currRow, currCol, forwardMovement, startRow);
                }
            }
        }
    }

    private static void addTwoSquareMove(chess.ChessBoard board, HashSet<ChessMove> validMove,
                                         ChessPosition position, int currRow, int currCol,
                                         int forwardMovement, int startRow) {
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

    /**
     * Add all possible capture moves for a pawn
     */
    private static void addCaptureMoves(chess.ChessBoard board, HashSet<ChessMove> validMove,
                                        ChessPosition position, ChessPiece piece,
                                        int currRow, int currCol, int forwardMovement, int promoRow) {
        // Check both diagonal captures (left and right)
        for (int adjCol : new int[]{1, -1}) {
            int captureRow = currRow + forwardMovement;
            int captureCol = currCol + adjCol;

            if (!isValidMove(captureRow, captureCol)) {
                continue;
            }

            ChessPosition capturePos = new ChessPosition(captureRow, captureCol);
            ChessPiece capturePiece = board.getPiece(capturePos);

            if (capturePiece == null || capturePiece.getTeamColor() == piece.getTeamColor()) {
                continue;
            }
            if (captureRow == promoRow) {
                addPromoMoves(validMove, position, capturePos);
            } else {
                validMove.add(new ChessMove(position, capturePos, null));
            }
        }
    }

    // all promotion pieces
    static void addPromoMoves(HashSet<ChessMove> moves, ChessPosition start, ChessPosition end) {
        moves.add(new ChessMove(start, end, ChessPiece.PieceType.QUEEN));
        moves.add(new ChessMove(start, end, ChessPiece.PieceType.ROOK));
        moves.add(new ChessMove(start, end, ChessPiece.PieceType.BISHOP));
        moves.add(new ChessMove(start, end, ChessPiece.PieceType.KNIGHT));
    }

    //create helper function that can check to see if the move is valid
    static boolean isValidMove(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }
}