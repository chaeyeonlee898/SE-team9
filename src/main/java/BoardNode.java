//BoardNode.java
import java.util.ArrayList;
import java.util.List;

class BoardNode {
    int id;
    String name;
    BoardNode next;
    BoardNode prev; // 복구
    BoardNode shortcut;
    boolean isIntersection = false;
    List<Piece> pieces;

    public BoardNode(int id, String name) {
        this.id = id;
        this.name = name;
        this.pieces = new ArrayList<>();
    }

    @Override
    public String toString() {
        return name + "[" + pieces.size() + "]";
    }
}

