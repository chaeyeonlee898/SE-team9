package model;

public class SquareBoard extends PolygonBoard {
    public SquareBoard() {
        super(4, 5);

        int[] innerIds = {20,21,22,23,24,25,26,27,28};
        BoardNode[] innerNodes = new BoardNode[innerIds.length];
        for (int i = 0; i < innerIds.length; i++) {
            innerNodes[i] = new BoardNode(innerIds[i], String.valueOf(innerIds[i]));
            nodes.add(innerNodes[i]);
        }

        BoardNode center = innerNodes[8];
        linkPath(innerNodes[0], innerNodes[1], innerNodes[8]);
        linkPath(innerNodes[2], innerNodes[3], innerNodes[8]);
        linkPath(innerNodes[8], innerNodes[4], innerNodes[5], outer[15]);
        linkPath(innerNodes[6], innerNodes[7], outer[0]);

        outer[5].isIntersection = true;
        outer[5].shortcut = innerNodes[0];

        outer[10].isIntersection = true;
        outer[10].shortcut = innerNodes[2];

        center.isIntersection = true;
        center.shortcut = innerNodes[6];
    }

    @Override protected void buildBoard() {
        super.buildBoard();
    }
}
