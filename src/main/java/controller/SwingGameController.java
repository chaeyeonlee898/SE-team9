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

    private List<YutResult> pendingResults;

    // 지금 클릭으로 골라야 할 윷 결과
    private YutResult currentResult;

    // 지금 말 선택 대기 모드인지 표시
    private boolean awaitingPieceSelection = false;

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

        // 클릭으로 말을 선택했을 때 onPieceClicked을 호출하도록 연결
        gamePanel.setPieceClickListener(this::onPieceClicked);

        // 초기 화면 갱신
        updateTurnLabel();
        updateStatusLabel();
        gamePanel.refresh();
    }

    /**
     * 윷 던지기 버튼 클릭 시 호출되는 메서드
     */
    public void onRoll() {
//        /** 던지기 방식 선택 */
        // 1) 윷 던지고 pendingResults 채우기
        pendingResults = new ArrayList<>();
        handleBonusThrows(pendingResults);

        // 2) 첫 번째 윷 결과 선택 단계로 넘어가기
        selectNextYutResult();
    }


    /**
     * 현재 플레이어 정보로 턴 라벨 갱신
     */
    private void updateTurnLabel() {
        String name = game.getCurrentPlayer().getName();
        turnLabel.setText("현재 플레이어: " + name);

        gamePanel.highlightCurrentPlayer(game.getCurrentPlayer());

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
            // 매번 모드 선택
            boolean isRandom = DialogUtils.askRandomMode();

            if (isRandom) {
                res = YutResult.throwYut(rand);
            } else {
                res = DialogUtils.askManualThrow();
                if (res == null) return;  // 수동 입력 취소 시 종료
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
    /**
     * 남은 pendingResults 중 하나를 선택하도록 유도.
     * 대화상자 대신 마우스 클릭으로 말 선택 대기 상태로 전환.
     */
    private void selectNextYutResult() {
        if (pendingResults.isEmpty()) {
            // 더 이상 처리할 윷 결과가 없으면 턴 종료
            endTurn();
            return;
        }
        // 기존 대화상자 선택은 그대로 유지해도 되고,
        // DialogUtils.selectYutResult 대신 첫 결과를 꺼내도 됩니다:
        currentResult = DialogUtils.selectYutResult(pendingResults);
        if (currentResult == null) {
            endTurn();
            return;
        }
        // 이제 “말 클릭”을 대기 상태로 전환
        awaitingPieceSelection = true;
        // (원하면 statusLabel 에 "말을 클릭하세요" 같은 메시지 표시)
        statusLabel.setText("말을 클릭하세요: " + currentResult);
    }

    /**
     * GamePanel 쪽에서 말이 클릭되면 여기로 콜백됩니다.
     */
    public void onPieceClicked(Piece piece) {
        if (!awaitingPieceSelection) return;                 // 대기 중 아니면 무시
        if (!game.getCurrentPlayer().getUnfinishedPieces().contains(piece)) {
            // 내 차례가 아닌 말이거나 이미 완주한 말이면 무시
            return;
        }

        // 선택 처리
        awaitingPieceSelection = false;
        pendingResults.remove(currentResult);

        boolean captured = game.applyYutResult(currentResult, piece);
        log(game.getCurrentPlayer().getName() + " → " + currentResult);

        // 뷰 갱신
        gamePanel.refresh();
        updateTurnLabel();
        updateStatusLabel();

        // 승리 체크
        if (game.isCurrentPlayerWin()) {
            if (DialogUtils.confirmRestart(game.getCurrentPlayer().getName())) {
                gameFrame.showStartPanel();
            }
            return;
        }

        // 캡처 시 추가 던지기
        if (captured) {
            handleBonusThrows(pendingResults);
        }
        // 다음 윷 결과 또는 턴 종료
        selectNextYutResult();
    }

    /** 턴 종료 후 상태 업데이트 */
    private void endTurn() {
        pendingResults = null;
        currentResult = null;
        game.nextTurn();
        updateTurnLabel();
        updateStatusLabel();
        gamePanel.refresh();
    }

}