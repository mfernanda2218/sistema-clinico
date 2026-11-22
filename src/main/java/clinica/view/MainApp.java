package clinica.view;

import clinica.repository.ConsultaRepositorioJson;
import clinica.repository.MedicoRepositorioJson;
import clinica.repository.PacienteRepositorioJson;
import clinica.service.ConsultaService;
import clinica.service.MedicoService;
import clinica.service.PacienteService;
import clinica.interfaces.Repositorio;
import clinica.exception.ArquivoInvalidoException;
import java.nio.file.Path;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {
    private final Path pastaDados;
    private PacienteRepositorioJson pacientes;
    private MedicoRepositorioJson medicos;
    private ConsultaRepositorioJson consultas;
    private boolean dadosCarregados;

    public MainApp() {
        this(Path.of("dados"));
    }

    MainApp(Path pastaDados) {
        this.pastaDados = pastaDados;
    }

    void carregarDados() throws ArquivoInvalidoException {
        dadosCarregados = false;
        pacientes = new PacienteRepositorioJson(pastaDados.resolve("pacientes.json").toString());
        medicos = new MedicoRepositorioJson(pastaDados.resolve("medicos.json").toString());
        consultas = new ConsultaRepositorioJson(pastaDados.resolve("consultas.json").toString(), pacientes, medicos);
        dadosCarregados = true;
    }

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        stage.setTitle("Sistema Clínico - UFAL Arapiraca");
        stage.setScene(new Scene(root, 900, 600));
        stage.setMinWidth(700);
        stage.setMinHeight(500);
        stage.show();
        try {
            carregarDados();
            PacienteService pacienteService = new PacienteService(pacientes, consultas);
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

    @Override
    public void stop() {
        if (!dadosCarregados) {
            return;
        }
        IllegalStateException falha = null;
        for (Repositorio<?> repositorio : List.of(pacientes, medicos, consultas)) {
            try {
                repositorio.salvar();
            } catch (RuntimeException e) {
                if (falha == null) {
                    falha = new IllegalStateException("Não foi possível salvar todos os dados ao fechar o sistema.");
                }
                falha.addSuppressed(e);
            }
        }
        if (falha != null) {
            throw falha;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
