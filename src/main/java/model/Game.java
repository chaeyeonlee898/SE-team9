package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Game {
    private final List<Player> players;
    private final Board board;
    private int currentTurn = 0; // 플레이어 인덱스
    Random random = new Random();

    public Game(int numPlayers, int piecesPerPlayer, Board board) {
        this.board = board;
        players = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            players.add(new Player("Player" + (i+1), piecesPerPlayer, board));
        }
    }

    public void start(Scanner scanner) {
        System.out.println("게임 시작!");
        while (true) {
            Player player = players.get(currentTurn);
            System.out.println("\n--- " + player.getName() + "의 턴 ---");
            board.printBoard();

            System.out.println("윷 던지기 방식 선택 (1: 지정, 2: 랜덤): ");
            int mode = scanner.nextInt();
            List<YutResult> results = new ArrayList<>();
            results = ThrowYutResult(scanner, mode, results); // 윷 결과 리스트

            // 결과 적용
            while (!results.isEmpty()) {
                YutResult selectedRes = selectYutResult(results, scanner);

                // 이동할 말 선택
                Piece chosen = selectPiece(player, scanner);

                // 말 이동
                boolean captureOccurred = board.movePiece(chosen, selectedRes.getStepCount(), scanner);
                System.out.println(chosen + " 이동 완료.");

                // 승리 조건 즉시 확인
                if (player.allPiecesFinished()) {
                    System.out.println(player.getName() + " 승리!");
                    return;
                }

                // 캡처 시 추가 던지기 (윷·모 연속)
                if (captureOccurred) {
                    System.out.println("말을 잡았습니다! 추가 던지기 기회 발생!");
                    results = ThrowYutResult(scanner, mode, results);
                }
            }

            // 결과 소진 후 승리 확인
            if (player.allPiecesFinished()) {
                System.out.println(player.getName() + " 승리!");
                break;
            }
            currentTurn = (currentTurn + 1) % players.size();
        }
        System.out.println("게임 종료");
    }

    private List<YutResult> ThrowYutResult(Scanner scanner, int mode, List<YutResult> results) {
        boolean extra = false; // 윷, 모일 때 추가 던지기 여부

        do{
            YutResult res;
            if (mode == 1) {
                res = UserYutThrow(scanner);
            } else if (mode == 2) {
                res = YutResult.throwYut(random);
                System.out.println("윷 결과: " + res);
            } else {
                System.out.println("1 또는 2를 선택하세요.");
                continue; // do-while 전체 반복
            }
            results.add(res);
            extra = res.grantsExtraThrow();
            if(extra){ System.out.println("추가 윷 던지기 기회 발생!");}
        }while(extra);
        return results;
    }

    private YutResult UserYutThrow(Scanner scanner) {
        while(true){
            System.out.println("번호 선택: 1. 빽도  2. 도  3. 개  4. 걸  5. 윷  6. 모");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1: return YutResult.BACKDO;
                case 2: return YutResult.DO;
                case 3: return YutResult.GAE;
                case 4: return YutResult.GEOL;
                case 5: return YutResult.YUT;
                case 6: return YutResult.MO;
                default:
                    System.out.println("잘못된 선택");
            }
        }
    }

    private YutResult selectYutResult(List<YutResult> results, Scanner scanner) {
        while(true){
            System.out.println("\n적용할 윷 결과를 선택하세요");

            for (int i = 0; i < results.size(); i++) {
                System.out.println(i + ": " + results.get(i));
            }
            System.out.print("번호 입력: ");

            if (!scanner.hasNextInt()) {
                scanner.next();
                System.out.println("숫자를 입력해주세요.");
                continue;
            }

            int resIdx = scanner.nextInt();
            if (resIdx < 0 || resIdx >= results.size()) {
                System.out.println("잘못된 인덱스입니다. 다시 입력해주세요.");
                continue;
            }

            YutResult selectedRes = results.remove(resIdx);
            System.out.println("선택한 결과: " + selectedRes);
            return selectedRes;
        }
    }

    private Piece selectPiece(Player player, Scanner scanner) {
        while (true) {
            System.out.println("\n이동할 말을 선택하세요");
            for (int i = 0; i < player.pieces.size(); i++) {
                System.out.println(i + ": " + player.pieces.get(i)
                        + (player.pieces.get(i).finished ? " (완주)" : ""));
            }
            System.out.print("번호 입력: ");

            if (!scanner.hasNextInt()) {
                scanner.next();
                System.out.println("숫자를 입력해주세요.");
                continue;
            }
            int pieceIdx = scanner.nextInt();
            if (pieceIdx < 0 || pieceIdx >= player.pieces.size()) {
                System.out.println("잘못된 인덱스입니다. 다시 입력해주세요.");
                continue;
            }
            Piece chosen = player.pieces.get(pieceIdx);
            if (chosen.finished) {
                System.out.println("이미 완주된 말입니다.");
                continue;
            }
            return chosen;
        }
    }


    public Player getCurrentPlayer() {
        return players.get(currentTurn);
    }

    public List<YutResult> rollAllYuts(Random rand) {
        List<YutResult> results = new ArrayList<>();
        YutResult r;
        do {
            r = YutResult.throwYut(rand);
            results.add(r);
        } while (r.grantsExtraThrow());
        return results;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Board getBoard() {
        return board;
    }

    public void nextTurn() {
        currentTurn = (currentTurn + 1) % players.size();
    }

    public boolean applyYutResult(YutResult result, Piece piece) {
        if (piece == null || piece.isFinished()) return false;

        // 출발 전인 경우
        if (piece.getPosition() == null) {
            piece.setHasLeftStart(false);
        }

        // 교차점에 멈춘 경우
        if (piece.getPosition() != null && piece.getPosition().isIntersection() && piece.getPosition().getShortcut() != null) {
            piece.setJustStoppedAtIntersection(true);
        } else {
            piece.setJustStoppedAtIntersection(false);
        }

        // 보드에서 이동 처리 및 캡처 여부 반환
        return board.movePiece(piece, result.getStepCount(), null);
    }

    public boolean isCurrentPlayerWin() {
        return getCurrentPlayer().allPiecesFinished();
    }

    public List<Piece> getPieces() {
        return players.stream()
                .flatMap(player -> player.getPieces().stream())
                .collect(Collectors.toList());
    }
}

