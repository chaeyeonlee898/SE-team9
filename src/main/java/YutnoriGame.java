//YutnoriGame.java
import java.util.Scanner;

public class YutnoriGame {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean playAgain = true;
        while (playAgain) {

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
            //참여자 수 및 말 개수 제한
            int numPlayers = InputNumPlayers();
            int piecesPerPlayer = InputNumPieces();

            Game game = new Game(numPlayers, piecesPerPlayer, board);
            game.start(scanner);

            System.out.println("\n게임이 종료되었습니다.");
            System.out.print("다시 게임을 하시겠습니까? (Y/N): ");
            String answer = scanner.next();

            if (!answer.equalsIgnoreCase("Y")) {
                playAgain = false;
            }
        }
        System.out.println("프로그램을 종료합니다.");
        scanner.close();
    }
    public static int InputNumPlayers() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("플레이어 수 입력: ");
        int numPlayers = scanner.nextInt();
        while (numPlayers < 2 || numPlayers > 4) {
            System.out.println("플레이어 수는 2-4명으로 제한됩니다\n다시 플레이어 수를 입력하세요\n플레이어 수 입력: ");
            numPlayers = scanner.nextInt();
            if (numPlayers >= 2 && numPlayers <= 4) {
                break;}
            }
        return numPlayers;
    }
    public static int InputNumPieces() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("말 개수 입력: ");
        int numPieces = scanner.nextInt();
        while (numPieces < 2 || numPieces > 5) {
            System.out.println("말 개수는 2-5개으로 제한됩니다\n다시 말 개수를 입력하세요\n말 개수 입력: ");
            numPieces = scanner.nextInt();
            if (numPieces >= 2 && numPieces <= 5) {
                break;}
        }
        return numPieces;
    }
}
