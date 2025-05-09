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

    public Board() {
        buildBoard();
    }

    protected abstract void buildBoard();

    public BoardNode getStart() {
        return start;
    }

    public BoardNode getNextNode(BoardNode current, int steps) {
        BoardNode temp = current;
        for (int i = 0; i < steps; i++) {
            if (temp == null) break;
            if (i == 0 && temp.id == 10 && temp.shortcut != null) {
                temp = temp.shortcut;
            } else {
                boolean last = (i == steps - 1);
                if (last && temp != start && temp.isIntersection && temp.shortcut != null)
                    temp = temp.shortcut;
                else
                    temp = temp.next;
            }
            if (temp == start && i < steps - 1) return start;
        }
        return temp;
    }

    public boolean movePiece(Piece piece, int steps, Scanner scanner) {
        // 1. 첫 entry 및 업 플래그
        boolean first = (piece.position==null); //처음 입장하는 경우
        BoardNode src = first? getStart(): piece.position; //현재 보드노드(추적)
        if (first) piece.hasLeftStart=true;
        System.out.println("[DEBUG] " + piece.owner.getName() + " 호출 movePiece(steps=" + steps + ")");
        boolean delayed = piece.justStoppedAtIntersection;


        debugPrintAllNodes();

        // 기본: outer 따라서
        // curr 노드가 intersection이면 shortcut으로(마지막 intersection 제외)
        // 0) 빽도 처리: steps == -1
        if (steps == -1) {
            // 0-a) 아직 보드에 안 올랐으면 불가
            if (piece.position == null) {
                System.out.println("아직 보드에 진입하지 않아 빽도 불가.");
                return false;
            }
            System.out.println("[DEBUG] 이전 위치 : " + piece.lastPosition);

            if (piece.position == getStart()
                    && piece.hasLeftStart
                    && piece.moveHistory.size() == 1) {
                System.out.println("start 지점에서 빽도! → 이전 위치로 돌아갑니다.");

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

                return false;
            }

            // 0-c) 이동 이력 없으면 불가
            if (piece.moveHistory.isEmpty()) {
                System.out.println("이전 위치 정보가 없어 빽도 불가.");
                return false;
            }

            // 0-d) 정상적인 한 칸 뒤로 이동
            if (piece.moveHistory.size() > 1) {
                piece.moveHistory.pop();
            }
            BoardNode prev = piece.moveHistory.peek();
            piece.position.pieces.remove(piece);

            boolean captured = false;
            for (Piece enemy : new ArrayList<>(prev.pieces)) {
                if (enemy.owner != piece.owner) {
                    captured = true;
                    prev.pieces.remove(enemy);
                    enemy.position = null;
                    System.out.println(enemy.owner.getName()
                            + "의 말이 빽도로 캡처되어 집으로 돌아갑니다.");
                }
            }

            // 1) 일반 이동에 들어가기 직전, lastPosition 에 현재 위치 저장
            piece.lastPosition = piece.position == null
                    ? getStart()  // 첫 진입인 경우 start 가 이전 위치
                    : piece.position;

            prev.pieces.add(piece);
            piece.position = prev;
            // 빽도 후에도 intersection 여부 유지
            piece.justStoppedAtIntersection = prev.isIntersection && prev.shortcut != null;
            System.out.println(piece.owner.getName()
                    + "의 말이 빽도로 " + prev + "로 한 칸 뒤로 이동했습니다.");
            return captured;


        }

        // 1) 일반 이동에 들어가기 직전, lastPosition 에 현재 위치 저장
        piece.lastPosition = piece.position == null
                ? getStart()  // 첫 진입인 경우 start 가 이전 위치
                : piece.position;

        // 2) “start에서 positive step” 완주 처리
        //    — 이미 보드에 올라갔었고(start를 한번 떠난 적이 있고),
        //      지금 위치가 start이며, steps>0 이면 즉시 완주
        if (!first && src == getStart() && piece.hasLeftStart && steps > 0) {
            piece.finished = true;
            System.out.println(piece.owner.getName() + "의 말이 완주했습니다! (start에서 positive step)");
            return false;
        }

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

        // 3) 업(스택) 기능
        List<Piece> stack=new ArrayList<>();
        if (first) stack.add(piece);
        else {
            for(Piece p:new ArrayList<>(src.pieces)) if(p.owner==piece.owner) stack.add(p);
            src.pieces.removeAll(stack);
        }

        // 4) 완주 처리: start를 한 칸 이상 지나쳐야 완주로 간주
        if (!first && path.stream().limit(path.size() - 1).anyMatch(n -> n == start)) {
            for (Piece p : stack) {
                p.finished = true;
                System.out.println(p.owner.getName() + "의 말이 완주했습니다!");
            }
            return false;
        }

        // 5. 캡처
        boolean capO=false;
        for(Piece e:new ArrayList<>(dest.pieces)) if(e.owner!=piece.owner){capO=true; dest.pieces.remove(e); e.position=null;
            System.out.println(e.owner.getName()+"의 말이 캡처되어 집으로 돌아갑니다.");}

        // 6. 이동 및 이력
        for(Piece p:stack){
            p.moveHistory.push(src);
            for(BoardNode n:path)
                p.moveHistory.push(n);
            dest.pieces.add(p);
            p.position=dest;
        }
        System.out.println(piece.owner.getName()+"의 말("+stack.size()+"개)이 "+dest+"로 이동했습니다.");
        return capO;
    }

    public void debugPrintAllNodes() {
        System.out.print("[DEBUG BOARD] ");
        for (BoardNode n : nodes)
            System.out.print(n.id + "[" + n.pieces.size() + "] ");
        System.out.println();
    }

    public abstract void printBoard();

    // Board.java 내부
    public List<BoardNode> getAllNodes() {
        return nodes;
    }

}
