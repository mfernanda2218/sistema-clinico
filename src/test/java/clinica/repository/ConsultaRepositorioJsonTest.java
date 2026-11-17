package clinica.repository;

import clinica.interfaces.Repositorio;
import clinica.model.*;
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
    private static class RepositorioTeste<T> implements Repositorio<T> {
        private final List<T> itens = new ArrayList<>();
        public void cadastrar(T item) { itens.add(item); }
        public List<T> listar() { return new ArrayList<>(itens); }
        public T buscar(String id) {
            for (T item : itens) {
                if (item instanceof Paciente paciente && paciente.getCpf().equals(id)) {
                    return item;
                }
            }
            return null;
        }
        public void atualizar(T item) { throw new UnsupportedOperationException(); }
        public void excluir(String id) { throw new UnsupportedOperationException(); }
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
