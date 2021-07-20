package openjfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainInside extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("MainPane.fxml"));
        Image image = new Image("/pictures/mainPanePicture.png");
        stage.getIcons().addAll(image);
        stage.setScene(new Scene(root));
        stage.setTitle("Загрузчик моделей");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
