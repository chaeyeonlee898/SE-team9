package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Board;
import model.Game;
import model.PentagonBoard;
import model.SquareBoard;
import model.HexagonBoard;
import javafx.scene.Scene;

import java.net.URL;
import java.util.ResourceBundle;

public class FXStartController implements Initializable {
    @FXML private ComboBox<Integer> playerCountBox;
    @FXML private ComboBox<Integer> pieceCountBox;
    @FXML private ComboBox<String> boardTypeBox;

    private Stage primaryStage;

    // JavaFX가 this 컨트롤러를 생성할 때 호출됨
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 아무 추가 초기화 필요 없음
    }

    // MainFX에서 primaryStage를 주입해줘야 합니다
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    private void onStartGame() throws Exception {
        int numPlayers = playerCountBox.getValue();
        int numPieces  = pieceCountBox.getValue();
        String type    = boardTypeBox.getValue();

        Board board;
        switch (type) {
            case "오각형": board = new PentagonBoard(); break;
            case "육각형": board = new HexagonBoard(); break;
            default:       board = new SquareBoard();
        }

        Game game = new Game(numPlayers, numPieces, board);

        // GamePane.fxml 로드
        URL fxmlUrl = getClass().getResource("/fxml/GamePane.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Pane gameRoot;
        try {
            gameRoot = (Pane) loader.load();       // ← 이 한 줄에서
            System.out.println("   loader.load() 성공");
        } catch (Exception loadEx) {
            System.err.println("   >>> loader.load() 에러 발생: ");
            loadEx.printStackTrace();              // ← 여기에서 원인 스택 전체를 출력!
            return;
        }
        FXGameController gameCtrl = loader.getController();
        gameCtrl.setGame(game);
        gameCtrl.initGame();       // 초기화 호출

        Scene gameScene = new Scene(gameRoot, 1000, 1000);
        primaryStage.setScene(gameScene);
    }



}
