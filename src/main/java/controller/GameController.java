package controller;

import model.*;
import view.GameFrame;
import view.GamePanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameController {
    private final Game game;
    private final GamePanel gamePanel;
    private final JLabel turnLabel;
    private final JTextArea logArea;
    private final GameFrame gameFrame;
    private final JLabel statusLabel;
    private final List<YutResult> pendingResults = new ArrayList<>();
    private final Random random = new Random();
    private boolean isRandomMode = true;

    public GameController(Game game, GamePanel gamePanel, JLabel turnLabel, JTextArea logArea, GameFrame gameFrame, JLabel statusLabel) {
        this.game = game;
        this.gamePanel = gamePanel;
        this.turnLabel = turnLabel;
        this.logArea = logArea;
        this.gameFrame = gameFrame;
        this.statusLabel = statusLabel;
        updateStatusLabel();
        updateTurnLabel();
    }

    public void chooseThrowMode() {
        String[] options = {"지정 윷 던지기", "랜덤 윷 던지기"};
        int choice = JOptionPane.showOptionDialog(null, "윷 던지기 방식을 선택하세요:", "던지기 방식 선택",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
        if (choice == 0) {
            isRandomMode = false;
            do {
                YutResult result = promptManualYutResult();
                if (result == null) break;
                pendingResults.add(result);
            } while (pendingResults.get(pendingResults.size() - 1).grantsExtraThrow());
        } else if (choice == 1) {
            isRandomMode = true;
            do {
                YutResult result = YutResult.throwYut(random);
                JOptionPane.showMessageDialog(null, "윷 결과: " + result);
                pendingResults.add(result);
            } while (pendingResults.get(pendingResults.size() - 1).grantsExtraThrow());
        }
        processResults();
    }

    private void processResults() {
        Player current = game.getCurrentPlayer();
        while (!pendingResults.isEmpty()) {
            YutResult selectedResult = (YutResult) JOptionPane.showInputDialog(
                    null,
                    "적용할 윷 결과를 선택하세요",
                    "윷 결과 적용",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    pendingResults.toArray(),
                    pendingResults.get(0));

            if (selectedResult == null) return;
            pendingResults.remove(selectedResult);

            List<Piece> movablePieces = current.getPieces().stream().filter(p -> !p.isFinished()).toList();
            if (movablePieces.isEmpty()) return;

            Piece chosen = (Piece) JOptionPane.showInputDialog(
                    null,
                    "이동할 말을 선택하세요",
                    "말 선택",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    movablePieces.toArray(),
                    movablePieces.get(0));

            if (chosen == null) return;

            boolean extra = game.applyYutResult(selectedResult, chosen);
            gamePanel.refresh();
            updateStatusLabel();
            log(current.getName() + " → " + selectedResult + ", 말: " + chosen);

            if (game.isCurrentPlayerWin()) {
                log(current.getName() + " 승리! 게임 종료");
                int option = JOptionPane.showConfirmDialog(null,
                        current.getName() + " 승리!\n게임을 다시 시작할까요?",
                        "게임 종료",
                        JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) gameFrame.showStartPanel();
                else System.exit(0);
                return;
            }

            if (extra && !selectedResult.grantsExtraThrow()) {
                // 캡처로 인한 추가 던지기
                do {
                    YutResult extraRes = isRandomMode ? YutResult.throwYut(random) : promptManualYutResult();
                    if (extraRes == null) break;
                    pendingResults.add(extraRes);
                    JOptionPane.showMessageDialog(null, "추가 윷 결과: " + extraRes);
                } while (pendingResults.get(pendingResults.size() - 1).grantsExtraThrow());
            }
        }
        game.nextTurn();
        updateTurnLabel();
        updateStatusLabel();
        log("\n");
    }

    private YutResult promptManualYutResult() {
        String[] options = {"빽도", "도", "개", "걸", "윷", "모"};
        String selected = (String) JOptionPane.showInputDialog(
                null,
                "지정할 윷 결과를 선택하세요:",
                "지정 윷 던지기",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (selected == null) return null;

        return switch (selected) {
            case "빽도" -> YutResult.BACKDO;
            case "도" -> YutResult.DO;
            case "개" -> YutResult.GAE;
            case "걸" -> YutResult.GEOL;
            case "윷" -> YutResult.YUT;
            case "모" -> YutResult.MO;
            default -> null;
        };
    }

    private void updateTurnLabel() {
        Player current = game.getCurrentPlayer();
        turnLabel .setText("현재 플레이어: " + current.getName());
    }

    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void updateStatusLabel() {
        Player p     = game.getCurrentPlayer();
        long fin     = p.getFinishedPieceCount();
        long remain  = p.getRemainingPieceCount();
        statusLabel.setText("완주: " + fin + " / 남은 말: " + remain);
    }
}
