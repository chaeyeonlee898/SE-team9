package model;

import java.util.Stack;

public class Piece {
    Player owner;
    BoardNode position; //말이 위치한 보드노드
    BoardNode lastPosition;         // ← 이전 위치 저장용
    boolean finished;
    boolean hasLeftStart = false;           // 한 바퀴 나감 플래그(시작 지점을 떠났음을 표시)
    Stack<BoardNode> moveHistory = new Stack<>(); // 이동 이력
    boolean justStoppedAtIntersection;    // 최근 턴에 교차점에 정확히 멈췄는지

    public Piece(Player owner) {
        this.owner = owner;
        this.finished = false;
        this.position = null;
        this.justStoppedAtIntersection = false;
    }

    @Override
    public String toString() {
        String info = "말@" + (position != null ? position.name : "집");
        if (finished) info += "(완주)";
        return info;
    }

    // Piece.java
    public Player getOwner() {
        return owner;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setHasLeftStart(boolean val) {
        this.hasLeftStart = val;
    }

    public void setJustStoppedAtIntersection(boolean val) {
        this.justStoppedAtIntersection = val;
    }

    public BoardNode getPosition() {
        return position;
    }


}
