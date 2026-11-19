package clinica.repository;

import clinica.exception.ArquivoInvalidoException;
import clinica.model.Clinico;
import clinica.model.Medico;
import clinica.model.Paciente;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.Assert.*;

public class RepositorioJsonArquivoInvalidoTest {

    private Path pastaTemp;

    @Before
    public void setup() throws IOException {
        pastaTemp = Files.createTempDirectory("teste-json-invalido");
    }

    @After
    public void tearDown() throws IOException {
        if (pastaTemp != null && Files.exists(pastaTemp)) {
            try (var stream = Files.walk(pastaTemp)) {
                stream.sorted((a, b) -> b.compareTo(a))
                      .forEach(p -> {
                          try {
                              Files.deleteIfExists(p);
                          } catch (IOException ignored) {}
                      });
            }
        }
    }

    // ===== PacienteRepositorioJson =====

    @Test
    public void pacienteArquivoAusenteNaoGeraErro() throws ArquivoInvalidoException {
        Path arquivo = pastaTemp.resolve("pacientes_inexistente.json");
        PacienteRepositorioJson repo = new PacienteRepositorioJson(arquivo.toString());
        assertTrue(repo.listar().isEmpty());
    }

    @Test
    public void pacienteArquivoCorrompidoLancaArquivoInvalidoException() throws IOException {
        Path arquivo = pastaTemp.resolve("pacientes_corrompido.json");
        Files.writeString(arquivo, "{ conteudo malformado ");
        assertThrows(ArquivoInvalidoException.class, () -> new PacienteRepositorioJson(arquivo.toString()));
    }

    @Test
    public void pacienteArquivoNaoArrayLancaArquivoInvalidoException() throws IOException {
        Path arquivo = pastaTemp.resolve("pacientes_objeto.json");
        Files.writeString(arquivo, "{\"nome\": \"Maria\"}");
        assertThrows(ArquivoInvalidoException.class, () -> new PacienteRepositorioJson(arquivo.toString()));
    }

    // ===== MedicoRepositorioJson =====

    @Test
    public void medicoArquivoAusenteNaoGeraErro() throws ArquivoInvalidoException {
        Path arquivo = pastaTemp.resolve("medicos_inexistente.json");
        MedicoRepositorioJson repo = new MedicoRepositorioJson(arquivo.toString());
        assertTrue(repo.listar().isEmpty());
    }

    @Test
    public void medicoArquivoCorrompidoLancaArquivoInvalidoException() throws IOException {
        Path arquivo = pastaTemp.resolve("medicos_corrompido.json");
        Files.writeString(arquivo, "[ { crm: invalido sem fechar ");
        assertThrows(ArquivoInvalidoException.class, () -> new MedicoRepositorioJson(arquivo.toString()));
    }

    @Test
    public void medicoArquivoNaoArrayLancaArquivoInvalidoException() throws IOException {
        Path arquivo = pastaTemp.resolve("medicos_objeto.json");
        Files.writeString(arquivo, "{\"crm\": \"123\"}");
        assertThrows(ArquivoInvalidoException.class, () -> new MedicoRepositorioJson(arquivo.toString()));
    }

    // ===== ConsultaRepositorioJson =====

    @Test
    public void consultaArquivoAusenteNaoGeraErro() throws Exception {
        Path arquivo = pastaTemp.resolve("consultas_inexistente.json");
        PacienteRepositorioJson repoPacientes = new PacienteRepositorioJson(pastaTemp.resolve("p.json").toString());
        MedicoRepositorioJson repoMedicos = new MedicoRepositorioJson(pastaTemp.resolve("m.json").toString());

        ConsultaRepositorioJson repoConsultas = new ConsultaRepositorioJson(arquivo.toString(), repoPacientes, repoMedicos);
        assertTrue(repoConsultas.listar().isEmpty());
    }

    @Test
    public void consultaArquivoCorrompidoLancaArquivoInvalidoException() throws Exception {
        Path arquivo = pastaTemp.resolve("consultas_corrompido.json");
        Files.writeString(arquivo, "nao e json");

        PacienteRepositorioJson repoPacientes = new PacienteRepositorioJson(pastaTemp.resolve("p2.json").toString());
        MedicoRepositorioJson repoMedicos = new MedicoRepositorioJson(pastaTemp.resolve("m2.json").toString());

        assertThrows(ArquivoInvalidoException.class, () -> new ConsultaRepositorioJson(arquivo.toString(), repoPacientes, repoMedicos));
    }

    @Test
    public void consultaArquivoNaoArrayLancaArquivoInvalidoException() throws Exception {
        Path arquivo = pastaTemp.resolve("consultas_objeto.json");
        Files.writeString(arquivo, "{}");

        PacienteRepositorioJson repoPacientes = new PacienteRepositorioJson(pastaTemp.resolve("p3.json").toString());
        MedicoRepositorioJson repoMedicos = new MedicoRepositorioJson(pastaTemp.resolve("m3.json").toString());

        assertThrows(ArquivoInvalidoException.class, () -> new ConsultaRepositorioJson(arquivo.toString(), repoPacientes, repoMedicos));
    }
}

