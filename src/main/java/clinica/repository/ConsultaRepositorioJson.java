package clinica.repository;

import clinica.exception.ArquivoInvalidoException;
import clinica.interfaces.Repositorio;
import clinica.model.*;
import com.google.gson.*;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConsultaRepositorioJson implements Repositorio<Consulta> {
    private final Path arquivo;
    private final Repositorio<Paciente> pacientes;
    private final Repositorio<Medico> medicos;
    private final List<Consulta> consultas = new ArrayList<>();
    private final Gson gson;

    public ConsultaRepositorioJson(Repositorio<Paciente> pacientes, Repositorio<Medico> medicos) throws ArquivoInvalidoException {
        this("dados/consultas.json", pacientes, medicos);
    }

    public ConsultaRepositorioJson(String caminho, Repositorio<Paciente> pacientes, Repositorio<Medico> medicos) throws ArquivoInvalidoException {
        if (caminho == null || caminho.isBlank() || pacientes == null || medicos == null) {
            throw new IllegalArgumentException("Informe o arquivo e os repositórios de pacientes e médicos.");
        }
        this.arquivo = Path.of(caminho);
        this.pacientes = pacientes;
        this.medicos = medicos;
        this.gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter().nullSafe()).create();
        carregar();
    }

    @Override
    public Consulta buscar(String id) {
        for (Consulta consulta : consultas) {
            if (consulta.getId().equals(id)) {
                return consulta;
            }
        }
        return null;
    }

    @Override
    public List<Consulta> listar() {
        return new ArrayList<>(consultas);
    }

    @Override
    public void cadastrar(Consulta consulta) {
        validar(consulta);
        if (buscar(consulta.getId()) != null) {
            throw new IllegalArgumentException("Consulta já cadastrada.");
        }
        consultas.add(consulta);
        try {
            salvar();
        } catch (RuntimeException e) {
            consultas.remove(consulta);
            throw e;
        }
    }

    @Override
    public void atualizar(Consulta consulta) {
        validar(consulta);
        Consulta antiga = buscar(consulta.getId());
        if (antiga == null) {
            throw new IllegalArgumentException("Consulta não encontrada.");
        }
        int indice = consultas.indexOf(antiga);
        consultas.set(indice, consulta);
        try {
            salvar();
        } catch (RuntimeException e) {
            consultas.set(indice, antiga);
            throw e;
        }
    }

    @Override
    public void excluir(String id) {
        Consulta consulta = buscar(id);
        if (consulta == null) {
            throw new IllegalArgumentException("Consulta não encontrada.");
        }
        int indice = consultas.indexOf(consulta);
        consultas.remove(indice);
        try {
            salvar();
        } catch (RuntimeException e) {
            consultas.add(indice, consulta);
            throw e;
        }
    }

    private void validar(Consulta consulta) {
        if (consulta == null || consulta.getId() == null || consulta.getId().isBlank()
                || consulta.getDataHora() == null || consulta.getPaciente() == null || consulta.getMedico() == null) {
            throw new IllegalArgumentException("Consulta incompleta.");
        }
        consulta.setTipo(consulta.getTipo());
        consulta.setValorBase(consulta.getValorBase());
        if (!(consulta.getEstrategia() instanceof PrecoParticular)
                && !(consulta.getEstrategia() instanceof PrecoConvenio)) {
            throw new IllegalArgumentException("Estratégia de preço não suportada pelo arquivo.");
        }
        if (pacientes.buscar(consulta.getCpfPaciente()) == null || encontrarMedico(consulta.getCrmMedico()) == null) {
            throw new IllegalArgumentException("Paciente ou médico da consulta não cadastrado.");
        }
    }

    private Medico encontrarMedico(String crm) {
        for (Medico medico : medicos.listar()) {
            if (medico.getCrm().equals(crm)) {
                return medico;
            }
        }
        return null;
    }

    @Override
    public void carregar() throws ArquivoInvalidoException {
        if (!Files.exists(arquivo)) {
            return;
        }
        List<Consulta> carregadas = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            JsonElement raiz = JsonParser.parseReader(reader);
            if (!raiz.isJsonArray()) {
                throw new ArquivoInvalidoException("O arquivo deve conter uma lista de consultas: " + arquivo);
            }
            for (JsonElement elemento : raiz.getAsJsonArray()) {
                if (!elemento.isJsonObject()) {
                    throw new ArquivoInvalidoException("Elemento inválido na lista de consultas: " + elemento);
                }
                JsonObject objeto = elemento.getAsJsonObject();
                if (!objeto.has("valorBase") || objeto.get("valorBase").isJsonNull()) {
                    throw new ArquivoInvalidoException("Valor da consulta ausente no arquivo: " + arquivo);
                }
                Consulta consulta = gson.fromJson(objeto, Consulta.class);
                consulta.setPaciente(pacientes.buscar(consulta.getCpfPaciente()));
                consulta.setMedico(encontrarMedico(consulta.getCrmMedico()));
                String forma = consulta.getFormaCobranca();
                if ("Particular".equals(forma)) {
                    consulta.setEstrategia(new PrecoParticular());
                } else if ("Convênio".equals(forma)) {
                    if (!objeto.has("percentualDesconto") || objeto.get("percentualDesconto").isJsonNull()) {
                        throw new ArquivoInvalidoException("Percentual de desconto ausente no arquivo para convênio: " + arquivo);
                    }
                    consulta.setEstrategia(new PrecoConvenio(objeto.get("percentualDesconto").getAsDouble()));
                } else {
                    throw new ArquivoInvalidoException("Forma de cobrança inválida no arquivo: " + forma);
                }
                validar(consulta);
                for (Consulta anterior : carregadas) {
                    if (anterior.getId().equals(consulta.getId())) {
                        throw new ArquivoInvalidoException("ID de consulta repetido no arquivo: " + consulta.getId());
                    }
                }
                carregadas.add(consulta);
            }
        } catch (JsonParseException | IOException | IllegalArgumentException e) {
            throw new ArquivoInvalidoException("Arquivo de consultas corrompido ou inválido: " + arquivo, e);
        }
        consultas.clear();
        consultas.addAll(carregadas);
    }

    @Override
    public void salvar() {
        JsonArray lista = new JsonArray();
        for (Consulta consulta : consultas) {
            validar(consulta);
            JsonObject objeto = gson.toJsonTree(consulta).getAsJsonObject();
            if (consulta.getEstrategia() instanceof PrecoConvenio convenio) {
                objeto.addProperty("percentualDesconto", convenio.getPercentualDesconto());
            }
            lista.add(objeto);
        }
        Path temporario = null;
        try {
            if (arquivo.getParent() != null) {
                Files.createDirectories(arquivo.getParent());
            }
            temporario = Files.createTempFile(arquivo.toAbsolutePath().getParent(), "consultas-", ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporario, StandardCharsets.UTF_8)) {
                gson.toJson(lista, writer);
            }
            try {
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gravar consultas: " + arquivo, e);
        } finally {
            if (temporario != null) {
                try {
                    Files.deleteIfExists(temporario);
                } catch (IOException e) {
                    temporario.toFile().deleteOnExit();
                }
            }
        }
    }
}
