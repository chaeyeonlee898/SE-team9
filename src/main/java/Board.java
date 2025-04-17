//Board.java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

abstract class Board {
    protected BoardNode start;  // 출발(시작) 노드

    public Board() {
        buildBoard();
    }

    // 각 하위 보드에서 보드 노드 그래프를 구성
    protected abstract void buildBoard();

    public BoardNode getStart() {
        return start;
    }

    // 현재 노드에서 steps만큼 이동한 노드를 계산합니다.
    // 첫 스텝에서는, 만약 현재 노드가 시작(0번)이라면 shortcut은 무시하고 외곽 길을 따라 갑니다.
    public BoardNode getNextNode(BoardNode current, int steps, Scanner scanner) {
        BoardNode temp = current;
        boolean isShortcut = false;

        if (temp.isIntersection) { //temp: 시작점 제외한 교차점이면
            isShortcut = true;
            System.out.println("교차점 " + temp + "에 도착했습니다.");
        }
        for (int i = 0; i < steps; i++) {
            if (temp == null) break;
            // BoardNode prevNode = temp;
            if(isShortcut) {  //최단경로로 이동해야 하는 경우
                if(temp.isIntersection){ //temp가 중앙점에 있으면 shortcut 방향으로 이동
                    temp = temp.shortcut;
                }
                else{
                    temp = temp.next;
                }
            }
            else{ //두번째 경로로 이동해야 하는 경우
                temp = temp.next; //두 번째 경로로 이동
            }
            // temp.prev = prevNode;
            // #3 issue) 말이 완주 후 시작점을 지나면 게임 종료
            if (temp == getStart() && current != getStart()) {
                return getStart();
            }
        }
        return temp;
    }

    // 업(스택) 기능 포함 movePiece() 메서드.
    // 만약 말이 아직 보드에 진입하지 않았다면(position == null),
    // 그 말을 보드에 진입시키고 그 다음 이동 계산을 수행합니다.
    // 그리고 상대 말을 잡으면 "집" (position == null)으로 돌려보냅니다.1
    public boolean movePiece(Piece piece, int steps, Scanner scanner) {
        // 0) 빽도 처리: 한 칸 뒤로
        if (steps == -1) {
            if (piece.position == null) {
                System.out.println("아직 보드에 진입하지 않아 빽도 불가. 제자리에 머무릅니다.");
                return false;
            }
            if (piece.moveHistory.isEmpty()) {
                System.out.println("이전 위치 정보가 없어 빽도 불가. 제자리에 머무릅니다.");
                return false;
            }

            BoardNode prev = piece.moveHistory.pop();
            piece.position.pieces.remove(piece);

            // 빽도로 캡처 검사
            boolean captureOccurred = false;
            for (Piece enemy : new ArrayList<>(prev.pieces)) {
                if (enemy.owner != piece.owner) {
                    captureOccurred = true;
                    prev.pieces.remove(enemy);
                    enemy.position = null;
                    System.out.println(enemy.owner.getName()
                        + "의 말이 빽도에 의해 캡처되어 집으로 돌아갑니다.");
                }
            }

            prev.pieces.add(piece);
            piece.position = prev;
            System.out.println(piece.owner.getName()
                + "의 말이 빽도로 " + prev + "로 한 칸 뒤로 이동했습니다.");
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
            cur = getNextNode(cur, 1, scanner);
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
            + "의 업(" + stack.size() + "개)이 " + dest + "로 이동했습니다.");
        return captureOccurred;
    }

    public abstract void printBoard();
}

//────────────────────────────────────────────────────────────
// SquareBoard 클래스: 전통 윷놀이 사각형 판 (총 29개 노드)
// 외곽 트랙: 0~19 (20개 노드), 0번은 출발 및 완주 지점
// 안쪽 X자 경로: 20~28 (9개 노드)
//  - 코너(0,5,10,15)에서만 shortcut 적용
//  - 처음에는 말이 집에 있으므로 첫 이동 시 board.getStart().next부터 진행
class SquareBoard extends Board {
    @Override
    protected void buildBoard() {
        // 1) 외곽 트랙: 20개 노드 (0~19)
        BoardNode[] outer = new BoardNode[20];
        for (int i = 0; i < 20; i++) {
            outer[i] = new BoardNode(i, String.valueOf(i));
        }
     // 2) 안쪽 X자 경로: 총 9개 노드 (20~28)
        //    각 코너에서 2개의 중간 노드를 거쳐 중앙(28)로 연결
        //    - 코너 0: 0 → 20 → 21 → 28
        //    - 코너 5: 5 → 22 → 23 → 28
        //    - 코너 10: 10 → 24 → 25 → 28
        //    - 코너 15: 15 → 26 → 27 → 28
        BoardNode n20 = new BoardNode(20, "20");
        BoardNode n21 = new BoardNode(21, "21");
        BoardNode n22 = new BoardNode(22, "22");
        BoardNode n23 = new BoardNode(23, "23");
        BoardNode n24 = new BoardNode(24, "24");
        BoardNode n25 = new BoardNode(25, "25");
        BoardNode n26 = new BoardNode(26, "26");
        BoardNode n27 = new BoardNode(27, "27");
        BoardNode center = new BoardNode(28, "Center");
        
        for (int i = 0; i < 19; i++) {
            outer[i].next = outer[i+1];
            outer[i+1].prev = outer[i];
        }
        outer[19].next = outer[0];
        outer[0].prev = outer[19];
        
        n20.prev = n21;  n21.prev = center;
        n22.prev = outer[5];  n23.prev = n22;
        n24.prev = outer[10]; n25.prev = n24;
        n26.prev = n27;  n27.prev = center;
        center.prev = n25;
        start = outer[0];  // 출발(0번)

        //교차점 지정 - 5, 10, 15, center
        outer[5].isIntersection = true;
        outer[10].isIntersection = true;
        outer[15].isIntersection = true;
        center.isIntersection = true;

        // shortcut 설정 (최단 경로로 갈 때 바로 다음 노드)
        outer[5].shortcut  = n22;  // 코너 0 → n20
        outer[10].shortcut  = n24;  // 코너 5 → n22
        outer[15].shortcut = outer[16];  // 코너 10 → n24
        center.shortcut = n21;  // 코너 15 → n26

        // 기타 경로 설정 - next
        n22.next = n23;   n23.next = center;
        n24.next = n25;   n25.next = center;
        center.next = n27;  n27.next = n26;
        n21.next = n20;  n20.next = outer[0];
        /*
        //기타 경로 설정 - prev
        n23.prev = n22;     n22.prev = outer[5];
        n25.prev = n24;     n24.prev = outer[10];
        n26.prev = n27;     outer[15].prev = outer[15];
        n21.prev = n20;     n20.prev = outer[5];
        */
    }

    @Override
    public void printBoard() {
        System.out.println("----- 전통 윷놀이 사각형 판 (총 29개 노드) -----");
        BoardNode curr = start;
        for (int i = 0; i < 20; i++) {
            System.out.print(curr + " -> ");
            curr = curr.next;
        }
        System.out.println("(back to 0)");
        System.out.println("안쪽 X자 경로: 코너 0,5,10,15에서 shortcut 적용 → Center(28)에서 분기");
    }
}

//────────────────────────────────────────────────────────────
// PentagonBoard 및 HexagonBoard (간단 예시)
class PentagonBoard extends Board {
    @Override
    protected void buildBoard() {
        BoardNode[] nodes = new BoardNode[17];
        for (int i = 0; i < 17; i++) {
            nodes[i] = new BoardNode(i, "P" + i);
        }
        for (int i = 0; i < 16; i++) {
            nodes[i].next = nodes[i+1];
        }
        nodes[16].next = nodes[0];
        start = nodes[0];
    }

    @Override
    public void printBoard() {
        System.out.println("----- 오각형 판 (간단 예시) -----");
        BoardNode curr = start;
        for (int i = 0; i < 17; i++) {
            System.out.print(curr + " -> ");
            curr = curr.next;
        }
        System.out.println("(back to start)");
    }
}

class HexagonBoard extends Board {
    @Override
    protected void buildBoard() {
        BoardNode[] nodes = new BoardNode[19];
        for (int i = 0; i < 19; i++) {
            nodes[i] = new BoardNode(i, "H" + i);
        }
        for (int i = 0; i < 18; i++) {
            nodes[i].next = nodes[i+1];
        }
        nodes[18].next = nodes[0];
        start = nodes[0];
    }

    @Override
    public void printBoard() {
        System.out.println("----- 육각형 판 (간단 예시) -----");
        BoardNode curr = start;
        for (int i = 0; i < 19; i++) {
            System.out.print(curr + " -> ");
            curr = curr.next;
        }
        System.out.println("(back to start)");
    }
}
// test
