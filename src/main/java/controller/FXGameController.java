package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import model.*;
import view.javafx.BoardPane;
import view.javafx.FXDialog;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class FXGameController implements Initializable {

    @FXML private Label turnLabel;
    @FXML private Label statusLabel;
    @FXML private Button throwButton;
    @FXML private TextArea logArea;
    @FXML private BoardPane boardPane;

    enum State {ROLLING, SELECTING_PIECE, APPLYING_MOVE, IDLE}
    private State state = State.IDLE;

    private Game game;
    private final List<YutResult> yutResults = new ArrayList<>();
    private YutResult currentYut;
    private List<Piece> candidates;
    private final Random random = new Random();

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        boardPane.setOnPieceClick(this::onPieceClicked);
        throwButton.setOnAction(e -> onRoll());
    }

    public void initGame() {
        yutResults.clear();
        logArea.clear();

        boardPane.drawBoard(game.getBoard(), game.getPieces(), game.getPlayers());
        updateTurnLabel();
        updateStatusLabel();
        Platform.runLater(() -> {
            boardPane.redraw();
            boardPane.highlightCurrentPlayer(game.getCurrentPlayer());
        });

        throwButton.setDisable(false);
        throwButton.setOnAction(e -> onRoll());
    }

    @FXML
    public void onRoll() {
        // ▶ 한 번 던진 뒤에는 버튼 비활성화 (다시 클릭 방지)
        throwButton.setDisable(true);
        yutResults.clear();

        boolean randomMode = FXDialog.askRandomMode();
        YutResult r;
        do {
            r = randomMode ? YutResult.throwYut(random) : FXDialog.askManualThrow();
            if (r == null) return;
            FXDialog.showThrowResult(r);
            log("던진 결과: " + r);
            yutResults.add(r);
        } while (r.grantsExtraThrow());

        if (yutResults.size() == 1) {
            currentYut = yutResults.remove(0);
            promptPieceSelection();
        } else {
            currentYut = FXDialog.selectYutResult(yutResults);
            if (currentYut == null) return;
            yutResults.remove(currentYut);
            promptPieceSelection();
        }
    }

    @FXML
    public void onPieceClicked(Piece p) {
        if (state != State.SELECTING_PIECE) return;
        if (!candidates.contains(p)) return;

        state = State.APPLYING_MOVE;
        BoardNode oldPos = p.getPosition();
        boolean captured = game.applyYutResult(currentYut, p);
        log(game.getCurrentPlayer().getName() + " 이동: " + currentYut);
        BoardNode newPos = p.getPosition();

        if (oldPos == null && newPos != null) {
            boardPane.animateFromWaitingTo(newPos.getId(), p, captured,
                () -> handleAfterMove(captured)
            );
        } else {
            boardPane.redraw();
            handleAfterMove(captured);
        }
    }

    private void handleAfterMove(boolean captured) {
        // bonus and captures... existing logic
        if (captured) {
            // 캡처 즉시 던지기 버튼 활성화
            throwButton.setDisable(false);
            var bonus = new ArrayList<YutResult>();
            boolean randomMode = FXDialog.askRandomMode();
            YutResult br;
            do {
                br = randomMode ? YutResult.throwYut(random) : FXDialog.askManualThrow();
                if (br == null) break;
                FXDialog.showThrowResult(br);
                log("보너스 던진 결과: " + br);
                bonus.add(br);
            } while (br.grantsExtraThrow());
            yutResults.addAll(bonus);
        }

        if (!yutResults.isEmpty()) {
            currentYut = yutResults.size() == 1 ? yutResults.remove(0) : FXDialog.selectYutResult(yutResults);
            if (currentYut != null) {
                yutResults.remove(currentYut);
                promptPieceSelection();
                return;
            }
        }

        if (game.isCurrentPlayerWin()) {
            boolean again = FXDialog.confirmRestart(game.getCurrentPlayer().getName());
            if (again) initGame(); else throwButton.setDisable(true);
            return;
        }

        game.nextTurn();
        updateTurnLabel();
        updateStatusLabel();
        boardPane.highlightCurrentPlayer(game.getCurrentPlayer());

        // ▶ 턴 종료 후에만 다시 버튼 활성화
        throwButton.setDisable(false);
        state = State.IDLE;
    }

    private void promptPieceSelection() {
        candidates = game.getCurrentPlayer().getUnfinishedPieces();
        state = State.SELECTING_PIECE;
    }

    private void updateTurnLabel() {
        turnLabel.setText("현재 플레이어: " + game.getCurrentPlayer().getName() + " 차례");
    }

    private void updateStatusLabel() {
        long f = game.getCurrentPlayer().getFinishedPieceCount();
        long r = game.getCurrentPlayer().getRemainingPieceCount();
        statusLabel.setText("완주: " + f + " / 남은 말: " + r);
    }

    private void log(String msg) {
        logArea.appendText(msg + "\n");
        logArea.positionCaret(logArea.getLength());
    }
}
