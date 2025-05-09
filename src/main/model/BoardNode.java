package model;
//BoardNode.java
import java.util.ArrayList;
import java.util.List;

public class BoardNode {
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

}

