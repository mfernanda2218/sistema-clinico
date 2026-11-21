package clinica.view;

import clinica.exception.ArquivoInvalidoException;
import clinica.service.ConsultaService;
import clinica.repository.PacienteRepositorioJson;
import clinica.repository.MedicoRepositorioJson;
import clinica.repository.ConsultaRepositorioJson;
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
        PacienteRepositorioJson pacientes = null;
        MedicoRepositorioJson medicos = null;
        Tab tabPacientes = new Tab("Pacientes");
        tabPacientes.setClosable(false);
        try {
            pacientes = new PacienteRepositorioJson();
            pacienteService = new PacienteService(pacientes);
            tabPacientes.setContent(new PacienteView(pacienteService));
        } catch (ArquivoInvalidoException e) {
            mostrarAlertaErro("Erro ao carregar pacientes", e.getMessage());
            tabPacientes.setContent(criarPlaceholder(
                    "Não foi possível carregar os pacientes.\nO arquivo está corrompido ou inválido."));
        }
        Tab tabMedicos = new Tab("Médicos");
        tabMedicos.setClosable(false);
        try {
            medicos = new MedicoRepositorioJson();
            medicoService = new MedicoService(medicos);
            tabMedicos.setContent(criarPlaceholder("Tela de Médicos (B) - em construção"));
        } catch (ArquivoInvalidoException e) {
            mostrarAlertaErro("Erro ao carregar médicos", e.getMessage());
            tabMedicos.setContent(criarPlaceholder(
                    "Não foi possível carregar os médicos.\nO arquivo está corrompido ou inválido."));
        }
        Tab tabConsultas = new Tab("Consultas");
        tabConsultas.setClosable(false);
        if (pacientes != null && medicos != null) {
            try {
                ConsultaRepositorioJson consultas = new ConsultaRepositorioJson(pacientes, medicos);
                medicoService = new MedicoService(medicos, consultas);
                consultaService = new ConsultaService(consultas, pacienteService, medicoService, 0);
                ConsultaView telaConsultas = new ConsultaView(consultaService, pacienteService, medicoService);
                tabConsultas.setContent(telaConsultas);
                tabConsultas.setOnSelectionChanged(e -> {
                    if (tabConsultas.isSelected()) {
                        telaConsultas.atualizar();
                    }
                });
                tabMedicos.setContent(new MedicoView(medicoService));
            } catch (Exception e) {
                mostrarAlertaErro("Erro ao carregar consultas", e.getMessage());
                tabConsultas.setContent(criarPlaceholder("Não foi possível carregar as consultas."));
            }
        } else {
            tabConsultas.setContent(criarPlaceholder("Corrija o carregamento de pacientes e médicos para acessar as consultas."));
        }
        tabPane.getTabs().addAll(tabPacientes, tabMedicos, tabConsultas);
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