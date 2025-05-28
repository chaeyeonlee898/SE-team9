package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 추상 보드 클래스: 다양한 다각형 보드에서 말 이동 로직을 공통으로 처리
 */
public abstract class Board {
    protected BoardNode start;                             // 출발(완주) 지점 노드
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

        debugPrintAllNodes();
        System.out.println("[DEBUG] " + piece.owner.getName() + " 호출 movePiece(steps=" + steps + ")");

        // 2. 빽도 처리
        if (steps == BACKDO) return handleBackdo(piece, src);

        // 3. 업(스택) 기능
        List<Piece> stack = collectStack(first, piece, src);

        // 4. “start에서 positive step” 완주 처리
        if (isFinishedPath(first, src, piece, steps)) {
            finishPieces(stack);
            return false;
        }

        // 5. 경로 설정
        List<BoardNode> path = calculatePath(src, steps);
        BoardNode dest = path.get(path.size() - 1);
        piece.justStoppedAtIntersection = dest.isIntersection && dest.shortcut!=null;


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
        System.out.println("현재 moveHistory: " + piece.moveHistory);

        return captureOccured;
    }

    private boolean handleBackdo(Piece piece, BoardNode src){

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

            // 업힌 말들 가져오기
            List<Piece> stack = new ArrayList<>();
            for (Piece p : new ArrayList<>(piece.position.pieces)) {
                if (p.owner == piece.owner) {
                    stack.add(p);
                }
            }

            //현재 위치에서 제거
            piece.position.pieces.remove(piece);

            // 캡쳐 처리
            boolean captureByBackdo = handleCapture(piece, prev);

            // 이동
            for (Piece p : stack) {
                if (!p.moveHistory.isEmpty()) {
                    p.moveHistory.pop();  // 현재 위치 제거
                }
                if (p.moveHistory.isEmpty() || p.moveHistory.peek() != prev) {
                    p.moveHistory.push(prev);
                }
                // 일반 이동에 들어가기 직전, lastPosition 에 현재 위치 저장
                // 첫 진입인 경우 start 가 이전 위치
                p.lastPosition = p.position == null ? getStart() : p.position;
                // 이전 위치에서 제거
                if (p.position != null) {
                    p.position.pieces.remove(p);
                }
                // 현재 위치 기억
                //BoardNode current = p.position;
                prev.pieces.add(p);
                p.position = prev;

                // 빽도 후에도 intersection 여부 유지
                p.justStoppedAtIntersection = prev.isIntersection && prev.shortcut != null;
                            }

            System.out.println(piece.owner.getName() + "의 말이 빽도로 " + prev + "로 한 칸 뒤로 이동했습니다.");
            System.out.println("현재 moveHistory: " + piece.moveHistory);

        return captureByBackdo;
    }

    private boolean backdoFromStart(Piece piece){
        // lastPosition 으로 돌아가기
        BoardNode prev = piece.lastPosition;

        // START에 올라와 있는 같은 소유주의 말 모두 수집
        List<Piece> stack = new ArrayList<>();
        for (Piece p : new ArrayList<>(getStart().pieces)) {
            if (p.owner == piece.owner) {
                stack.add(p);
            }
        }

        // START에서 모두 제거
        for (Piece p : stack) {
            getStart().pieces.remove(p);
        }

        // 모두 이전 위치(prev)로 이동
        for (Piece p : stack) {
            prev.pieces.add(p);
            p.position = prev;
            // 이력 초기화: START → prev
            p.moveHistory.clear();
            p.moveHistory.push(getStart());
            p.moveHistory.push(prev);
            p.justStoppedAtIntersection = prev.isIntersection && prev.shortcut != null;
        }

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
            if (p.position != null) {
                p.position.pieces.remove(p);
                p.position = null;
            }
            System.out.println(p.owner.getName() + "의 말이 완주했습니다!");
        }
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
        boolean squareboardException = false;

        // 1) 사각형 판 예외 처리
        BoardNode temp = cur;
        if(this instanceof SquareBoard && temp.id  == 23 && temp.next.id == 28){  // 사각형 판에서 23 -> 28로 가는 경우
            squareboardException = true;         // 숏컷(26 방향)으로 가도록 플래그 설정
            System.out.println("SquareBoardException");
        }

        // 2) 교차점 여부에 따라 한 칸 이동
        if((cornerSquare || cornerPent || cornerHex) && cur.shortcut != null)
            cur = cur.shortcut;  // 교차점(마지막 제외)이면 shortcut
        else
            cur = cur.next;  // 아니면 다음 노드로
        path.add(cur);

        // 3) 나머지 경로 이동
        for(int i=1; i<steps ;i++){
            if(this instanceof SquareBoard && cur.id  == 23 && cur.next.id == 28){  // 사각형 판에서 23 -> 28로 가는 경우
                squareboardException = true;         // 숏컷(26 방향)으로 가도록 플래그 설정
                System.out.println("SquareBoardException");
            }
            if(squareboardException && cur.id == 28){
                System.out.println("Move: "+cur.id+"->"+cur.shortcut.id);
                cur = cur.shortcut;
                squareboardException = false;
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
            // path의 첫 노드가 src일 경우 push하지 않음
            if ((path.isEmpty() || path.get(0) != src)||(p.moveHistory!=null&&p.moveHistory.peek()!=src)) {
                p.moveHistory.push(src);
            }
            for(BoardNode n:path)
                p.moveHistory.push(n);
            dest.pieces.add(p);
            p.position=dest;
        }
    }
    public void debugPrintAllNodes(){ System.out.print("[DEBUG BOARD] "); for(BoardNode n:nodes) System.out.print(n.id+"["+n.pieces.size()+"] "); System.out.println(); }
    public abstract void printBoard();

    // Board.java 내부
    public List<BoardNode> getAllNodes() {
        return nodes;
    }
}