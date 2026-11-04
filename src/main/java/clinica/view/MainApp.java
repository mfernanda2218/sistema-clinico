package clinica.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        Label label = new Label("Sistema Clínico - Hello JavaFX + Maven + Gson");
        Scene scene = new Scene(new StackPane(label), 500, 300);
        stage.setTitle("Sistema Clínico - UFAL Arapiraca");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}