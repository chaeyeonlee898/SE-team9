import java.util.ArrayList;
import java.util.List;

class Player {
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
}
