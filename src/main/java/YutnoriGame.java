//YutnoriGame.java
import java.util.Scanner;

public class YutnoriGame {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("보드 타입 선택 (1: 사각형, 2: 오각형, 3: 육각형): ");
        int boardType = scanner.nextInt();
        Board board;
        switch(boardType) {
            case 2:
                board = new PentagonBoard();
                break;
            case 3:
                board = new HexagonBoard();
                break;
            default:
                board = new SquareBoard();  // 전통 윷놀이 사각형 판 (총 29개 노드)
                break;
        }

        System.out.println("플레이어 수 입력: ");
        int numPlayers = scanner.nextInt();
        System.out.println("각 플레이어의 말 개수 입력: ");
        int piecesPerPlayer = scanner.nextInt();

        Game game = new Game(numPlayers, piecesPerPlayer, board);
        game.start(scanner);

        scanner.close();
    }
}
