class Piece {
    Player owner;
    BoardNode position;  // 처음에는 null (집에 있음)
    boolean finished;

    public Piece(Player owner) {
        this.owner = owner;
        this.finished = false;
        this.position = null; // 초기 상태: 집 (보드에 없음)
    }

    @Override
    public String toString() {
        String info = "말@" + (position != null ? position.name : "집");
        if (finished) info += "(완주)";
        return info;
    }
}
