package clinica.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        // ===== Abas =====
        TabPane tabPane = new TabPane();

        // Aba Pacientes
        Tab tabPacientes = new Tab("Pacientes");
        tabPacientes.setClosable(false);
        tabPacientes.setContent(criarPlaceholder("Tela de Pacientes (A) - em construção"));

        // Aba Médicos
        Tab tabMedicos = new Tab("Médicos");
        tabMedicos.setClosable(false);
        tabMedicos.setContent(criarPlaceholder("Tela de Médicos (B) - em construção"));

        // Aba Consultas
        Tab tabConsultas = new Tab("Consultas");
        tabConsultas.setClosable(false);
        tabConsultas.setContent(criarPlaceholder("Tela de Consultas (C) - em construção"));

        tabPane.getTabs().addAll(tabPacientes, tabMedicos, tabConsultas);

        // ===== Layout principal =====
        BorderPane root = new BorderPane();
        root.setCenter(tabPane);

        // ===== Cena e Stage =====
        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Sistema Clínico - UFAL Arapiraca");
        stage.setScene(scene);
        stage.setMinWidth(700);
        stage.setMinHeight(500);
        stage.show();
    }

    /**
     * Cria um painel simples com texto centralizado.
     * Depois cada pessoa substituirá esse conteúdo pela sua tela real.
     */
    private StackPane criarPlaceholder(String texto) {
        Label label = new Label(texto);
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #555;");
        return new StackPane(label);
    }

    public static void main(String[] args) {
        launch(args);
    }
}