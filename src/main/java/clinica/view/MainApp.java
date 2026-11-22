package clinica.view;

import clinica.repository.ConsultaRepositorioJson;
import clinica.repository.MedicoRepositorioJson;
import clinica.repository.PacienteRepositorioJson;
import clinica.service.ConsultaService;
import clinica.service.MedicoService;
import clinica.service.PacienteService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        stage.setTitle("Sistema Clínico - UFAL Arapiraca");
        stage.setScene(new Scene(root, 900, 600));
        stage.setMinWidth(700);
        stage.setMinHeight(500);
        stage.show();
        try {
            PacienteRepositorioJson pacientes = new PacienteRepositorioJson();
            MedicoRepositorioJson medicos = new MedicoRepositorioJson();
            ConsultaRepositorioJson consultas = new ConsultaRepositorioJson(pacientes, medicos);
            PacienteService pacienteService = new PacienteService(pacientes);
            MedicoService medicoService = new MedicoService(medicos, consultas);
            ConsultaService consultaService = new ConsultaService(consultas, pacienteService, medicoService, 0);
            root.setCenter(new NavegacaoView(pacienteService, medicoService, consultaService));
        } catch (Exception e) {
            root.setCenter(new Label("Não foi possível carregar os dados. Verifique os arquivos e reabra o sistema."));
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.initOwner(stage);
            alerta.setHeaderText("Erro ao carregar o sistema");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}