package app;

import controller.FXStartController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/StartPane.fxml")
        );
        Parent startRoot = loader.load();

        // controller.FXStartController 가 여기에 매핑됩니다
        FXStartController startCtrl = loader.getController();
        startCtrl.setPrimaryStage(stage);

        Scene scene = new Scene(startRoot, 600, 400);
        stage.setScene(scene);
        stage.setTitle("윷놀이 게임 시작");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
