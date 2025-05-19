package model;

//Player.java
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Player {
    private String name;
    List<Piece> pieces;

    public Player(String name, int pieceCount, Board board) {
        this.name = name;
        pieces = new ArrayList<>();
        for (int i = 0; i < pieceCount; i++) {
            Piece p = new Piece(this);
            pieces.add(p);
        }
    }

    public String getName() {
        return name;
    }

    public boolean allPiecesFinished() {
        for (Piece p : pieces) {
            if (!p.finished) return false;
        }
        return true;
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public long getFinishedPieceCount() {
        return pieces.stream().filter(Piece::isFinished).count();
    }

    public long getRemainingPieceCount() {
        return pieces.stream().filter(p -> !p.isFinished()).count();
    }

    public List<Piece> getUnfinishedPieces() {
        return pieces.stream()
                .filter(p -> !p.isFinished())
                .collect(Collectors.toList());
    }
}
