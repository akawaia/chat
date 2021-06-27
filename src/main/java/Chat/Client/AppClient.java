package Chat.Client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class AppClient extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/scene.fxml"));
        Parent root = loader.load();
        ChatController.setStage(stage);

        stage.setResizable(false);
        Scene scene = new Scene(root);
//        scene.getStylesheets().add((getClass().getResource("/style.css")).toExternalForm()); //Подключение css файла
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}

