package model;
//────────────────────────────────────────────────────────────
//HexagonBoard 클래스
//────────────────────────────────────────────────────────────
public class HexagonBoard extends PolygonBoard {
    public HexagonBoard() {
        super(6,5);

        // (1) 내부 노드 생성
        int[] innerIds = {30,31,32,33,34,35,36,37,38,39,40,41,42};
        BoardNode[] innerNodes = createInnerNodes(innerIds);
        // (2) 중앙 교차점 설정
        BoardNode center = innerNodes[12];  // id == 42
        center.isIntersection = true;

        // (3) 내부 경로 연결
        linkPath(innerNodes[0],  innerNodes[1],  center);        // 30 → 31 → 42
        linkPath(innerNodes[2],  innerNodes[3],  center);        // 32 → 33 → 42
        linkPath(innerNodes[4],  innerNodes[5],  center);        // 34 → 35 → 42
        linkPath(innerNodes[6],  innerNodes[7],  center);        // 36 → 37 → 42
        linkPath(innerNodes[8],  innerNodes[9],  outer[25]);     // 38 → 39 → 외곽 25
        linkPath(innerNodes[10], innerNodes[11], outer[0]);      // 40 → 41 → 외곽 0

        // (4) 중앙에서 나가는 길
        center.next     = innerNodes[8];   // 멈추지 않을 경우 38→…
        innerNodes[8].prev   = center;
        center.shortcut = innerNodes[10];  // 정확히 42에서 멈춘 후 40→…
        innerNodes[10].prev  = center;

        // (5) 외곽 교차점 숏컷 설정 (ID 매핑 방식)
        // 배열 형식을 {외곽ID, 내부ID}로 두고, find()로 BoardNode를 가져옵니다.
        int[][] sc = {{5, 30 }, {10, 32 }, {15, 34 }, {20, 36 }};
        setShortcuts(sc);
    }
}