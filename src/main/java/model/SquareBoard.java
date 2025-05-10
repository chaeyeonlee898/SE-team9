package model;
//────────────────────────────────────────────────────────────
// SquareBoard 클래스: 사각형 윷놀이 보드
//────────────────────────────────────────────────────────────
public class SquareBoard extends PolygonBoard {
    public SquareBoard() {
        super(4, 5); // 4변 × 5칸 = 20칸 외곽

        // 1) 내부 노드 생성 및 배열 저장
        int[] innerIds = {20,21,22,23,24,25,26,27,28};
        BoardNode[] innerNodes = createInnerNodes(innerIds);
        // 2) 중심 교차점 설정
        BoardNode center = innerNodes[8];  // id == 28
        // 3) 내부 경로 연결
        linkPath(innerNodes[0], innerNodes[1], innerNodes[8]); // 20 → 21 → 28
        linkPath(innerNodes[2], innerNodes[3], innerNodes[8]); // 22 → 23 → 28
        // 4) 중심에서 외곽으로 두 갈래
        linkPath(innerNodes[8], innerNodes[4], innerNodes[5], outer[15]); // 28 → 24 → 25 → 15
        linkPath(innerNodes[6], innerNodes[7], outer[0]); // 26 → 27 → 0

        // 5) shortcut 설정 (직접 참조 사용)
        int[][] sc = {{5,20},{10,22}};
        setShortcuts(sc);
        center.isIntersection     = true;           // (center == innerNodes[8], id=28)
        center.shortcut           = innerNodes[6];  // 28 → 26

    }
    @Override protected void buildBoard() {
        super.buildBoard();
    }
}