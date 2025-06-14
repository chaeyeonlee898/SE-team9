package model;
//BoardNode.java
import java.util.ArrayList;
import java.util.List;

public class BoardNode {
    final int id;
    final String name;
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

    // BoardNode.java 내부에 추가
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BoardNode getNext() {
        return next;
    }

    public BoardNode getPrev() {
        return prev;
    }

    public BoardNode getShortcut() {
        return shortcut;
    }

    public boolean isIntersection() {
        return isIntersection;
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    /** 이 노드 위에 말 하나를 추가합니다. */
    public void addPiece(Piece p) {
        pieces.add(p);
    }

    /** 이 노드에서 말 하나를 제거합니다. */
    public void removePiece(Piece p) {
        pieces.remove(p);
    }

}

