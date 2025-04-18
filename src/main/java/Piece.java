import java.util.Stack;

class Piece {
    Player owner;
    BoardNode position;
    boolean finished;
    boolean hasLeftStart = false;           // 한 바퀴 나감 플래그
    Stack<BoardNode> moveHistory = new Stack<>(); // 이동 이력

    public Piece(Player owner) {
        this.owner = owner;
        this.finished = false;
        this.position = null;
    }

    @Override
    public String toString() {
        String info = "말@" + (position != null ? position.name : "집");
        if (finished) info += "(완주)";
        return info;
    }
}
