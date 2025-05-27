package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import model.*;
import view.javafx.BoardPane;
import view.javafx.FXDialog;
import view.swing.DialogUtils;

import java.io.IOException;
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

    enum State {ROLLING, SELECTING_PIECE, APPLYING_MOVE, IDLE}
    private State state = State.IDLE;

    private Game game;
    // 던진 결과 대기 리스트
    private final List<YutResult> yutResults = new ArrayList<>();
    // 현재 적용할 YutResult
    private YutResult currentYut;
    // 선택 가능한 말 목록
    private List<Piece> candidates;    private final Random random = new Random();
    // 지정/랜덤 윷 던지기
    private boolean randomMode;

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        boardPane.setOnPieceClick(this::onPieceClicked);
        throwButton.setOnAction(e -> onRoll());
    }

    public void initGame() {
        if (game == null) {
            throw new IllegalStateException("game이 주입되지 않았습니다!");
        }

        yutResults.clear();
        logArea.clear();

        boardPane.drawBoard(game.getBoard(), game.getPieces(), game.getPlayers());
        updateTurnLabel();
        updateStatusLabel();
        throwButton.setDisable(false);
        throwButton.setOnAction(e -> onRoll());
    }

    public void onRoll() {
        // 던지기 대기 리스트 초기화
        yutResults.clear();
        // 모드 선택
        randomMode = FXDialog.askRandomMode();

        // 1) 윷 던지기: 연속 던지기 허용
        handleBonusThrows(yutResults);

        // 2) 적용할 한 판 선택
        if (yutResults.size() == 1) {
            currentYut = yutResults.remove(0);
            promptPieceSelection();
        } else {
            // 여러 개일 때는 다이얼로그로 선택
            currentYut = FXDialog.selectYutResult(yutResults);
            if (currentYut == null) return;
            yutResults.remove(currentYut);
            promptPieceSelection();
        }
    }

    /** BoardPane에서 클릭할 때 호출되는 콜백 */
    public void onPieceClicked(Piece p) {
        if (state != State.SELECTING_PIECE) return;
        // 클릭된 말이 실제 후보인가 확인
        if (!candidates.contains(p)) return;

        state = State.APPLYING_MOVE;
        // 1) 이동 적용
        boolean captured = game.applyYutResult(currentYut, p);
        log(game.getCurrentPlayer().getName() + " 이동: " + currentYut);

        // 2) 화면 갱신
        boardPane.drawBoard(game.getBoard(), game.getPieces(), game.getPlayers());
        updateTurnLabel();
        updateStatusLabel();

        // 3) 승리 판정
        // 승리 판정
        // FXGameController.java 의 onRoll() 안 승리 판정 부분을 이렇게 바꿔 보세요.

        if (game.isCurrentPlayerWin()) {
            boolean again = FXDialog.confirmRestart(game.getCurrentPlayer().getName());
            if (again) {
                try {
                    // ① 현재 윈도우(Stage) 가져오기
                    Stage stage = (Stage) throwButton.getScene().getWindow();

                    // ② Start 화면 FXML 로드
                    FXMLLoader loader =
                            new FXMLLoader(getClass().getResource("/fxml/StartPane.fxml"));
                    Parent startRoot = loader.load();

                    // ③ 컨트롤러에 Stage 주입
                    FXStartController startCtrl = loader.getController();
                    startCtrl.setPrimaryStage(stage);
                    stage.setScene(new Scene(startRoot, 900, 800));


                    // ④ 새 Scene 으로 교체
                    stage.setScene(new Scene(startRoot));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                throwButton.setDisable(true);
            }
            return;
        }

        // 4) 캡처 보너스
        if (captured) {
            handleBonusThrows(yutResults);
        }

        // 5) 남아 있는 yutResults가 있으면 다시 말 선택
        if (!yutResults.isEmpty()) {
            currentYut = (yutResults.size() == 1)
                    ? yutResults.remove(0)
                    : FXDialog.selectYutResult(yutResults);
            if (currentYut != null) {
                yutResults.remove(currentYut);
                promptPieceSelection();
                return;
            }
        }

        // 6) 턴 종료
        game.nextTurn();
        updateTurnLabel();
        updateStatusLabel();
        state = State.IDLE;
    }


    /** 말을 클릭으로 선택하도록 UI 준비 */
    private void promptPieceSelection() {
        // 지금부터 클릭으로 조작
        candidates = game.getCurrentPlayer().getUnfinishedPieces();
        state = State.SELECTING_PIECE;
        // (옵션) boardPane에 후보 말 표시 강조
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

    private void handleBonusThrows(List<YutResult> yutResults) {
        YutResult res;
        do {
            if (randomMode) {
                res = YutResult.throwYut(random);
            } else {
                res = FXDialog.askManualThrow();
                if (res == null) return;  // 사용자가 취소
            }
            FXDialog.showThrowResult(res);
            log("던진 결과: " + res);
            yutResults.add(res);
        } while (res.grantsExtraThrow());
    }

    private void log(String message) {
        logArea.appendText(message + "\n");
        logArea.positionCaret(logArea.getLength());
    }
}