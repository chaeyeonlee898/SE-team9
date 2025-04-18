//Game.java
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

class Game {
    List<Player> players;
    Board board;
    int currentTurn = 0; //플레이어 인덱스
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
            List<YutResult> results = new ArrayList<>(); //윷 결과 리스트

            boolean extra = false; //윷,모일 때 추가 던지기
            if (mode == 1) { //지정일 때
                do{
                    if(extra){
                        System.out.println("추가 윷 던지기 기회 발생!");
                    }
                    System.out.println("번호 선택: 1. 빽도  2. 도  3. 개  4. 걸  5. 윷  6. 모");
                    int choice = scanner.nextInt();
                    YutResult res = null;
                    switch (choice) {
                        case 1: res = YutResult.BACKDO; break;
                        case 2: res = YutResult.DO; break;
                        case 3: res = YutResult.GAE; break;
                        case 4: res = YutResult.GEOL; break;
                        case 5: res = YutResult.YUT; break;
                        case 6: res = YutResult.MO; break;
                        default:
                            System.out.println("잘못된 선택");
                            continue;
                    }
                    extra = res.grantsExtraThrow();
                    results.add(res);
                } while(extra);
            } else {
                do {
                    YutResult res = YutResult.throwYut(random);
                    results.add(res);
                    System.out.println("윷 결과: " + res);
                    extra = res.grantsExtraThrow();
                } while (extra);
            }

            // 결과 적용
            while (!results.isEmpty()) {
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
                    continue;        // 리스트 유지, 기회 차감 없음
                }

                YutResult selectedRes = results.remove(resIdx); //선택된 결과 리스트에서 제거
                System.out.println("선택한 결과: " + selectedRes);

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
                        System.out.println("이미 완주한 말입니다. 다른 말을 선택해주세요.");
                        continue;
                    }

                    // 말 이동 시키기
                    board.movePiece(chosen, selectedRes.getStepCount(), scanner);
                    System.out.println(chosen + " 이동 완료.");
                    break;
                }
            }

            if (player.allPiecesFinished()) {
                System.out.println(player.getName() + " 승리!");
                break;
            }
            currentTurn = (currentTurn + 1) % players.size();
        }
        System.out.println("게임 종료");
    }
}
