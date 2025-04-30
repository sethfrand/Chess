package chess;

import chess.MoveCalculators.BishopMoveCalc;
import chess.MoveCalculators.KingMoveCalc;
import chess.MoveCalculators.PawnMoveCalc;
import chess.MoveCalculators.RookMoveCalc;

import java.util.HashSet;

import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {


    private final ChessGame.TeamColor team;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.team = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor()
    {
        return team;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType()
    {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public HashSet<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition)
    {
        switch(getPieceType())
        {
            case BISHOP:
                return BishopMoveCalc.getMoves(board,myPosition);
            case KING:
                return KingMoveCalc.getMoves(board,myPosition);
            case QUEEN:
                return null;
            case KNIGHT:
                return null;
            case ROOK:
                return RookMoveCalc.getMoves(board,myPosition);
            case PAWN:
                return PawnMoveCalc.getMoves(board,myPosition);
            default: new HashSet<>();
        }
        return null;

    }
    @Override
    public String toString()
    {
        return switch (type)
        {
            case KING -> team == ChessGame.TeamColor.WHITE ? "K" : "k";
            case QUEEN -> team == ChessGame.TeamColor.WHITE ? "Q" : "q";
            case KNIGHT -> team == ChessGame.TeamColor.WHITE ? "N" : "n";
            case PAWN -> team == ChessGame.TeamColor.WHITE ? "P" : "p";
            case ROOK -> team == ChessGame.TeamColor.WHITE ? "R" : "r";
            case BISHOP -> team == ChessGame.TeamColor.WHITE ? "B" : "b";
        };
    }

    @Override
    public int hashCode() {
        int result = team.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return false;
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        ChessPiece piece = (ChessPiece) obj;
        if (team != piece.team) return false;
        return type == piece.type;


    }
}
