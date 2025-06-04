package controller;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.animation.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.util.Duration;
import model.*;
import view.javafx.BoardPane;
import view.javafx.FXDialog;

import java.io.IOException;
import java.net.URL;
import java.util.*;

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
            playYutAnimation(r, () -> handleSingleThrow(r));
        } else {
            // 지정 모드: 다이얼로그로 결과 선택 → 애니메이션 → 콜백
            YutResult r = FXDialog.askManualThrow();
            if (r == null) return;
            playYutAnimation(r, () -> handleSingleThrow(r));
        }
    }


    @FXML
    public void onPieceClicked(Piece p) {
        if (state != State.SELECTING_PIECE) return;
        if (!candidates.contains(p)) return;

        state = State.APPLYING_MOVE;

        // **①** 클릭 전 위치(oldPos) 저장 (null이면 대기장)
        BoardNode oldPos = p.getPosition();
        int steps = stepsOf(currentYut);

        // **②** 애니메이션만 실행 (모델 적용은 끝난 뒤 onFinished 에서)
        boardPane.animateAlongPath(p, oldPos, steps, () -> {
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

            // (2) 먼저 승리 체크
            if (game.isCurrentPlayerWin()) {
                boolean again = FXDialog.confirmRestart(game.getCurrentPlayer().getName());
                Stage stage = (Stage) throwButton.getScene().getWindow();

                if (again) {
                    try {
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/fxml/StartPane.fxml")
                        );
                        Parent startRoot = loader.load();
                        FXStartController startCtrl = loader.getController();
                        startCtrl.setPrimaryStage(stage);
                        Scene startScene = new Scene(startRoot, 600, 400);
                        stage.setScene(startScene);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Platform.exit();
                }
                return;
            }

            // (2) 캡처 보너스가 있으면 보너스 던지기 체인으로 분기
            if (captured) {
                // 여기서 바로 askRandomMode 하면 에러 → runLater 안으로 들어왔으니 안전
                bonusThrowChain(new ArrayList<>());
                return;
            }

            // (3) 남은 윷 결과가 있으면 결과 선택 단계
            if (!yutResults.isEmpty()) {
                selectAndPromptNextYut();
                return;
            }

            if (game.isCurrentPlayerWin()) {
                // 다이얼로그로 묻기
                boolean again = FXDialog.confirmRestart(game.getCurrentPlayer().getName());

                // 현재 Window(Stage) 참조
                Stage stage = (Stage) throwButton.getScene().getWindow();

                if (again) {
                    // 1) StartPane.fxml 로드
                    try {
                        FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/fxml/StartPane.fxml")
                        );
                        Parent startRoot = loader.load();

                        // 2) 컨트롤러에 Stage 주입
                        FXStartController startCtrl = loader.getController();
                        startCtrl.setPrimaryStage(stage);

                        // 3) 씬 교체
                        Scene startScene = new Scene(startRoot, 600, 400);
                        stage.setScene(startScene);

                    } catch (IOException ex) {
                        ex.printStackTrace();
                        // 혹은 에러 표시
                    }
                } else {
                    // “아니요” → 애플리케이션 종료
                    Platform.exit();
                }
                return;
            }

            // ④ 턴 종료
            game.nextTurn();
            updateTurnLabel();
            updateStatusLabel();
            boardPane.highlightCurrentPlayer(game.getCurrentPlayer());
            throwButton.setDisable(false);
            state = State.IDLE;
        
        });
    }

    /**
     * 캡처 보너스 던지기 로직. grantsExtraThrow 때마다
     * 지정/랜덤을 새로 물어보도록 변경.
     */
    private void bonusThrowChain(List<YutResult> collected) {
        // ★ 여기서도 mode를 매번 물어봄
        boolean nextRandom = FXDialog.askRandomMode();
        YutResult br = nextRandom
            ? YutResult.throwYut(random)
            : FXDialog.askManualThrow();

        if (br == null) {
            // 수동 모드에서 취소한 경우, 지금까지 모은 것들만 남기고 다음 단계
            yutResults.addAll(collected);
            updateYutDisplay();
            selectAndPromptNextYut();
            return;
        }

        playYutAnimation(br, () -> {
            Platform.runLater(() -> {
                collected.add(br);
                log("보너스 던진 결과: " + br);
                if (br.grantsExtraThrow()) {
                    // recursive call without passing a flag
                    bonusThrowChain(collected);
                } else {
                    yutResults.addAll(collected);
                    updateYutDisplay();
                    selectAndPromptNextYut();
                }
            });
        });
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
    
    /**
     * 단일 윷 결과 처리, grantsExtraThrow() 일 때마다
     * 던지기 모드를 새로 선택하도록 변경.
     */
    private void handleSingleThrow(YutResult r) {
        log("던진 결과: " + r);
        yutResults.add(r);
        updateYutDisplay();

        if (r.grantsExtraThrow()) {
            // 매번 모드 선택
            boolean randomMode = FXDialog.askRandomMode();
            if (randomMode) {
                YutResult next = YutResult.throwYut(random);
                playYutAnimation(next, () -> handleSingleThrow(next));
            } else {
                YutResult next = FXDialog.askManualThrow();
                if (next != null) {
                    playYutAnimation(next, () -> handleSingleThrow(next));
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
   

}
