package model;

public class PolygonBoard extends Board {
    protected final BoardNode[] outer;

    public PolygonBoard(int sides, int nodesPerSide) {
        outer = new BoardNode[sides * nodesPerSide];
        for (int i = 0; i < outer.length; i++) {
            outer[i] = new BoardNode(i, String.valueOf(i));
            nodes.add(outer[i]);
        }
        linkCycle(outer);
        start = outer[0];
    }

    protected void linkPath(BoardNode... path) {
        for (int i = 0; i < path.length - 1; i++) {
            path[i].next = path[i+1];
            path[i+1].prev = path[i];
        }
    }

    protected void linkCycle(BoardNode... cycle) {
        for (int i = 0; i < cycle.length; i++) {
            BoardNode curr = cycle[i];
            BoardNode next = cycle[(i+1) % cycle.length];
            curr.next = next;
            next.prev = curr;
        }
    }

    @Override protected void buildBoard() {}
    @Override public void printBoard() {}
}
