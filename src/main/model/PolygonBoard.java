package model;
//────────────────────────────────────────────────────────────
// PolygonBoard 클래스: 외곽 다각형 보드 기본 구현
//────────────────────────────────────────────────────────────
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
            BoardNode next = cycle[(i + 1) % cycle.length];
            curr.next = next;
            next.prev = curr;
        }
    }

        protected BoardNode[] createInnerNodes (int[] innerIds){
            BoardNode[] innerNodes = new BoardNode[innerIds.length];
            for (int i = 0; i < innerIds.length; i++) {
                innerNodes[i] = new BoardNode(innerIds[i], String.valueOf(innerIds[i]));
                nodes.add(innerNodes[i]);
            }
            return innerNodes;
        }
        protected void setShortcuts ( int[][] sc){
            for (int[] pair : sc) {
                BoardNode outerNode = find(pair[0]);   // ex. ID=5 노드
                BoardNode target = find(pair[1]);   // ex. ID=30 노드
                outerNode.isIntersection = true;
                outerNode.shortcut = target;
            }
        }
        protected BoardNode find(int id){
            return nodes.stream().filter(n -> n.id == id).findFirst().orElse(null);
        }

    @Override protected void buildBoard() {}
    @Override public void printBoard() {}
}
