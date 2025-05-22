package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import model.*;
import view.javafx.BoardPane;
import view.javafx.FXDialog;

import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JavaFX용 게임 컨트롤러
 * FXDialog를 통해 모든 UI 상호작용을 처리하고,
 * model.Game으로 게임 로직을 실행합니다.
 */
public class FXGameController implements Initializable {

    @FXML private Label turnLabel;
    @FXML private Label statusLabel;
    @FXML private Button throwButton;
    @FXML private TextArea logArea;
    @FXML private BoardPane boardPane;

    private Game game;
    private final List<YutResult> yutResults = new ArrayList<>();
    private final Random random = new Random();

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
                            // 이제 이 메서드를 직접 정의합니다
        throwButton.setOnAction(e -> onRoll());
    }

    public void initGame() {
        if (game == null) {
            throw new IllegalStateException("game이 주입되지 않았습니다!");
        }
        yutResults.clear();
        logArea.clear();

        boardPane.drawBoard(game.getBoard(), game.getPieces());
        updateTurnLabel();
        updateStatusLabel();
        throwButton.setDisable(false);
        throwButton.setOnAction(e -> onRoll());
    }

    public void onRoll() {
        // 던지기 대기 리스트 초기화
        yutResults.clear();
        // 모드 선택
        boolean randomMode = FXDialog.askRandomMode();

        // 1) 윷 던지기: 연속 던지기 허용
        YutResult r;
        do {
            if (randomMode) {
                r = YutResult.throwYut(random);
            } else {
                r = FXDialog.askManualThrow();
                if (r == null) return;
            }
            FXDialog.showThrowResult(r);
            log("던진 결과: " + r);
            yutResults.add(r);
        } while (r.grantsExtraThrow());

        // 2) 결과 적용 루프
        while (!yutResults.isEmpty()) {
            YutResult selected = (yutResults.size() == 1)
                    ? yutResults.get(0)
                    : FXDialog.selectYutResult(yutResults);
            if (selected == null) break;
            yutResults.remove(selected);

            log("선택된 결과: " + selected);

            // 이동할 말 선택
            List<Piece> candidates = game.getCurrentPlayer().getUnfinishedPieces();
            Piece chosen = FXDialog.askPieceSelection(candidates);
            if (chosen == null) return;

            // 이동 적용 및 캡처 보너스 처리
            boolean captured = game.applyYutResult(selected, chosen);
            log(game.getCurrentPlayer().getName() + " 이동: " + selected);

            boardPane.drawBoard(game.getBoard(), game.getPieces());
            updateTurnLabel();
            updateStatusLabel();

            // 승리 판정
            if (game.isCurrentPlayerWin()) {
                if (FXDialog.confirmRestart(game.getCurrentPlayer().getName())) {
                    // 새 Game 인스턴스를 만들어 주입
                    Game newGame = new Game(2, 2,
                            game.getBoard());   // 보드 복사 또는 새로 생성
                    setGame(newGame);
                    initGame();                 // ← 완전히 새 게임으로 리셋
                } else {
                    throwButton.setDisable(true);
                }
                return;
            }

            // 캡처 보너스 던지기
            if (captured) {
                List<YutResult> bonusList = game.rollAllYuts(random);
                for (YutResult b : bonusList) {
                    FXDialog.showThrowResult(b);
                    log("보너스 던진 결과: " + b);
                    yutResults.add(b);
                }
            }
        }

        // 3) 턴 종료
        game.nextTurn();
        updateTurnLabel();
        updateStatusLabel();
    }

    private void updateTurnLabel() {
        turnLabel.setText("현재 플레이어: " + game.getCurrentPlayer().getName() + " 차례");
    }

    private void updateStatusLabel() {
        long finished  = game.getCurrentPlayer().getFinishedPieceCount();
        long remaining = game.getCurrentPlayer().getRemainingPieceCount();
        statusLabel.setText(
                "완주: " + finished + " / 남은 말: " + remaining
        );
    }

    private void log(String message) {
        logArea.appendText(message + "\n");
        logArea.positionCaret(logArea.getLength());
    }
}
