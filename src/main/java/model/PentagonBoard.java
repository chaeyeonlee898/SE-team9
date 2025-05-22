package model;
//────────────────────────────────────────────────────────────
// PentagonBoard 클래스
//────────────────────────────────────────────────────────────
public class PentagonBoard extends PolygonBoard {
    public PentagonBoard() {
        super(5,5);

        // 1) 내부 노드 생성 및 배열 저장
        int[] innerIds = {25,26,27,28,29,30,31,32,33,34,35};
        BoardNode[] innerNodes = createInnerNodes(innerIds);
        // 2) 중심 교차점 설정
        BoardNode center = innerNodes[10];  // id == 28
        center.isIntersection = true;
        // 3) 내부 경로 연결
        linkPath(innerNodes[0], innerNodes[1], innerNodes[10]); // 25 → 26 → 35
        linkPath(innerNodes[2], innerNodes[3], innerNodes[10]); // 27 → 28 → 35
        linkPath(innerNodes[4], innerNodes[5], innerNodes[10]); // 29 → 30 → 35
        // 4) 중심에서 외곽으로 두 갈래
        linkPath(innerNodes[10], innerNodes[6], innerNodes[7], outer[20]); // 35 → 31 → 32 → 20
        linkPath(innerNodes[8], innerNodes[9], outer[0]); // 35 → 33 → 34 → 0
        // 5) shortcut 설정 (직접 참조 사용)
        int[][] sc = {{5,25},{10,27},{15,29}};
        setShortcuts(sc);
        center.isIntersection     = true;           // (center == innerNodes[10], id=35)
        center.shortcut           = innerNodes[8];  // 35 → 33

    }

    @Override protected void buildBoard() {}
    @Override public void printBoard() {}
}