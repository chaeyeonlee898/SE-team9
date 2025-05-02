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
    public Board(int i, int i1) { buildBoard(); }
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

        // 0. 빽도 처리
        if (steps == -1) {
            if (piece.position == null || piece.moveHistory.isEmpty()) {
                System.out.println("빽도 불가. 제자리에 머뭅니다."); return false;
            }
            BoardNode prev = piece.moveHistory.pop();
            if (piece.position.id == 24 && !piece.moveHistory.isEmpty() && piece.moveHistory.peek().id == 28)
                prev = piece.moveHistory.pop();
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
        boolean first = (piece.position==null);
        BoardNode src = first? getStart(): piece.position;
        if (first) piece.hasLeftStart=false;

        // 2. 경로 계산
        List<BoardNode> path = new ArrayList<>();
        BoardNode cur = src;
        boolean corner5 = (src.id==5);
        boolean corner10 = (src.id==10);
        // 오각형: 첫 세 꼭지점(5,10,15)만 중앙길로, 4번째(20)는 제외
        boolean cornerPent = this instanceof PentagonBoard && (src.id==5||src.id==10||src.id==15);
        // 육각형: 마지막 꼭지점(25) 제외한 모든 outer 교차점에서 중앙길
        boolean cornerHex = this instanceof HexagonBoard && src.isIntersection && src.id!=25;
        boolean delay5 = delayed && src.id==5;
        boolean delay28= delayed && src.id==28;
        for (int i=0;i<steps;i++){
            boolean last=(i==steps-1);
            if (i==0 && (corner10||cornerPent||cornerHex||delay5||delay28) && src.shortcut!=null) cur=src.shortcut;
            else if (last && cur!=src && cur.isIntersection && cur.shortcut!=null && (corner10||(!corner5 && cur.id!=28))) cur=cur.shortcut;
            else cur=cur.next;
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
        // 외곽 next/prev 연결
        for (int i = 0; i < outer.length; i++) {
            BoardNode curr = outer[i];
            BoardNode nxt = outer[(i + 1) % outer.length];
            curr.next = nxt;
            nxt.prev = curr;
        }
        start = outer[0];
    }
    @Override protected void buildBoard() {}
    @Override public void printBoard() {}
}

//────────────────────────────────────────────────────────────
// SquareBoard 클래스: 사각형 윷놀이 보드
class SquareBoard extends PolygonBoard {
    public SquareBoard() {
        super(4, 5); // 4변 × 5칸 = 20칸 외곽
        // 내부 노드(20~28) 생성
        int[] inner = {20,21,22,23,24,25,26,27,28};
        for (int id : inner) {
            nodes.add(new BoardNode(id, String.valueOf(id)));}
        // 중심 교차점 설정
        BoardNode center = find(28);
        center.isIntersection = true;
        // 내부 경로 연결
        linkPath(new int[]{20,21,28}); linkPath(new int[]{22,23,28});
        // 중심에서 외곽으로 두 갈래
        linkPath(new int[]{28,24,25,15}); linkPath(new int[]{26,27,0});
        // shortcut 설정
        int[][] sc = {{5,20},{10,22},{15,25},{0,27},{28,26}};
        for (int[] s : sc) {
            BoardNode c = find(s[0]);
            c.isIntersection = true;
            c.shortcut = find(s[1]);
        }
    }
    private void linkPath(int[] p) {
        for (int i = 0; i < p.length-1; i++) {
            BoardNode a = find(p[i]), b = find(p[i+1]);
            a.next = b; b.prev = a;
        }
    }
    private BoardNode find(int id) {
        return nodes.stream().filter(n->n.id==id).findFirst().orElse(null);
    }
    @Override protected void buildBoard() {}
}

//────────────────────────────────────────────────────────────
// PentagonBoard 클래스
class PentagonBoard extends PolygonBoard {
    public PentagonBoard() {
        super(5,5);
        int[] inner = {25,26,27,28,29,30,31,32,33,34,35};
        for (int id : inner) nodes.add(new BoardNode(id, String.valueOf(id)));
        BoardNode center = find(35);
        center.isIntersection = true;
        linkPath(new int[]{25,26,35}); linkPath(new int[]{27,28,35});
        linkPath(new int[]{29,30,35}); linkPath(new int[]{31,32,20});
        linkPath(new int[]{33,34,0});
        center.next = find(0); find(0).prev = center;
        int[][] sc = {{5,25},{10,27},{15,29},{32,20},{34,0}};
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
