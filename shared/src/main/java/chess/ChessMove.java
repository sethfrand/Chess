package chess;

import java.util.PrimitiveIterator;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {
    private final ChessPosition startPosition;
    private final ChessPosition endPosition;
    private ChessPiece.PieceType promotionPiece;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return endPosition;
    }

    /*
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotionPiece;
    }

    @Override
    public int hashCode() {
        int result = startPosition.hashCode();
        result = 31 * result + endPosition.hashCode();
        result = 31 * result + (promotionPiece != null ? promotionPiece.hashCode() : 0);
        return result;

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChessMove move = (ChessMove) obj;

        if (!startPosition.equals(move.startPosition)) {
            return false;
        }
        if (!endPosition.equals(move.endPosition)) {
            return false;
        }
        if (promotionPiece == null) {
            return move.promotionPiece == null;
        } else {
            return promotionPiece.equals(move.promotionPiece);
        }
    }
}
