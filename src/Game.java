import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

class Game {
    List<Player> players;
    Board board;
    int currentTurn = 0;
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

            if (mode == 1) {
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
                results.add(res);
            } else {
                boolean extra;
                do {
                    YutResult res = YutResult.throwYut(random);
                    results.add(res);
                    System.out.println("윷 결과: " + res);
                    extra = res.grantsExtraThrow();
                } while (extra);
            }

            for (YutResult res : results) {
                System.out.println("\n적용할 윷 결과: " + res);
                System.out.println("이동할 말 선택 (인덱스): ");
                for (int i = 0; i < player.pieces.size(); i++) {
                    System.out.println(i + ": " + player.pieces.get(i));
                }
                int idx = scanner.nextInt();
                if (idx < 0 || idx >= player.pieces.size()) {
                    System.out.println("잘못된 인덱스, 결과 스킵.");
                    continue;
                }

                Piece selected = player.pieces.get(idx);
                if (selected.finished) {
                    System.out.println("이미 완주한 말입니다.");
                    continue;
                }

                boolean captured = board.movePiece(selected, res.getStepCount(), scanner);
                System.out.println(selected + " 이동 완료.");

                if (captured || res.grantsExtraThrow()) {
                    System.out.println("추가 윷 던지기 기회 발생!");
                    YutResult extraRes = YutResult.throwYut(random);
                    System.out.println("추가 결과: " + extraRes);
                    System.out.println("이동할 말 선택 (인덱스): ");
                    for (int i = 0; i < player.pieces.size(); i++) {
                        System.out.println(i + ": " + player.pieces.get(i));
                    }
                    idx = scanner.nextInt();
                    if (idx < 0 || idx >= player.pieces.size()) {
                        System.out.println("잘못된 인덱스");
                        continue;
                    }
                    Piece extraPiece = player.pieces.get(idx);
                    if (!extraPiece.finished) {
                        board.movePiece(extraPiece, extraRes.getStepCount(), scanner);
                        System.out.println(extraPiece + " 이동 완료.");
                    } else {
                        System.out.println("이미 완주한 말입니다.");
                    }
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
