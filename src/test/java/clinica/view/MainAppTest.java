package clinica.view;

import clinica.exception.ArquivoInvalidoException;
import clinica.model.Clinico;
import clinica.model.Consulta;
import clinica.model.Paciente;
import clinica.repository.ConsultaRepositorioJson;
import clinica.repository.MedicoRepositorioJson;
import clinica.repository.PacienteRepositorioJson;
import clinica.service.ConsultaService;
import clinica.service.MedicoService;
import clinica.service.PacienteService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class MainAppTest {
    @Rule
    public TemporaryFolder temporaria = new TemporaryFolder();

    @Test
    public void impedeExcluirReferenciasDeConsultasFuturasEPassadas() throws Exception {
        for (boolean passada : new boolean[]{false, true}) {
            Path pasta = temporaria.newFolder().toPath();
            Consulta original = cadastrarDados(pasta);
            Path arquivo = pasta.resolve("consultas.json");
            if (passada) {
                String json = Files.readString(arquivo);
                com.google.gson.JsonArray lista = com.google.gson.JsonParser.parseString(json).getAsJsonArray();
                lista.get(0).getAsJsonObject().addProperty("dataHora", original.getDataHora().minusDays(2).toString());
                Files.writeString(arquivo, lista.toString());
            }
            PacienteRepositorioJson pacientes = new PacienteRepositorioJson(pasta.resolve("pacientes.json").toString());
            MedicoRepositorioJson medicos = new MedicoRepositorioJson(pasta.resolve("medicos.json").toString());
            ConsultaRepositorioJson consultas = new ConsultaRepositorioJson(arquivo.toString(), pacientes, medicos);
            assertEquals(passada, consultas.buscar(original.getId()).getDataHora().isBefore(LocalDateTime.now()));
            PacienteService ps = new PacienteService(pacientes, consultas);
            MedicoService ms = new MedicoService(medicos, consultas);
            assertThrows(IllegalStateException.class, () -> ps.excluir("529.982.247-25"));
            assertThrows(IllegalStateException.class, () -> ms.excluir("123"));
            pacientes.carregar();
            medicos.carregar();
            consultas.carregar();
            assertNotNull(consultas.buscar(original.getId()).getPaciente());
            assertNotNull(consultas.buscar(original.getId()).getMedico());
            consultas.excluir(original.getId());
            ps.excluir("52998224725");
            ms.excluir("123");
            assertTrue(pacientes.listar().isEmpty());
            assertTrue(medicos.listar().isEmpty());
        }
    }

    private Consulta cadastrarDados(Path pasta) throws Exception {
        PacienteRepositorioJson pacientes = new PacienteRepositorioJson(pasta.resolve("pacientes.json").toString());
        MedicoRepositorioJson medicos = new MedicoRepositorioJson(pasta.resolve("medicos.json").toString());
        ConsultaRepositorioJson consultas = new ConsultaRepositorioJson(pasta.resolve("consultas.json").toString(), pacientes, medicos);
        pacientes.cadastrar(new Paciente("Ana", "52998224725", "9999", LocalDate.of(2000, 1, 1)));
        medicos.cadastrar(new Clinico("Bia", "11144477735", "9999", "123"));
        ConsultaService service = new ConsultaService(consultas, new PacienteService(pacientes), new MedicoService(medicos), 0);
        return service.agendar("52998224725", "123", LocalDateTime.now().plusDays(1), "Rotina");
    }

    @Test
    public void salvaOsTresRepositoriosAoEncerrarEReconectaAoReabrir() throws Exception {
        Path pasta = temporaria.getRoot().toPath();
        Consulta original = cadastrarDados(pasta);
        MainApp app = new MainApp(pasta);
        app.carregarDados();
        Files.writeString(pasta.resolve("pacientes.json"), "[]");
        Files.writeString(pasta.resolve("medicos.json"), "[]");
        Files.writeString(pasta.resolve("consultas.json"), "[]");
        app.stop();

        PacienteRepositorioJson pacientes = new PacienteRepositorioJson(pasta.resolve("pacientes.json").toString());
        MedicoRepositorioJson medicos = new MedicoRepositorioJson(pasta.resolve("medicos.json").toString());
        ConsultaRepositorioJson consultas = new ConsultaRepositorioJson(pasta.resolve("consultas.json").toString(), pacientes, medicos);
        Consulta carregada = consultas.buscar(original.getId());
        assertNotNull(carregada);
        assertSame(pacientes.buscar("52998224725"), carregada.getPaciente());
        assertSame(medicos.buscar("123"), carregada.getMedico());
        assertEquals(original.getDataHora(), carregada.getDataHora());
        assertEquals(original.calcularValor(), carregada.calcularValor(), 0.001);
    }

    @Test
    public void naoSobrescreveArquivosSeCarregamentoFalhar() throws Exception {
        Path pasta = temporaria.getRoot().toPath();
        cadastrarDados(pasta);
        String pacientes = Files.readString(pasta.resolve("pacientes.json"));
        String medicos = Files.readString(pasta.resolve("medicos.json"));
        Files.writeString(pasta.resolve("consultas.json"), "invalido");
        MainApp app = new MainApp(pasta);
        assertThrows(ArquivoInvalidoException.class, app::carregarDados);
        app.stop();
        assertEquals(pacientes, Files.readString(pasta.resolve("pacientes.json")));
        assertEquals(medicos, Files.readString(pasta.resolve("medicos.json")));
        assertEquals("invalido", Files.readString(pasta.resolve("consultas.json")));
    }

    @Test
    public void tentaSalvarOsOutrosRepositoriosQuandoUmFalha() throws Exception {
        Path pasta = temporaria.getRoot().toPath();
        cadastrarDados(pasta);
        MainApp app = new MainApp(pasta);
        app.carregarDados();
        Files.delete(pasta.resolve("medicos.json"));
        Files.createDirectory(pasta.resolve("medicos.json"));
        Files.writeString(pasta.resolve("pacientes.json"), "[]");
        Files.writeString(pasta.resolve("consultas.json"), "[]");
        IllegalStateException erro = assertThrows(IllegalStateException.class, app::stop);
        assertEquals(1, erro.getSuppressed().length);
        assertTrue(Files.readString(pasta.resolve("pacientes.json")).contains("52998224725"));
        assertTrue(Files.readString(pasta.resolve("consultas.json")).contains("52998224725"));
    }
}
