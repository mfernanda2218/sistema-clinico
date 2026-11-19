package clinica.view;

import clinica.exception.ArquivoInvalidoException;
import clinica.service.ConsultaService;
import clinica.service.MedicoService;
import clinica.service.PacienteService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    private PacienteService pacienteService;
    private MedicoService medicoService;
    private ConsultaService consultaService;

    @Override
    public void start(Stage stage) throws ArquivoInvalidoException {
        TabPane tabPane = new TabPane();

        // ===== Aba Pacientes =====
        Tab tabPacientes = new Tab("Pacientes");
        tabPacientes.setClosable(false);
        try {
            pacienteService = new PacienteService(); // carrega o JSON internamente
            tabPacientes.setContent(new PacienteView(pacienteService));
        } catch (ArquivoInvalidoException e) {
            mostrarAlertaErro("Erro ao carregar pacientes", e.getMessage());
            tabPacientes.setContent(criarPlaceholder(
                    "Não foi possível carregar os pacientes.\nO arquivo está corrompido ou inválido."));
        }

        // ===== Aba Médicos =====
        Tab tabMedicos = new Tab("Médicos");
        tabMedicos.setClosable(false);
        try {
            medicoService = new MedicoService(); // carrega o JSON internamente
            // Quando a tela de médicos estiver pronta, troque a linha abaixo:
            // tabMedicos.setContent(new MedicoView(medicoService));
            tabMedicos.setContent(criarPlaceholder("Tela de Médicos (B) - em construção"));
        } catch (ArquivoInvalidoException e) {
            mostrarAlertaErro("Erro ao carregar médicos", e.getMessage());
            tabMedicos.setContent(criarPlaceholder(
                    "Não foi possível carregar os médicos.\nO arquivo está corrompido ou inválido."));
        }

        // ===== Aba Consultas =====
        // quando implementar a tela, implementar o try catch
        Tab tabConsultas = new Tab("Consultas");
        tabConsultas.setClosable(false);
        consultaService = new ConsultaService(null, pacienteService, medicoService, 0); // carrega o JSON internamente
        // Quando a tela de consultas estiver pronta, troque a linha abaixo:
        // tabConsultas.setContent(new ConsultaView(consultaService));
        tabConsultas.setContent(criarPlaceholder("Tela de Consultas (C) - em construção"));

        tabPane.getTabs().addAll(tabPacientes, tabMedicos, tabConsultas);

        // ===== Layout principal =====
        BorderPane root = new BorderPane();
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Sistema Clínico - UFAL Arapiraca");
        stage.setScene(scene);
        stage.setMinWidth(700);
        stage.setMinHeight(500);
        stage.show();
    }

    private void mostrarAlertaErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro de Persistência");
        alert.setHeaderText(titulo);
        alert.setContentText(mensagem + "\n\nO arquivo NÃO foi apagado. Verifique o conteúdo ou restaure um backup.");
        alert.showAndWait();
    }

    private StackPane criarPlaceholder(String texto) {
        Label label = new Label(texto);
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #555; -fx-text-alignment: center;");
        label.setWrapText(true);
        return new StackPane(label);
    }

    public static void main(String[] args) {
        launch(args);
    }
}