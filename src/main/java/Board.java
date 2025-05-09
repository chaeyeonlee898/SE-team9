import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 추상 보드 클래스: 다양한 다각형 보드에서 말 이동 로직을 공통으로 처리
 */
abstract class Board {
    protected BoardNode start;                // 출발(완주) 지점 노드
    protected final List<BoardNode> nodes = new ArrayList<>();  // 모든 노드
    private static final int BACKDO = -1;
    public Board() { buildBoard(); }
    protected abstract void buildBoard();
    public BoardNode getStart() { return start; }
    private boolean isFirstEntry(Piece piece){ return piece.position == null;}
    private BoardNode getSourceNode(Piece piece, boolean first){ return first? getStart(): piece.position;}

    public boolean movePiece(Piece piece, int steps, Scanner scanner) {
        // 1. 첫 entry 및 업 플래그
        boolean first = isFirstEntry(piece); //처음 입장하는 경우 플래그
        if (first) piece.hasLeftStart=true;
        BoardNode src = getSourceNode(piece, first); //현재 보드노드(추적)

        System.out.println("[DEBUG] " + piece.owner.getName() + " 호출 movePiece(steps=" + steps + ")");
        debugPrintAllNodes();

        // 2. 빽도 처리
        if (steps == BACKDO) return handleBackdo(piece);

        // 3. “start에서 positive step” 완주 처리
        if (isFinishedPath(first, src, piece, steps)) {
            finishPieces(piece);
            return false;
        }

        // 4. 경로 설정
        List<BoardNode> path = calculatePath(src, steps);
        BoardNode dest = path.getLast();
        piece.justStoppedAtIntersection = dest.isIntersection && dest.shortcut!=null;

        // 5. 업(스택) 기능
        List<Piece> stack = collectStack(first, piece, src);


        // 6. 완주 처리: start를 한 칸 이상 지나쳐야 완주로 간주
        if (isFinishedPath(first, path)) {
            finishPieces(stack);
            return false;
        }

        // 7. 이동 및 이력
        recordAndMove(stack, src, path, dest);

        // 8. 캡처
        boolean captureOccured = handleCapture(piece, dest);

        System.out.println(piece.owner.getName()+"의 말("+stack.size()+"개)이 "+dest+"로 이동했습니다.");
        return captureOccured;
    }

    private boolean handleBackdo(Piece piece){
        
            // a) 아직 보드에 안 올랐으면 불가
            if (piece.position == null) {
                System.out.println("아직 보드에 진입하지 않아 빽도 불가.");
                return false;
            }
            System.out.println("[DEBUG] 이전 위치 : " + piece.lastPosition);

            // b) 게임 시작부터 빽도
            if (piece.position == getStart() && piece.hasLeftStart && piece.moveHistory.size() == 1) {
                return backdoFromStart(piece);
            }

            // c) 이동 이력 없으면 백도 불가
            if (piece.moveHistory.isEmpty()) {
                System.out.println("이전 위치 정보가 없어 빽도 불가.");
                return false;
            }

            // d) 정상적인 한 칸 뒤로 이동
            if (piece.moveHistory.size() > 1) {
                piece.moveHistory.pop();
            }
            BoardNode prev = piece.moveHistory.peek();
            piece.position.pieces.remove(piece);

            // 캡쳐 처리
            boolean captureByBackdo = handleCapture(piece, prev);

            // 일반 이동에 들어가기 직전, lastPosition 에 현재 위치 저장
            // 첫 진입인 경우 start 가 이전 위치
            piece.lastPosition = piece.position == null ? getStart() : piece.position;
            prev.pieces.add(piece);
            piece.position = prev;
            // 빽도 후에도 intersection 여부 유지
            piece.justStoppedAtIntersection = prev.isIntersection && prev.shortcut != null;

            System.out.println(piece.owner.getName() + "의 말이 빽도로 " + prev + "로 한 칸 뒤로 이동했습니다.");
            return captureByBackdo;
    }

    private boolean backdoFromStart(Piece piece){
        // 1) lastPosition 으로 돌아가기
        BoardNode prev = piece.lastPosition;

        // 2) 보드 상 위치 갱신
        getStart().pieces.remove(piece);
        prev.pieces.add(piece);
        piece.position = prev;
        piece.justStoppedAtIntersection = prev.isIntersection && prev.shortcut != null;

        // 3) moveHistory 재설정: [start, prev]
        piece.moveHistory.clear();
        piece.moveHistory.push(getStart());
        piece.moveHistory.push(prev);

        System.out.println("start 지점에서 빽도! → 이전 위치로 돌아갑니다.");
        return false;
    }

    private boolean isFinishedPath(boolean first, BoardNode src, Piece piece, int steps){
        return !first && src == getStart() && piece.hasLeftStart && steps > 0;
    }

    private boolean isFinishedPath(boolean first, List<BoardNode> path){
        return !first && path.stream().limit(path.size() - 1).anyMatch(n -> n == start);
    }

    private void finishPieces(List<Piece> stack){
        for (Piece p : stack) {
            p.finished = true;
            System.out.println(p.owner.getName() + "의 말이 완주했습니다!");
        }
    }

    private void finishPieces(Piece piece) {
        List<Piece> singleton = new ArrayList<>();
        singleton.add(piece);
        finishPieces(singleton);
    }


    private boolean handleCapture(Piece piece, BoardNode curr){
        boolean captured = false;
        for (Piece enemy : new ArrayList<>(curr.pieces)) {
            if (enemy.owner != piece.owner) {
                captured = true;
                curr.pieces.remove(enemy);
                enemy.position = null;
                System.out.println(enemy.owner.getName()+"의 말이 캡처되어 집으로 돌아갑니다.");
            }
        }
        return captured;
    }

    private List<BoardNode> calculatePath(BoardNode src, int steps){
        BoardNode cur = src;
        List<BoardNode> path = new ArrayList<>();
        // 마지막 꼭지점(15, 20, 25) 제외한 모든 outer 교차점에서 중앙길
        boolean cornerSquare = this instanceof SquareBoard && src.isIntersection && src.id!=15;
        boolean cornerPent = this instanceof PentagonBoard && src.isIntersection && src.id!=20;
        boolean cornerHex = this instanceof HexagonBoard && src.isIntersection && src.id!=25;
        // 교차점 처리 플래그 - 기본 & 사각형판 예외
        boolean stopatIntersection = false;
        boolean squareboardException = false;

        // 1) 교차점 여부에 따라 한 칸 이동
        if((cornerSquare || cornerPent || cornerHex) && cur.shortcut != null)
            cur = cur.shortcut;  // 교차점(마지막 제외)이면 shortcut
        else
            cur = cur.next;  // 아니면 다음 노드로
        path.add(cur);

        // 2) 사각형 판 예외 처리
        BoardNode temp = cur;
        for (int i = 0; i < steps-1; i++) {
            if(temp.id ==23 && temp.next.id == 28){  // 사각형 판에서 23 -> 28로 가는 경우
                squareboardException = true;         // 숏컷(26 방향)으로 가도록 플래그 설정
                System.out.println("SquareBoardException");
            }
            if(squareboardException && temp.id == 28)
                temp = temp.shortcut;
            else
                temp = temp.next;
            System.out.println("temp: "+temp.id);
        }
        if(temp.isIntersection || squareboardException){  // 숏컷 적용하는 경우(교차점 or 사각형판 예외)
            stopatIntersection = true;
        }

        // 3) 나머지 경로 이동
        for(int i=1; i<steps ;i++){
            if(i==steps-1 && cur.shortcut!=null && stopatIntersection){ // 숏컷 이동
                cur = cur.shortcut;
                stopatIntersection = false;
            }
            else  // 기존 경로 이동
                cur = cur.next;
            path.add(cur);
        }

        return path;
    }

    private List<Piece> collectStack(boolean first, Piece piece, BoardNode src){
        List<Piece> stack = new ArrayList<>();
        if (first) stack.add(piece);
        else {
            for(Piece p:new ArrayList<>(src.pieces)) if(p.owner==piece.owner) stack.add(p);
            src.pieces.removeAll(stack);
        }
        return stack;
    }

    private void recordAndMove(List<Piece> stack, BoardNode src, List<BoardNode> path, BoardNode dest){
        for(Piece p:stack){
            p.moveHistory.push(src);
            for(BoardNode n:path)
                p.moveHistory.push(n);
            dest.pieces.add(p);
            p.position=dest;
        }
    }
    public void debugPrintAllNodes(){ System.out.print("[DEBUG BOARD] "); for(BoardNode n:nodes) System.out.print(n.id+"["+n.pieces.size()+"] "); System.out.println(); }
    public abstract void printBoard();
}

//────────────────────────────────────────────────────────────
// PolygonBoard 클래스: 외곽 다각형 보드 기본 구현
class PolygonBoard extends Board {
    protected final BoardNode[] outer;
    public PolygonBoard(int sides, int nodesPerSide) {
        outer = new BoardNode[sides * nodesPerSide];
        // 외곽 노드 생성
        for (int i = 0; i < outer.length; i++) {
            outer[i] = new BoardNode(i, String.valueOf(i));
            nodes.add(outer[i]);
        }
        // 외곽 노드를 순환 연결
        linkCycle(outer);
        // 시작점 설정(기존대로)
        start = outer[0];
    }

    /** 주어진 노드들을 순서대로 연결(비순환) */
    protected void linkPath(BoardNode... path) {
        for (int i = 0; i < path.length - 1; i++) {
            path[i].next = path[i+1];
            path[i+1].prev = path[i];
        }
    }
    /** 주어진 노드들을 순환 구조로 연결(마지막→첫번째까지) */
    protected void linkCycle(BoardNode... cycle) {
        for (int i = 0; i < cycle.length; i++) {
            BoardNode curr = cycle[i];
            BoardNode next = cycle[(i+1) % cycle.length];
            curr.next = next;
            next.prev = curr;
        }
    }
    protected BoardNode[] createInnerNodes(int[] innerIds){
        BoardNode[] innerNodes = new BoardNode[innerIds.length];
        for (int i = 0; i < innerIds.length; i++) {
            innerNodes[i] = new BoardNode(innerIds[i], String.valueOf(innerIds[i]));
            nodes.add(innerNodes[i]);
        }
        return innerNodes;
    }
    protected void setShortcuts(int[][] sc){
        for (int[] pair : sc) {
            BoardNode outerNode = find(pair[0]);   // ex. ID=5 노드
            BoardNode target    = find(pair[1]);   // ex. ID=30 노드
            outerNode.isIntersection = true;
            outerNode.shortcut       = target;
        }
    }
    protected BoardNode find(int id) {return nodes.stream().filter(n->n.id==id).findFirst().orElse(null);}
    @Override protected void buildBoard() {}
    @Override public void printBoard() {}
}

//────────────────────────────────────────────────────────────
// SquareBoard 클래스: 사각형 윷놀이 보드
//────────────────────────────────────────────────────────────
class SquareBoard extends PolygonBoard {
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

//────────────────────────────────────────────────────────────
// PentagonBoard 클래스
//────────────────────────────────────────────────────────────
class PentagonBoard extends PolygonBoard {
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

//────────────────────────────────────────────────────────────
//HexagonBoard 클래스
//────────────────────────────────────────────────────────────
class HexagonBoard extends PolygonBoard {
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
