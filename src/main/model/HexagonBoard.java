package model;

public class HexagonBoard extends PolygonBoard {
    public HexagonBoard() {
        super(6, 5);

        int[] innerIds = {30,31,32,33,34,35,36,37,38,39,40,41,42};
        BoardNode[] inner = new BoardNode[innerIds.length];
        for (int i = 0; i < inner.length; i++) {
            inner[i] = new BoardNode(innerIds[i], String.valueOf(innerIds[i]));
            nodes.add(inner[i]);
        }

        BoardNode center = inner[12];
        center.isIntersection = true;

        linkPath(inner[0], inner[1], center);
        linkPath(inner[2], inner[3], center);
        linkPath(inner[4], inner[5], center);
        linkPath(inner[6], inner[7], center);
        linkPath(inner[8], inner[9], outer[25]);
        linkPath(inner[10], inner[11], outer[0]);

        center.next = inner[8];
        inner[8].prev = center;
        center.shortcut = inner[10];
        inner[10].prev = center;

        int[][] sc = {
                {5,30}, {10,32}, {15,34}, {20,36}
        };
        for (int[] pair : sc) {
            BoardNode outerNode = find(pair[0]);
            BoardNode target = find(pair[1]);
            outerNode.isIntersection = true;
            outerNode.shortcut = target;
        }
    }

    private BoardNode find(int id) {
        return nodes.stream().filter(n -> n.id == id).findFirst().orElse(null);
    }
}
