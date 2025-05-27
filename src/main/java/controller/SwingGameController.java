package controller;

import model.Game;
import model.Piece;
import model.YutResult;
import view.javafx.FXDialog;
import view.swing.DialogUtils;
import view.swing.GameFrame;
import view.swing.GamePanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * GameController는 뷰로부터 입력을 받아 모델(Game)을 호출하고,
 * 화면(GamePanel, 라벨 등)을 갱신하는 역할만 담당합니다.
 */
public class SwingGameController {
    private final Game game;
    private final GamePanel gamePanel;
    private final JLabel turnLabel;
    private final JTextArea logArea;
    private final GameFrame gameFrame;
    private final JLabel statusLabel;
    private final Random rand = new Random();
    private boolean randomMode;

    public SwingGameController(
            Game game,
            GamePanel gamePanel,
            JLabel turnLabel,
            JTextArea logArea,
            GameFrame gameFrame,
            JLabel statusLabel
    ) {
        this.game = game;
        this.gamePanel = gamePanel;
        this.turnLabel = turnLabel;
        this.logArea = logArea;
        this.gameFrame = gameFrame;
        this.statusLabel = statusLabel;

        // 초기 화면 갱신
        updateTurnLabel();
        updateStatusLabel();
        gamePanel.refresh();
    }

    /**
     * 윷 던지기 버튼 클릭 시 호출되는 메서드
     */
    public void onRoll() {
        /** 던지기 방식 선택 */
        randomMode = DialogUtils.askRandomMode();

        List<YutResult> pending = new ArrayList<>();
        handleBonusThrows(pending);

        /** 결과 적용 단계 */
        while (!pending.isEmpty()) {
            // 사용자가 수집된 결과 중 하나를 선택
            YutResult sel = DialogUtils.selectYutResult(pending);
            if (sel == null) break;  // 취소 시 적용 단계 종료
            pending.remove(sel);

            // 이동할 말을 선택
            Piece p = DialogUtils.askPieceSelection(
                    game.getCurrentPlayer().getUnfinishedPieces()
            );
            if (p == null) return;  // 취소 시 턴 종료

            // model 계층에 적용 : 말 이동, 캡처 여부 반환
            boolean captured = game.applyYutResult(sel, p);
            log(game.getCurrentPlayer().getName() + " → " + sel);

            // view 계층 갱신
            gamePanel.refresh();
            updateTurnLabel();
            updateStatusLabel();

            // 승리 판정
            if (game.isCurrentPlayerWin()) {
                if (DialogUtils.confirmRestart(game.getCurrentPlayer().getName())) {
                    gameFrame.showStartPanel();
                }
                return;
            }

            // 캡처 : 잡았을 때 추가 던지기
            if (captured) {
                handleBonusThrows(pending);
            }
        }
        /** 턴 종료 */
        game.nextTurn();
        updateTurnLabel();
        updateStatusLabel();
    }


    /**
     * 현재 플레이어 정보로 턴 라벨 갱신
     */
    private void updateTurnLabel() {
        String name = game.getCurrentPlayer().getName();
        turnLabel.setText("현재 플레이어: " + name);
    }

    /**
     * 현재 플레이어 정보로 상태 라벨 갱신
     */
    private void updateStatusLabel() {
        long finished = game.getCurrentPlayer().getFinishedPieceCount();
        long remaining = game.getCurrentPlayer().getRemainingPieceCount();
        statusLabel.setText("완주: " + finished + " / 남은 말: " + remaining);
    }

    /**
     * 추가 윷 던지기 실행 함수
     */
    private void handleBonusThrows(List<YutResult> pending) {
        YutResult res;
        do {
            if (randomMode) {
                res = YutResult.throwYut(rand);
            } else {
                res = DialogUtils.askManualThrow();
                if (res == null) return;  // 사용자가 취소
            }
            DialogUtils.showThrowResult(res);
            log("던진 결과: " + res);
            pending.add(res);
        } while (res.grantsExtraThrow());
    }

    /**
     * 로그 출력 및 스크롤 자동 이동
     */
    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}