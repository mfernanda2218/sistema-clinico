package clinica.view;

import clinica.service.ConsultaService;
import clinica.service.MedicoService;
import clinica.service.PacienteService;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class NavegacaoView extends TabPane {
    public NavegacaoView(PacienteService pacientes, MedicoService medicos, ConsultaService consultas) {
        PacienteView telaPacientes = new PacienteView(pacientes);
        MedicoView telaMedicos = new MedicoView(medicos);
        ConsultaView telaConsultas = new ConsultaView(consultas, pacientes, medicos);
        Tab abaPacientes = new Tab("Pacientes", telaPacientes);
        Tab abaMedicos = new Tab("Médicos", telaMedicos);
        Tab abaConsultas = new Tab("Consultas", telaConsultas);
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        getTabs().addAll(abaPacientes, abaMedicos, abaConsultas);
        getSelectionModel().selectedItemProperty().addListener((obs, anterior, atual) -> {
            if (atual == abaConsultas) {
                telaConsultas.atualizar();
            }
        });
    }
}
