package controller;

import java.util.Scanner;
import model.Board;
import model.Game;
import model.SquareBoard;
import model.PentagonBoard;
import model.HexagonBoard;

public class YutnoriGame {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean playAgain = true;
        while (playAgain) {
            // 1) 보드 타입 입력
            Board board = selectBoard(scanner);
            // 2) 플레이어 수 입력 (2~4명)
            int numPlayers = InputNumPlayers(scanner);
            // 3) 말 개수 입력 (2~5개)
            int piecesPerPlayer = InputNumPieces(scanner);

            Game game = new Game(numPlayers, piecesPerPlayer, board);
            game.start(scanner);

            System.out.println("\n게임이 종료되었습니다.");
            System.out.print("다시 게임을 하시겠습니까? (Y/N): ");
            String answer = scanner.next();
            playAgain = answer.equalsIgnoreCase("Y");
        }
        System.out.println("프로그램을 종료합니다.");
        scanner.close();
    }

    private static Board selectBoard(Scanner scanner) {
        while (true) {
            System.out.println("보드 타입 선택 (1: 사각형, 2: 오각형, 3: 육각형): ");
            if (!scanner.hasNextInt()) {
                scanner.next();
                System.out.println("숫자를 입력해주세요.");
                continue;
            }
            int boardType = scanner.nextInt();
            switch (boardType) {
                case 1: return new SquareBoard();
                case 2: return new PentagonBoard();
                case 3: return new HexagonBoard();
                default: System.out.println("1~3 사이의 숫자를 입력해주세요.");
            }
        }
    }

    // 플레이어 수 입력 루프
    private static int InputNumPlayers(Scanner scanner) {
        int numPlayers;
        while (true) {
            System.out.println("플레이어 수 입력 (2~4명): ");
            if (!scanner.hasNextInt()) {
                scanner.next();
                System.out.println("숫자를 입력해주세요.");
                continue;
            }
            numPlayers = scanner.nextInt();
            if (numPlayers < 2 || numPlayers > 4) {
                System.out.println("플레이어 수는 2~4명으로 제한됩니다.");
            } else {
                break;
            }
        }
        return numPlayers;
    }

    // 말 개수 입력 루프
    private static int InputNumPieces(Scanner scanner) {
        int numPieces;
        while (true) {
            System.out.println("말 개수 입력 (2~5개): ");
            if (!scanner.hasNextInt()) {
                scanner.next();
                System.out.println("숫자를 입력해주세요.");
                continue;
            }
            numPieces = scanner.nextInt();
            if (numPieces < 2 || numPieces > 5) {
                System.out.println("말 개수는 2~5개로 제한됩니다.");
            } else {
                break;
            }
        }
        return numPieces;
    }
}