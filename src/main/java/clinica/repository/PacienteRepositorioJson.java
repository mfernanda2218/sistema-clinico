package clinica.repository;

import clinica.interfaces.Repositorio;
import clinica.model.Paciente;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacienteRepositorioJson implements Repositorio<Paciente> {

    private final String caminhoArquivo;
    private final List<Paciente> pacientes;
    private final Map<String, Paciente> pacientePorCpf;
    private final Gson gson;

    public PacienteRepositorioJson() {
        this("dados/pacientes.json");
    }

    public PacienteRepositorioJson(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
        this.pacientes = new ArrayList<>();
        this.pacientePorCpf = new HashMap<>();
        this.gson = criarGson();
        carregar();
    }

    private Gson criarGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                        new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) ->
                        LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
                .create();
    }

    @Override
    public void cadastrar(Paciente paciente) {
        if (paciente == null || paciente.getCpf() == null) {
            throw new IllegalArgumentException("Paciente ou CPF não pode ser nulo.");
        }

        String cpf = paciente.getCpf();
        if (pacientePorCpf.containsKey(cpf)) {
            throw new IllegalArgumentException("Paciente com CPF já cadastrado.");
        }

        pacientes.add(paciente);
        pacientePorCpf.put(cpf, paciente);
        salvar();
    }

    @Override
    public List<Paciente> listar() {
        return new ArrayList<>(pacientes);
    }

    @Override
    public Paciente buscar(String cpf) {
        if (cpf == null) {
            return null;
        }
        String cpfLimpo = cpf.replaceAll("\\D", "");
        return pacientePorCpf.get(cpfLimpo.isEmpty() ? cpf : cpfLimpo);
    }

    @Override
    public void atualizar(Paciente paciente) {
        if (paciente == null || paciente.getCpf() == null) {
            throw new IllegalArgumentException("Paciente ou CPF não pode ser nulo.");
        }

        String cpf = paciente.getCpf();
        Paciente antigo = pacientePorCpf.get(cpf);

        if (antigo == null) {
            throw new IllegalArgumentException("Paciente não encontrado.");
        }

        int indice = pacientes.indexOf(antigo);
        pacientes.set(indice, paciente);
        pacientePorCpf.put(cpf, paciente);
        salvar();
    }

    @Override
    public void excluir(String cpf) {
        if (cpf == null) {
            throw new IllegalArgumentException("CPF não pode ser nulo.");
        }

        String cpfLimpo = cpf.replaceAll("\\D", "");
        Paciente paciente = pacientePorCpf.remove(cpfLimpo);

        if (paciente == null) {
            throw new IllegalArgumentException("Paciente não encontrado.");
        }

        pacientes.remove(paciente);
        salvar();
    }

    @Override
    public void carregar() {
        File arquivo = new File(caminhoArquivo);
        if (!arquivo.exists()) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(arquivo.toPath(), StandardCharsets.UTF_8)) {
            Type tipoLista = new TypeToken<ArrayList<Paciente>>() {}.getType();
            List<Paciente> carregados = gson.fromJson(reader, tipoLista);

            pacientes.clear();
            pacientePorCpf.clear();

            if (carregados != null) {
                for (Paciente p : carregados) {
                    pacientes.add(p);
                    pacientePorCpf.put(p.getCpf(), p);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler arquivo: " + caminhoArquivo, e);
        }
    }

    @Override
    public void salvar() {
        File arquivo = new File(caminhoArquivo);
        File pasta = arquivo.getParentFile();

        if (pasta != null && !pasta.exists()) {
            pasta.mkdirs();
        }

        try (Writer writer = Files.newBufferedWriter(arquivo.toPath(), StandardCharsets.UTF_8)) {
            gson.toJson(pacientes, writer);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gravar arquivo: " + caminhoArquivo, e);
        }
    }
}
