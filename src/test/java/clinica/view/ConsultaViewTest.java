package clinica.view;

import clinica.model.*;
import clinica.repository.*;
import clinica.service.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

public class ConsultaViewTest {
    @Test
    public void mostraConsultaEValorCalculado() throws Exception {
        Path pasta = Files.createTempDirectory("tela-consultas");
        try {
            PacienteRepositorioJson pacientes = new PacienteRepositorioJson(pasta.resolve("pacientes.json").toString());
            MedicoRepositorioJson medicos = new MedicoRepositorioJson(pasta.resolve("medicos.json").toString());
            pacientes.cadastrar(new Paciente("Ana", "12345678900", "9999", LocalDate.of(2000, 1, 1), "Unimed"));
            medicos.cadastrar(new Clinico("Bia", "98765432100", "9999", "123"));
            ConsultaRepositorioJson consultas = new ConsultaRepositorioJson(pasta.resolve("consultas.json").toString(), pacientes, medicos);
            PacienteService ps = new PacienteService(pacientes);
            MedicoService ms = new MedicoService(medicos, consultas);
            ConsultaService cs = new ConsultaService(consultas, ps, ms, 20);
            Consulta consulta = cs.agendar("12345678900", "123", LocalDateTime.now().plusDays(1).withSecond(0).withNano(0), "Rotina");
            try {
                Platform.startup(() -> Platform.setImplicitExit(false));
            } catch (IllegalStateException e) {
                Platform.setImplicitExit(false);
            }
            FutureTask<Void> tarefa = new FutureTask<>(() -> {
                ConsultaView tela = new ConsultaView(cs, ps, ms);
                new Scene(tela, 900, 600);
                tela.applyCss();
                tela.layout();
                TableView<Consulta> tabela = (TableView<Consulta>) tela.getCenter();
                assertEquals(1, tabela.getItems().size());
                Button remarcar = (Button) tela.lookupAll(".button").stream()
                        .filter(n -> n instanceof Button b && b.getText().equals("Remarcar selecionada"))
                        .findFirst().orElseThrow();
                assertTrue(remarcar.isDisabled());
                tabela.getSelectionModel().select(consulta);
                assertFalse(remarcar.isDisabled());
                assertTrue(((Label) tela.getBottom()).getText().contains("120,00"));
                assertTrue(((Label) tela.getBottom()).getText().contains("Convênio"));
                Button agendar = (Button) tela.lookupAll(".button").stream()
                        .filter(n -> n instanceof Button b && b.getText().equals("Agendar"))
                        .findFirst().orElseThrow();
                String arquivoAntes = Files.readString(pasta.resolve("consultas.json"));
                java.util.concurrent.atomic.AtomicReference<String> aviso = new java.util.concurrent.atomic.AtomicReference<>();
                Platform.runLater(() -> {
                    for (javafx.stage.Window janela : new java.util.ArrayList<>(javafx.stage.Window.getWindows())) {
                        if (janela.getScene().getRoot() instanceof DialogPane dialogo) {
                            aviso.set(dialogo.getHeaderText());
                            janela.hide();
                        }
                    }
                });
                agendar.fire();
                assertEquals("Horário indisponível", aviso.get());
                assertEquals(arquivoAntes, Files.readString(pasta.resolve("consultas.json")));
                assertEquals(1, cs.listar().size());
                NavegacaoView abas = new NavegacaoView(ps, ms, cs);
                new Scene(abas, 900, 600);
                abas.getSelectionModel().select(2);
                ConsultaView consultasView = (ConsultaView) abas.getTabs().get(2).getContent();
                consultasView.applyCss();
                consultasView.layout();
                ComboBox<Paciente> campoPaciente = (ComboBox<Paciente>) consultasView.lookup("#pacienteConsulta");
                ComboBox<Medico> campoMedico = (ComboBox<Medico>) consultasView.lookup("#medicoConsulta");
                assertEquals(1, campoPaciente.getItems().size());
                abas.getSelectionModel().select(0);
                Paciente novo = new Paciente("Caio", "11122233344", "9999", LocalDate.of(2000, 1, 1));
                ps.cadastrar(novo);
                abas.getSelectionModel().select(1);
                Medico novoMedico = ms.cadastrar("Pediatra", "Dora", "44455566677", "9999", "456");
                abas.getSelectionModel().select(2);
                assertTrue(campoPaciente.getItems().contains(novo));
                assertTrue(campoMedico.getItems().contains(novoMedico));
                campoPaciente.setValue(novo);
                campoMedico.setValue(novoMedico);
                abas.getSelectionModel().select(0);
                ps.excluir(novo.getCpf());
                ms.excluir(novoMedico.getCrm());
                abas.getSelectionModel().select(2);
                assertFalse(campoPaciente.getItems().contains(novo));
                assertFalse(campoMedico.getItems().contains(novoMedico));
                assertNull(campoPaciente.getValue());
                assertNull(campoMedico.getValue());
                assertSame(consultasView, abas.getTabs().get(2).getContent());
                return null;
            });
            Platform.runLater(tarefa);
            tarefa.get(30, TimeUnit.SECONDS);
        } finally {
            for (String nome : new String[]{"consultas.json", "medicos.json", "pacientes.json"}) {
                Files.deleteIfExists(pasta.resolve(nome));
            }
            Files.deleteIfExists(pasta);
        }
    }
}
