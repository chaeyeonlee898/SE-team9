package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.animation.TranslateTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox; 
import javafx.scene.layout.HBox;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.paint.Color;
import javafx.util.Duration;
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
    @FXML private VBox    yutContainer; 
    @FXML private ImageView staticYutView;
    @FXML private HBox yutBox;

    enum State {ROLLING, SELECTING_PIECE, APPLYING_MOVE, IDLE}
    private State state = State.IDLE;

    private Game game;
    private final List<YutResult> yutResults = new ArrayList<>();
    private YutResult currentYut;
    private List<Piece> candidates;
    private final Random random = new Random();
    private Image defaultYutThrowImage;

    public void setGame(Game game) {
        this.game = game;
    }
    
    private int stepsOf(YutResult r) {
        return switch (r) {
            case DO      -> 1;
            case GAE     -> 2;
            case GEOL    -> 3;
            case YUT     -> 4;
            case MO      -> 5;
            case BACKDO  -> -1;
        };
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1) 기존 초기화
        boardPane.setOnPieceClick(this::onPieceClicked);
        throwButton.setOnAction(e -> onRoll());

        // 2) 오른쪽에 고정될 윷 이미지 세팅
        URL imgUrl = getClass().getResource("/images/yut_throw.png");
        if (imgUrl != null) {
            staticYutView.setImage(new Image(imgUrl.toString()));
        } else {
            System.err.println("static yut image not found!");
        }
        defaultYutThrowImage = new Image(imgUrl.toString());
        staticYutView.setImage(defaultYutThrowImage);
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
        throwButton.setDisable(true);
        yutResults.clear();
        updateYutDisplay();

        boolean randomMode = FXDialog.askRandomMode();

        if (randomMode) {
            // 랜덤 모드: 즉시 결과 생성 + 애니메이션 → 콜백
            YutResult r = YutResult.throwYut(random);
            playYutAnimation(r, () -> handleSingleThrow(r, true));
        } else {
            // 지정 모드: 다이얼로그로 결과 선택 → 애니메이션 → 콜백
            YutResult r = FXDialog.askManualThrow();
            if (r == null) return;
            playYutAnimation(r, () -> handleSingleThrow(r, false));
        }
    }


    @FXML
    public void onPieceClicked(Piece p) {
        if (state != State.SELECTING_PIECE) return;
        if (!candidates.contains(p)) return;

        state = State.APPLYING_MOVE;

        // 몇 칸 이동할지
        int steps = stepsOf(currentYut);

        // 애니메이션 → 끝나면 모델에 반영 + 화면 갱신 + 후처리
        boardPane.animateAlongPath(p, steps, () -> {
            boolean captured = game.applyYutResult(currentYut, p);
            log(game.getCurrentPlayer().getName() + " 이동: " + currentYut);
            boardPane.redraw();
            handleAfterMove(captured);
        });
    }


    /**
     * 말 이동 애니메이션이 끝난 뒤 호출됩니다.
     * • 방금 적용한 currentYut을 yutResults에서 제거
     * • 캡처 보너스나 남은 윷 결과가 있으면 다음 결과 선택
     * • 승리 판정 및 턴 전환
     */
    private void handleAfterMove(boolean captured) {
        // ▶ 애니메이션 → PauseTransition 콜백이 끝난 다음 프레임
        Platform.runLater(() -> {
            // (1) 방금 적용한 currentYut 제거
            if (currentYut != null) {
                yutResults.remove(currentYut);
                currentYut = null;          // 강조 초기화
                updateYutDisplay();
            }

            // (2) 캡처 보너스가 있으면 보너스 던지기 체인으로 분기
            if (captured) {
                // 여기서 바로 askRandomMode 하면 에러 → runLater 안으로 들어왔으니 안전
                boolean randomMode = FXDialog.askRandomMode();
                bonusThrowChain(randomMode, new ArrayList<>());
                return;
            }

            // (3) 남은 윷 결과가 있으면 결과 선택 단계
            if (!yutResults.isEmpty()) {
                selectAndPromptNextYut();
                return;
            }

            // (4) 승리 판정
            if (game.isCurrentPlayerWin()) {
                boolean again = FXDialog.confirmRestart(game.getCurrentPlayer().getName());
                if (again) initGame();
                else throwButton.setDisable(true);
                return;
            }

            // (5) 턴 종료
            game.nextTurn();
            updateTurnLabel();
            updateStatusLabel();
            boardPane.highlightCurrentPlayer(game.getCurrentPlayer());
            throwButton.setDisable(false);
            state = State.IDLE;
        });
    }

    /**
     * 보너스 윷 던질 때마다 playYutAnimation → 결과 수집 → grantsExtraThrow 검사 → 재귀
     * 마지막에 selectAndPromptNextYut() 호출
     */
    private void bonusThrowChain(boolean randomMode, List<YutResult> collected) {
        YutResult br = randomMode
            ? YutResult.throwYut(random)
            : FXDialog.askManualThrow();    // runLater 로 들어왔으니 안전
        if (br == null) {
            // 취소 시
            yutResults.addAll(collected);
            updateYutDisplay();
            selectAndPromptNextYut();
            return;
        }

        playYutAnimation(br, () -> {
            // 애니메이션 끝난 다음 프레임
            Platform.runLater(() -> {
                collected.add(br);
                log("보너스 던진 결과: " + br);
                if (br.grantsExtraThrow()) {
                    bonusThrowChain(randomMode, collected);
                } else {
                    yutResults.addAll(collected);
                    updateYutDisplay();
                    selectAndPromptNextYut();
                }
            });
        });
    }



    /**
     * yutResults에서 currentYut을 다시 골라
     * promptPieceSelection() 단계로 넘어갑니다.
     */
    private void proceedToNextYut() {
        if (yutResults.size() > 1) {
            currentYut = FXDialog.selectYutResult(yutResults);
        } else {
            currentYut = yutResults.get(0);
        }
        promptPieceSelection();
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
    
    /** 단일 윷 결과 처리, 연속 던지기 재귀 호출 */
    private void handleSingleThrow(YutResult r, boolean randomMode) {
        log("던진 결과: " + r);
        yutResults.add(r);
        updateYutDisplay();  // 아직 highlight 없음

        if (r.grantsExtraThrow()) {
            // 연속 던지기 (랜덤/지정)
            if (randomMode) {
                YutResult next = YutResult.throwYut(random);
                playYutAnimation(next, () -> handleSingleThrow(next, true));
            } else {
                YutResult next = FXDialog.askManualThrow();
                if (next != null) {
                    playYutAnimation(next, () -> handleSingleThrow(next, false));
                } else {
                    selectAndPromptNextYut();
                }
            }
        } else {
            selectAndPromptNextYut();
        }
    }

    private void selectAndPromptNextYut() {
        // 반드시 runLater 바깥: 이미 안전한 타이밍임
        if (yutResults.size() > 1) {
            currentYut = FXDialog.selectYutResult(yutResults);
        } else {
            currentYut = yutResults.get(0);
        }
        updateYutDisplay();   // 이 시점에만 currentYut 강조
        promptPieceSelection();
    }


    /** yutResults에서 하나를 최종 선택하고 말 선택 단계로 */
    private void finalizeRolls() {
        if (yutResults.size() > 1) {
            YutResult sel = FXDialog.selectYutResult(yutResults);
            if (sel == null) return;
            currentYut = sel;
            yutResults.remove(sel);
        } else {
            currentYut = yutResults.get(0);
            yutResults.remove(0);
        }
        updateYutDisplay();   // ← 반드시 여기서 갱신
        promptPieceSelection();
    }

    
    private void updateYutDisplay() {
        yutBox.getChildren().clear();
        for (int i = 0; i < yutResults.size(); i++) {
            YutResult r = yutResults.get(i);
            Label lbl = new Label(r.toString() + (i < yutResults.size() - 1 ? ", " : ""));
            // 글자 크기를 16px로 키우고, currentYut만 강조
            lbl.setStyle(String.join(";",
                "-fx-font-size: 16px",
                r.equals(currentYut)
                    ? "-fx-text-fill: crimson; -fx-font-weight: bold"
                    : ""
            ));
            yutBox.getChildren().add(lbl);
        }
    }
   


 // 1) 결과 → 이미지 맵핑
    private final Map<YutResult, Image> resultImages = Map.of(
    	    YutResult.DO,      new Image(getClass().getResource("/images/do.png").toString()),
    	    YutResult.GAE,     new Image(getClass().getResource("/images/gae.png").toString()),
    	    YutResult.GEOL,    new Image(getClass().getResource("/images/geol.png").toString()),
    	    YutResult.YUT,     new Image(getClass().getResource("/images/yut.png").toString()),
    	    YutResult.MO,      new Image(getClass().getResource("/images/mo.png").toString()),
    	    YutResult.BACKDO, new Image(getClass().getResource("/images/backdo.png").toString())
    	);


    /**
     * 윷 던지기 애니메이션 + 면 결과 플리핑
     */
    private void playYutAnimation(YutResult result, Runnable onFinished) {
        // 이미 staticYutView가 yutContainer 안에 있으므로 레이아웃만 다시 설정
    	 staticYutView.setImage(defaultYutThrowImage);
        staticYutView.setTranslateX(0);
        staticYutView.setTranslateY(0);
        staticYutView.setRotate(0);

        // 1) 튕기고 회전하는 애니메이션
        TranslateTransition tt = new TranslateTransition(Duration.seconds(0.4), staticYutView);
        tt.setByY(-120); tt.setAutoReverse(true); tt.setCycleCount(2);

        RotateTransition rt = new RotateTransition(Duration.seconds(0.4), staticYutView);
        rt.setByAngle(720); rt.setAutoReverse(true); rt.setCycleCount(2);

        ParallelTransition toss = new ParallelTransition(tt, rt);
        toss.setOnFinished(e -> {
            // 결과 이미지 교체
            staticYutView.setImage(resultImages.get(result));

            // 잠깐 딜레이
            PauseTransition pause = new PauseTransition(Duration.seconds(0.8));
            pause.setOnFinished(ev -> {
                // ▶▶ 여기서 바로 onFinished.run() 대신
                // 다음 JavaFX 펄스에 실행하도록 감쌉니다.
                Platform.runLater(onFinished);
            });
            pause.play();
        });
        toss.play();
    }

    /** 짧은 Timeline으로 이미지 빠르게 교체 후, 마지막에 실제 결과 이미지로 고정 */
    private void flipToResult(ImageView iv, YutResult result, Runnable onFinished) {
        List<Image> frames = List.copyOf(resultImages.values());
        double frameDuration = 0.1;  // 0.1초마다 교체
        Timeline flip = new Timeline();
        int cycles = 10;  // 총 10프레임(1초)
        for (int i = 0; i < cycles; i++) {
            Image frameImg = frames.get(i % frames.size());
            flip.getKeyFrames().add(new KeyFrame(
                Duration.seconds(i * frameDuration),
                evt -> iv.setImage(frameImg)
            ));
        }
        // 마지막 프레임에서 실제 결과 이미지로 고정
        flip.getKeyFrames().add(new KeyFrame(
            Duration.seconds(cycles * frameDuration),
            evt -> {
                iv.setImage(resultImages.get(result));
                // 약간의 지연 후에 지우고 다음 로직
                PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                pause.setOnFinished(ev2 -> {
                    boardPane.getChildren().remove(iv);
                    onFinished.run();
                });
                pause.play();
            }
        ));
        flip.play();
    }
   

}
