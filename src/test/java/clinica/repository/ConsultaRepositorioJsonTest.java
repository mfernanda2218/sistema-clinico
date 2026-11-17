package clinica.repository;

import clinica.interfaces.Repositorio;
import clinica.model.*;
import clinica.service.ConsultaService;
import clinica.service.PacienteService;
import clinica.service.MedicoService;
import clinica.exception.ConsultaNaoEncontradaException;
import clinica.exception.HorarioIndisponivelException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class ConsultaRepositorioJsonTest {
    private static class RepositorioComFalha extends ConsultaRepositorioJson {
        private boolean falhar;

        RepositorioComFalha(String caminho, Repositorio<Paciente> pacientes, Repositorio<Medico> medicos) {
            super(caminho, pacientes, medicos);
        }

        @Override
        public void salvar() {
            if (falhar) {
                throw new java.io.UncheckedIOException(new java.io.IOException("Falha simulada"));
            }
            super.salvar();
        }
    }

    private static class RepositorioTeste<T> implements Repositorio<T> {
        private final List<T> itens = new ArrayList<>();
        public void cadastrar(T item) { itens.add(item); }
        public List<T> listar() { return new ArrayList<>(itens); }
        public T buscar(String id) {
            for (T item : itens) {
                if (item instanceof Paciente paciente && paciente.getCpf().equals(id)) {
                    return item;
                }
                if (item instanceof Medico medico && medico.getCrm().equals(id)) {
                    return item;
                }
            }
            return null;
        }
        public void atualizar(T item) { throw new UnsupportedOperationException(); }
        public void excluir(String id) { throw new UnsupportedOperationException(); }
    }

    @Test
    public void preservaConsultaAgendaEArquivoQuandoSalvarFalha() throws Exception {
        Path pasta = Files.createTempDirectory("falha-consultas");
        Path arquivo = pasta.resolve("consultas.json");
        try {
            RepositorioTeste<Paciente> pacientes = new RepositorioTeste<>();
            RepositorioTeste<Medico> medicos = new RepositorioTeste<>();
            Paciente paciente = new Paciente("Ana", "12345678900", "9999", LocalDate.of(2000, 1, 1));
            Medico medico = new Clinico("Bia", "98765432100", "9999", "123");
            pacientes.cadastrar(paciente);
            medicos.cadastrar(medico);
            RepositorioComFalha repo = new RepositorioComFalha(arquivo.toString(), pacientes, medicos);
            ConsultaService service = new ConsultaService(repo, new PacienteService(pacientes), new MedicoService(medicos), 20);
            LocalDateTime horario = LocalDateTime.now().plusDays(1);
            Consulta consulta = service.agendar(paciente.getCpf(), "123", horario, "Rotina");
            String original = Files.readString(arquivo);
            repo.falhar = true;
            assertThrows(java.io.UncheckedIOException.class, () -> service.remarcar(consulta.getId(), horario.plusHours(1)));
            assertThrows(java.io.UncheckedIOException.class, () -> service.cancelar(consulta.getId()));
            assertSame(consulta, service.buscarPorId(consulta.getId()));
            assertSame(consulta, medico.getAgenda().get(horario));
            assertTrue(medico.estaDisponivel(horario.plusHours(1)));
            assertEquals(original, Files.readString(arquivo));
            JsonObject objeto = JsonParser.parseString(original).getAsJsonArray().get(0).getAsJsonObject();
            objeto.remove("valorBase");
            Files.writeString(arquivo, "[" + objeto + "]");
            assertThrows(IllegalArgumentException.class, repo::carregar);
            assertSame(consulta, repo.buscar(consulta.getId()));
        } finally {
            Files.deleteIfExists(arquivo);
            Files.deleteIfExists(pasta);
        }
    }

    @Test
    public void remarcaCancelaEFiltraComPersistencia() throws Exception {
        Path pasta = Files.createTempDirectory("crud-consultas");
        Path arquivo = pasta.resolve("consultas.json");
        try {
            RepositorioTeste<Paciente> pacientes = new RepositorioTeste<>();
            RepositorioTeste<Medico> medicos = new RepositorioTeste<>();
            Paciente paciente = new Paciente("Ana", "12345678900", "9999", LocalDate.of(2000, 1, 1));
            Medico medico = new Clinico("Bia", "98765432100", "9999", "123");
            pacientes.cadastrar(paciente);
            medicos.cadastrar(medico);
            ConsultaRepositorioJson repo = new ConsultaRepositorioJson(arquivo.toString(), pacientes, medicos);
            ConsultaService service = new ConsultaService(repo, new PacienteService(pacientes), new MedicoService(medicos), 20);
            LocalDateTime horario = LocalDateTime.now().plusDays(1);
            Consulta primeira = service.agendar(paciente.getCpf(), "123", horario, "Rotina");
            Consulta segunda = service.agendar(paciente.getCpf(), "123", horario.plusHours(1), "Rotina");
            String antes = Files.readString(arquivo);
            assertThrows(HorarioIndisponivelException.class, () -> service.remarcar(primeira.getId(), segunda.getDataHora()));
            assertEquals(antes, Files.readString(arquivo));
            assertEquals(horario, service.buscarPorId(primeira.getId()).getDataHora());
            assertSame(primeira, medico.getAgenda().get(horario));
            LocalDateTime novoHorario = horario.plusDays(1);
            service.remarcar(primeira.getId(), novoHorario);
            assertTrue(medico.estaDisponivel(horario));
            assertEquals(primeira.getId(), medico.getAgenda().get(novoHorario).getId());
            repo.carregar();
            assertEquals(novoHorario, service.buscarPorId(primeira.getId()).getDataHora());
            assertEquals(2, service.listarPorPaciente("123.456.789-00").size());
            assertEquals(2, service.listarPorMedico("123").size());
            assertEquals(1, service.listarPorData(novoHorario.toLocalDate()).size());
            service.listar().clear();
            assertEquals(2, service.listar().size());
            service.cancelar(primeira.getId());
            assertTrue(medico.estaDisponivel(novoHorario));
            repo.carregar();
            assertEquals(1, service.listar().size());
            assertThrows(ConsultaNaoEncontradaException.class, () -> service.buscarPorId(primeira.getId()));
            assertThrows(ConsultaNaoEncontradaException.class, () -> service.cancelar("inexistente"));
        } finally {
            Files.deleteIfExists(arquivo);
            Files.deleteIfExists(pasta);
        }
    }

    @Test
    public void preservaDadosReferenciasEstrategiasECrud() throws Exception {
        Path pasta = Files.createTempDirectory("consultas-teste");
        Path arquivo = pasta.resolve("consultas.json");
        try {
            RepositorioTeste<Paciente> pacientes = new RepositorioTeste<>();
            RepositorioTeste<Medico> medicos = new RepositorioTeste<>();
            Paciente paciente = new Paciente("Ana", "12345678900", "9999", LocalDate.of(2000, 1, 1));
            Medico medico = new Clinico("Bia", "98765432100", "9999", "123");
            pacientes.cadastrar(paciente);
            medicos.cadastrar(medico);
            LocalDateTime horario = LocalDateTime.now().plusDays(1).withNano(123456789);
            Consulta consulta = new Consulta(paciente, medico, horario, "Rotina", 200, new PrecoConvenio(25));
            ConsultaRepositorioJson repo = new ConsultaRepositorioJson(arquivo.toString(), pacientes, medicos);
            repo.cadastrar(consulta);
            JsonObject json = JsonParser.parseString(Files.readString(arquivo)).getAsJsonArray().get(0).getAsJsonObject();
            assertFalse(json.has("paciente"));
            assertFalse(json.has("medico"));
            assertFalse(json.has("estrategia"));
            assertEquals(paciente.getCpf(), json.get("cpfPaciente").getAsString());
            assertEquals(medico.getCrm(), json.get("crmMedico").getAsString());
            repo = new ConsultaRepositorioJson(arquivo.toString(), pacientes, medicos);
            Consulta lida = repo.buscar(consulta.getId());
            assertSame(paciente, lida.getPaciente());
            assertSame(medico, lida.getMedico());
            assertEquals(horario, lida.getDataHora());
            assertEquals(150, lida.calcularValor(), 0.001);
            assertThrows(IllegalArgumentException.class, () -> new ConsultaRepositorioJson(arquivo.toString(), new RepositorioTeste<>(), medicos));
            lida.setEstrategia(new PrecoParticular());
            repo.atualizar(lida);
            repo.carregar();
            assertEquals(200, repo.buscar(lida.getId()).calcularValor(), 0.001);
            String conteudo = Files.readString(arquivo);
            LocalDateTime passado = LocalDateTime.of(2020, 1, 1, 12, 30, 10, 123456789);
            Files.writeString(arquivo, conteudo.replace(horario.toString(), passado.toString()));
            repo.carregar();
            assertEquals(passado, repo.buscar(lida.getId()).getDataHora());
            Files.writeString(arquivo, "{}");
            ConsultaRepositorioJson repositorio = repo;
            assertThrows(IllegalArgumentException.class, repositorio::carregar);
            assertEquals(1, repo.listar().size());
            repo.excluir(lida.getId());
            repo.carregar();
            assertTrue(repo.listar().isEmpty());
        } finally {
            Files.deleteIfExists(arquivo);
            Files.deleteIfExists(pasta);
        }
    }
}
