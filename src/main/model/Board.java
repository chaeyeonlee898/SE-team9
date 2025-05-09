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

    // Board.java 내부
    public List<BoardNode> getAllNodes() {
        return nodes;
    }
}