package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import controller.FXStartController;

public class MainFX extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // StartPane.fxml 로드
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StartPane.fxml"));
        Parent root = loader.load();
        FXStartController startCtrl = loader.getController();
        startCtrl.setPrimaryStage(primaryStage);
        primaryStage.setScene(new Scene(root, 900, 800));
        primaryStage.setTitle("윷놀이 설정");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
