import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 추상 보드 클래스: 다양한 다각형 보드에서 말 이동 로직을 공통으로 처리
 */
abstract class Board {
    protected BoardNode start;                // 출발(완주) 지점 노드
    protected final List<BoardNode> nodes = new ArrayList<>();  // 모든 노드
    public Board() { buildBoard(); }
    protected abstract void buildBoard();
    public BoardNode getStart() { return start; }

    /**
     * 한 칸 이동: 마지막 스텝에만 shortcut 적용. 10번 노드에서는 첫 스텝에도 shortcut.
     */
    public BoardNode getNextNode(BoardNode current, int steps) {
        BoardNode temp = current;
        for (int i = 0; i < steps; i++) {
            if (temp == null) break;
            if (i == 0 && temp.id == 10 && temp.shortcut != null) {
                temp = temp.shortcut;
            } else {
                boolean last = (i == steps - 1);
                if (last && temp != start && temp.isIntersection && temp.shortcut != null) temp = temp.shortcut;
                else temp = temp.next;
            }
            if (temp == start && i < steps - 1) return start;
        }
        return temp;
    }

    /**
     * 말 이동: 5번,10번,28번에 대한 특별 숏컷, 지연 숏컷, 빽도, 업, 캡처 처리
     */
    public boolean movePiece(Piece piece, int steps, Scanner scanner) {
        System.out.println("[DEBUG] " + piece.owner.getName() + " 호출 movePiece(steps=" + steps + ")");
        boolean delayed = piece.justStoppedAtIntersection;

        // 기본: outer 따라서
        // curr 노드가 intersection이면 shortcut으로(마지막 intersection 제외)
        // 0. 빽도 처리
        if (steps == -1) {
            if (piece.position == null || piece.moveHistory.isEmpty()) {
                System.out.println("빽도 불가. 제자리에 머뭅니다."); return false;
            }
            // 1) 현재 위치 제거
            piece.moveHistory.pop();
            // 2) 한 칸 전 위치 꺼내기
            BoardNode prev = piece.moveHistory.pop();
            //if (piece.position.id == 24 && !piece.moveHistory.isEmpty() && piece.moveHistory.peek().id == 28)
              //  prev = piece.moveHistory.pop();
            piece.position.pieces.remove(piece);
            boolean cap=false;
            for (Piece e : new ArrayList<>(prev.pieces)) if (e.owner!=piece.owner) {
                cap=true; prev.pieces.remove(e); e.position=null;
                System.out.println("[DEBUG] 빽도로 캡처: " + e.owner.getName());
            }
            prev.pieces.add(piece); piece.position = prev;
            // backdo 후 위치가 교차점인지 플래그 설정
            piece.justStoppedAtIntersection = (prev.isIntersection && prev.shortcut != null);
            return cap;
        }

        // 1. 첫 entry 및 업 플래그
        boolean first = (piece.position==null); //처음 입장하는 경우
        BoardNode src = first? getStart(): piece.position; //현재 보드노드(추적)
        if (first) piece.hasLeftStart=false;

        //2. 경로 설정
        BoardNode cur = src;
        List<BoardNode> path = new ArrayList<>();
        // 마지막 꼭지점(15, 20, 25) 제외한 모든 outer 교차점에서 중앙길
        boolean cornerSquare = this instanceof SquareBoard && src.isIntersection && src.id!=15;
        boolean cornerPent = this instanceof PentagonBoard && src.isIntersection && src.id!=20;
        boolean cornerHex = this instanceof HexagonBoard && src.isIntersection && src.id!=25;

        boolean stopatIntersection = false;
        boolean squareboardException = false;
        if((cornerSquare || cornerPent || cornerHex) && cur.shortcut != null)
            cur = cur.shortcut;
        else
            cur = cur.next;
        path.add(cur);
        BoardNode temp = cur;
        for (int i = 0; i < steps-1; i++) {
            if(temp.id ==23 && temp.next.id == 28){
                squareboardException = true;
                System.out.println("SquareBoardException");
            }
            if(squareboardException && temp.id == 28)
                temp = temp.shortcut;
            else
                temp = temp.next;
            System.out.println("temp: "+temp.id);
        }
        if(temp.isIntersection || squareboardException){
            stopatIntersection = true;
        }

        for(int i=1;i<steps;i++){
            if(i==steps-1 && cur.shortcut!=null && stopatIntersection){
                cur = cur.shortcut;
                stopatIntersection = false;
            }
            else
                cur = cur.next;
            path.add(cur);
        }
        BoardNode dest=cur;
        piece.justStoppedAtIntersection = dest.isIntersection && dest.shortcut!=null;
        if (src.id==5 && !delayed) {
            BoardNode fb=src; for(int i=0;i<steps;i++) fb=fb.next; dest=fb;
        }

        // 3) 완주 처리: start를 한 칸 이상 지나쳐야 완주로 간주
        if (!first && path.stream().limit(path.size() - 1).anyMatch(n -> n == start)) {
            piece.finished = true;
            System.out.println(piece.owner.getName() + "의 말이 완주했습니다!");
            return false;
        }

        // 4) 업(스택) 기능
        List<Piece> stack=new ArrayList<>();
        if (first) stack.add(piece);
        else {
            for(Piece p:new ArrayList<>(src.pieces)) if(p.owner==piece.owner) stack.add(p);
            src.pieces.removeAll(stack);
        }

        // 5. 캡처
        boolean capO=false;
        for(Piece e:new ArrayList<>(dest.pieces)) if(e.owner!=piece.owner){capO=true; dest.pieces.remove(e); e.position=null;
            System.out.println(e.owner.getName()+"의 말이 캡처되어 집으로 돌아갑니다.");}

        // 6. 이동 및 이력
        for(Piece p:stack){ p.moveHistory.push(src); for(BoardNode n:path) p.moveHistory.push(n); dest.pieces.add(p); p.position=dest; }
        System.out.println(piece.owner.getName()+"의 말("+stack.size()+"개)이 "+dest+"로 이동했습니다.");
        debugPrintAllNodes(); return capO;
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
    @Override protected void buildBoard() {}
    @Override public void printBoard() {}
}

//────────────────────────────────────────────────────────────
// SquareBoard 클래스: 사각형 윷놀이 보드
class SquareBoard extends PolygonBoard {
    public SquareBoard() {
        super(4, 5); // 4변 × 5칸 = 20칸 외곽

        // 1) 내부 노드 생성 및 배열 저장
        int[] innerIds = {20,21,22,23,24,25,26,27,28};
        BoardNode[] innerNodes = new BoardNode[innerIds.length];
        for (int i = 0; i < innerIds.length; i++) {
            innerNodes[i] = new BoardNode(innerIds[i], String.valueOf(innerIds[i]));
            nodes.add(innerNodes[i]);
        }
        // 2) 중심 교차점 설정
        BoardNode center = innerNodes[8];  // id == 28
        // 3) 내부 경로 연결
        linkPath(innerNodes[0], innerNodes[1], innerNodes[8]); // 20 → 21 → 28
        linkPath(innerNodes[2], innerNodes[3], innerNodes[8]); // 22 → 23 → 28
        // 4) 중심에서 외곽으로 두 갈래
        linkPath(innerNodes[8], innerNodes[4], innerNodes[5], outer[15]); // 28 → 24 → 25 → 15
        linkPath(innerNodes[6], innerNodes[7], outer[0]); // 26 → 27 → 0

        // 5) shortcut 설정 (직접 참조 사용)
        outer[5].isIntersection   = true;
        outer[5].shortcut         = innerNodes[0];  // 5 → 20

        outer[10].isIntersection  = true;
        outer[10].shortcut        = innerNodes[2];  // 10 → 22

        center.isIntersection     = true;           // (center == innerNodes[8], id=28)
        center.shortcut           = innerNodes[6];  // 28 → 26

    }
    @Override protected void buildBoard() {
        super.buildBoard();
    }
}

//────────────────────────────────────────────────────────────
// PentagonBoard 클래스
class PentagonBoard extends PolygonBoard {
    public PentagonBoard() {
        super(5,5);

        // 1) 내부 노드 생성 및 배열 저장
        int[] innerIds = {25,26,27,28,29,30,31,32,33,34,35};
        BoardNode[] innerNodes = new BoardNode[innerIds.length];
        for (int i = 0; i < innerIds.length; i++) {
            innerNodes[i] = new BoardNode(innerIds[i], String.valueOf(innerIds[i]));
            nodes.add(innerNodes[i]);
        }
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
        for (int[] s : sc) {
            BoardNode c = find(s[0]);
            c.isIntersection = true;
            c.shortcut = find(s[1]);
        }
        center.isIntersection     = true;           // (center == innerNodes[10], id=35)
        center.shortcut           = innerNodes[8];  // 35 → 33

    }
    private BoardNode find(int id) {return nodes.stream().filter(n->n.id==id).findFirst().orElse(null);}
    @Override protected void buildBoard() {}
    @Override public void printBoard() {}
}

//────────────────────────────────────────────────────────────
// HexagonBoard 클래스
class HexagonBoard extends PolygonBoard {
    public HexagonBoard() {
        super(6,5);
        int[] inner = {30,31,32,33,34,35,36,37,38,39,40,41,42};
        for (int id : inner) nodes.add(new BoardNode(id,String.valueOf(id)));
        BoardNode center = find(42);
        center.isIntersection = true;
        linkPath(new int[]{30,31,42}); linkPath(new int[]{32,33,42});
        linkPath(new int[]{34,35,42}); linkPath(new int[]{36,37,42});
        linkPath(new int[]{38,39,25}); linkPath(new int[]{40,41,0});
        center.next = find(0); find(0).prev = center;
        int[][] sc = {{5,30},{10,32},{15,34},{20,36},{39,25},{41,0}};
        for (int[] s : sc) {
            BoardNode c = find(s[0]);
            c.isIntersection = true;
            c.shortcut = find(s[1]);
        }
    }
    private void linkPath(int[] p) { for (int i=0;i<p.length-1;i++){BoardNode a=find(p[i]),b=find(p[i+1]);a.next=b;b.prev=a;} }
    private BoardNode find(int id) {return nodes.stream().filter(n->n.id==id).findFirst().orElse(null);}    
    @Override protected void buildBoard() {}
    @Override public void printBoard() {}
}
