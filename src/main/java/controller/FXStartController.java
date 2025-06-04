package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.Scene;
import model.Board;
import model.Game;
import model.PentagonBoard;
import model.SquareBoard;
import model.HexagonBoard;

import java.net.URL;
import java.util.ResourceBundle;

public class FXStartController implements Initializable {
    @FXML private ComboBox<Integer> playerCountBox;
    @FXML private ComboBox<Integer> pieceCountBox;
    @FXML private ComboBox<String>  boardTypeBox;

    private Stage primaryStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 콤보박스 초기값 세팅 등 필요하면 여기서
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    private void onStartGame() {
        try {
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

            // 1) GamePane.fxml 로드
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/GamePane.fxml")
            );
            Pane gameRoot = loader.load();

            // 2) 컨트롤러와 모델 연결
            FXGameController gameCtrl = loader.getController();
            gameCtrl.setGame(game);

            // 3) 씬 교체
            Scene gameScene = new Scene(gameRoot, 1000, 1000);
            primaryStage.setScene(gameScene);

            // 4) 씬이 완전히 교체된 다음에 화면 초기화
            gameCtrl.initGame();  // drawBoard + highlightCurrentPlayer 호출

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
