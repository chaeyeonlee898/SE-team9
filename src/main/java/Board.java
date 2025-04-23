//Board.java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

abstract class Board {
    protected BoardNode start;  // 출발(시작) 노드
    protected final List<BoardNode> nodes = new ArrayList<>();

    public Board() {
        buildBoard();
    }

    public Board(int i, int i1) {
    }

    // 각 하위 보드에서 보드 노드 그래프를 구성
    protected abstract void buildBoard();

    public BoardNode getStart() {
        return start;
    }

    // 현재 노드에서 steps만큼 이동한 노드를 계산합니다.
    // 첫 스텝에서는, 만약 현재 노드가 시작(0번)이라면 shortcut은 무시하고 외곽 길을 따라 갑니다.
    public BoardNode getNextNode(BoardNode current, int steps) {
        BoardNode temp = current;
        for (int i = 0; i < steps; i++) {
            if (temp == null) break;
            boolean lastStep = (i == steps - 1);

            // ★ 시작점(start)에서는 절대 shortcut을 타지 않도록 temp != start 추가
            if (lastStep
                    && temp != start
                    && temp.isIntersection
                    && temp.shortcut != null) {
                // 정확히 교차점에 멈췄을 때만 최단 경로로
                temp = temp.shortcut;
            } else {
                // 그 외 모든 경우 외곽(next) 이동
                temp = temp.next;
            }

            // 중간에 start를 재도달하면 즉시 완주 처리
            if (temp == start && i < steps - 1) {
                return start;
            }
        }
        return temp;
    }


    // 업(스택) 기능 포함 movePiece() 메서드.
    // 만약 말이 아직 보드에 진입하지 않았다면(position == null),
    // 그 말을 보드에 진입시키고 그 다음 이동 계산을 수행합니다.
    // 그리고 상대 말을 잡으면 "집" (position == null)으로 돌려보냅니다.1
    public boolean movePiece(Piece piece, int steps, Scanner scanner) {

        // 디버그: 이동 시작
        System.out.println("[DEBUG] " + piece.owner.getName()
                + " 호출 movePiece(steps=" + steps + ")");

        // 0) 빽도 처리
        if (steps == -1) {
            if (piece.position == null || piece.moveHistory.isEmpty()) {
                System.out.println("아직 보드에 진입하지 않아 빽도 불가. 제자리에 머무릅니다.");
                return false;
            }
            BoardNode prev = piece.moveHistory.pop();
            piece.position.pieces.remove(piece);

            boolean captureOccurred = false;
            for (Piece enemy : new ArrayList<>(prev.pieces)) {
                if (enemy.owner != piece.owner) {
                    captureOccurred = true;
                    prev.pieces.remove(enemy);
                    enemy.position = null;
                    System.out.println("[DEBUG] 빽도로 캡처: " + enemy.owner.getName());
                }
            }

            prev.pieces.add(piece);
            piece.position = prev;
            System.out.println("[DEBUG] " + piece.owner.getName()
                    + " 빽도로 이동 → " + prev);
            return captureOccurred;
        }

        // 1) 첫 진입 여부 및 src 결정
        boolean firstEntry = (piece.position == null);
        BoardNode src = firstEntry ? getStart() : piece.position;

        // 2) “한 바퀴 돌아 출발점에 있는 상태”인 경우, 
        //    빽도(-1) 외 어떤 steps이든 즉시 완주 처리
        if (!firstEntry && src == getStart() && piece.hasLeftStart && steps > 0) {
            piece.finished = true;
            System.out.println(piece.owner.getName() + "의 말이 완주했습니다!");
            return false;
        }

        // 3) 업(스택) 기능: 같은 팀 말 묶기
        List<Piece> stack = new ArrayList<>();
        if (firstEntry) {
            stack.add(piece);
        } else {
            for (Piece p : new ArrayList<>(src.pieces)) {
                if (p.owner == piece.owner) stack.add(p);
            }
            for (Piece p : stack) src.pieces.remove(p);
        }

        // 4) 단계별 경로 계산
        List<BoardNode> path = new ArrayList<>();
        BoardNode cur = src;
        for (int i = 0; i < steps; i++) {
            cur = getNextNode(cur, 1);
            path.add(cur);
        }
        BoardNode dest = cur;

        // 5) 첫 진입 후 ‘한 바퀴 나감’ 플래그 설정
        if (firstEntry && dest != getStart()) {
            piece.hasLeftStart = true;
        }

        // 6) 경로에 출발점(start)을 지나친 경우 완주
        if (!firstEntry) {
            for (int i = 0; i < path.size() - 1; i++) {
                if (path.get(i) == getStart()) {
                    for (Piece p : stack) p.finished = true;
                    System.out.println(piece.owner.getName()
                        + "의 업(" + stack.size() + "개)이 완주했습니다!");
                    return false;
                }
            }
        }

        // 7) 캡처 처리
        boolean captureOccurred = false;
        for (Piece enemy : new ArrayList<>(dest.pieces)) {
            if (enemy.owner != piece.owner) {
                captureOccurred = true;
                dest.pieces.remove(enemy);
                enemy.position = null;
                System.out.println(enemy.owner.getName()
                    + "의 말이 캡처되어 집으로 돌아갑니다.");
            }
        }

        // 8) 이동 및 이력 기록
        for (Piece p : stack) {
            p.moveHistory.push(src);
            for (int i = 0; i < path.size() - 1; i++) {
                p.moveHistory.push(path.get(i));
            }
            dest.pieces.add(p);
            p.position = dest;
        }

        System.out.println(piece.owner.getName()
            + "의 말(" + stack.size() + "개)이 " + dest + "로 이동했습니다.");
        debugPrintAllNodes();
        return captureOccurred;
    }

    /** 전체 노드 상태 디버그 출력 */
    public void debugPrintAllNodes() {
        System.out.print("[DEBUG BOARD] ");
        for (BoardNode n : nodes) {
            System.out.print(n.id + "[" + n.pieces.size() + "] ");
        }
        System.out.println();
    }

    public abstract void printBoard();
}

/**
 * sides: 외곽 변의 개수, nodesPerSide: 변당 칸 수
 */
class PolygonBoard extends Board {
    protected final BoardNode[] outer;

    public PolygonBoard(int sides, int nodesPerSide) {
        outer = new BoardNode[sides * nodesPerSide];
        for (int i = 0; i < outer.length; i++) {
            outer[i] = new BoardNode(i, String.valueOf(i));
            nodes.add(outer[i]);
        }
        for (int i = 0; i < outer.length; i++) {
            BoardNode curr = outer[i];
            BoardNode next = outer[(i + 1) % outer.length];
            curr.next = next;
            next.prev = curr;
        }
        start = outer[0];
    }

    @Override
    protected void buildBoard() {
        // 외곽은 생성자에서 구성, 내부 경로는 서브클래스에서 추가
    }

    @Override
    public void printBoard() {

    }
}

//────────────────────────────────────────────────────────────
// SquareBoard 클래스: 전통 윷놀이 사각형 판 (총 29개 노드)
// 외곽 트랙: 0~19 (20개 노드), 0번은 출발 및 완주 지점
// 안쪽 X자 경로: 20~28 (9개 노드)
//  - 코너(0,5,10,15)에서만 shortcut 적용
//  - 처음에는 말이 집에 있으므로 첫 이동 시 board.getStart().next부터 진행


class SquareBoard extends PolygonBoard {
    public SquareBoard() {
        super(4, 5); // 4변 × 5칸 = 20칸 외곽

        // 1) 내부 노드(20~27) 및 중심(28) 생성
        int[] innerIds = {20,21,22,23,24,25,26,27,28};
        for (int id : innerIds) {
            BoardNode node = new BoardNode(id, String.valueOf(id));
            nodes.add(node);
        }
        BoardNode center = find(28);
        center.isIntersection = true;

        // 2) 내부 노드 간 외곽(다음) 연결 (X자 경로)
        // 경로: 5→20→21→28, 10→22→23→28, 28→26→27→0, 28→24→25→15
        linkPath(new int[]{20,21,28});
        linkPath(new int[]{22,23,28});
        linkPath(new int[]{28,24,25});
        linkPath(new int[]{28,26,27});
        // 28(Center)→0 출발점(완주) 연결
        center.next = find(0); find(0).prev = center;

        // 3) 교차점 표시 및 최단 경로(shortcut) 설정
        int[][] shortcuts = {
                {0,27}, {5,20}, {10,22}, {15,25}
        };
        for (int[] sc : shortcuts) {
            BoardNode corner = find(sc[0]);
            corner.isIntersection = true;
            corner.shortcut = find(sc[1]);
        }
    }

    private void linkPath(int[] path) {
        for (int i = 0; i < path.length - 1; i++) {
            BoardNode a = find(path[i]);
            BoardNode b = find(path[i+1]);
            a.next = b; b.prev = a;
        }
    }

    private BoardNode find(int id) {
        return nodes.stream().filter(n->n.id==id).findFirst().orElse(null);
    }

    @Override
    protected void buildBoard() {}
}

//────────────────────────────────────────────────────────────
// PentagonBoard 및 HexagonBoard (간단 예시)
class PentagonBoard extends PolygonBoard {
    public PentagonBoard() {
        super(5, 5); // 5변 × 5칸 = 25칸 외곽

        // 1) 내부 노드(25~34) 및 중심(35) 생성
        int[] innerIds = {25,26,27,28,29,30,31,32,33,34,35};
        for (int id : innerIds) {
            BoardNode node = new BoardNode(id, String.valueOf(id));
            nodes.add(node);
        }
        BoardNode center = find(35);
        center.isIntersection = true;

        // 2) 내부 노드 간 외곽(다음) 연결 (X자 경로)
        // 경로: 5→25→26→35, 10→27→28→35, 15→29→30→35, 35→31→32→20, 35→33→34→0
        linkPath(new int[]{25,26,35});
        linkPath(new int[]{27,28,35});
        linkPath(new int[]{29,30,35});
        linkPath(new int[]{35,31,32});
        linkPath(new int[]{35,33,34});
        // 28(Center)→0 출발점(완주) 연결
        center.next = find(0); find(0).prev = center;

        // 3) 교차점 표시 및 최단 경로(shortcut) 설정
        int[][] shortcuts = {
                {34,0}, {5,25}, {10,27}, {15,29}, {32,20}
        };
        for (int[] sc : shortcuts) {
            BoardNode corner = find(sc[0]);
            corner.isIntersection = true;
            corner.shortcut = find(sc[1]);
        }
    }

    private void linkPath(int[] path) {
        for (int i = 0; i < path.length - 1; i++) {
            BoardNode a = find(path[i]);
            BoardNode b = find(path[i+1]);
            a.next = b; b.prev = a;
        }
    }

    private BoardNode find(int id) {
        return nodes.stream().filter(n->n.id==id).findFirst().orElse(null);
    }

    @Override
    protected void buildBoard() {}

    @Override
    public void printBoard() {

    }
}

class HexagonBoard extends PolygonBoard {
    public HexagonBoard() {
        super(6, 5); // 6변 × 5칸 = 30칸 외곽

        // 1) 내부 노드(30~41) 및 중심(42) 생성
        int[] innerIds = {30,31,32,33,34,35,36,37,38,39,40,41,42};
        for (int id : innerIds) {
            BoardNode node = new BoardNode(id, String.valueOf(id));
            nodes.add(node);
        }
        BoardNode center = find(42);
        center.isIntersection = true;

        // 2) 내부 노드 간 외곽(다음) 연결 (X자 경로)
        // 경로: 5→30→31→42, 10→32→33→42, 15→34→35→42, 20→36→37→42, 42→38→39→25, 42→40→41→0
        linkPath(new int[]{30, 31, 42});
        linkPath(new int[]{32, 33, 42});
        linkPath(new int[]{34, 35, 42});
        linkPath(new int[]{36, 37, 42});
        linkPath(new int[]{42, 38, 39});
        linkPath(new int[]{42, 40, 41});
        // 28(Center)→0 출발점(완주) 연결
        center.next = find(0);
        find(0).prev = center;

        // 3) 교차점 표시 및 최단 경로(shortcut) 설정
        int[][] shortcuts = {
                {41, 0}, {5, 30}, {10, 32}, {15, 34}, {20, 36}, {39, 25}
        };
        for (int[] sc : shortcuts) {
            BoardNode corner = find(sc[0]);
            corner.isIntersection = true;
            corner.shortcut = find(sc[1]);
        }
    }

    private void linkPath(int[] path) {
        for (int i = 0; i < path.length - 1; i++) {
            BoardNode a = find(path[i]);
            BoardNode b = find(path[i+1]);
            a.next = b; b.prev = a;
        }
    }

    private BoardNode find(int id) {
        return nodes.stream().filter(n->n.id==id).findFirst().orElse(null);
    }

    @Override
    protected void buildBoard() {

    }

    @Override
    public void printBoard() {

    }
}
// test
