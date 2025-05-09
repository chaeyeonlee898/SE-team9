package model;

public class PentagonBoard extends PolygonBoard {
    public PentagonBoard() {
        super(5, 5);

        int[] innerIds = {25,26,27,28,29,30,31,32,33,34,35};
        BoardNode[] innerNodes = new BoardNode[innerIds.length];
        for (int i = 0; i < innerIds.length; i++) {
            innerNodes[i] = new BoardNode(innerIds[i], String.valueOf(innerIds[i]));
            nodes.add(innerNodes[i]);
        }

        BoardNode center = innerNodes[10];
        center.isIntersection = true;

        linkPath(innerNodes[0], innerNodes[1], innerNodes[10]);
        linkPath(innerNodes[2], innerNodes[3], innerNodes[10]);
        linkPath(innerNodes[4], innerNodes[5], innerNodes[10]);
        linkPath(innerNodes[10], innerNodes[6], innerNodes[7], outer[20]);
        linkPath(innerNodes[8], innerNodes[9], outer[0]);

        int[][] sc = {{5,25}, {10,27}, {15,29}};
        for (int[] s : sc) {
            BoardNode c = find(s[0]);
            c.isIntersection = true;
            c.shortcut = find(s[1]);
        }

        center.shortcut = innerNodes[8];
    }

    private BoardNode find(int id) {
        return nodes.stream().filter(n -> n.id == id).findFirst().orElse(null);
    }

    @Override protected void buildBoard() {}
    @Override public void printBoard() {}
}
