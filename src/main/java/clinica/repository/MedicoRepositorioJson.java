package clinica.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import clinica.exception.ArquivoInvalidoException;
import clinica.exception.MedicoNaoEncontradoException;
import clinica.interfaces.Repositorio;
import clinica.model.Medico;
import clinica.service.MedicoFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MedicoRepositorioJson implements Repositorio<Medico> {
	
	private static final String CAMINHO_PADRAO = "dados/medicos.json";
	private final String caminhoArquivo;
	private final List<Medico> medicos;
	private final Gson gson;
	 
	public MedicoRepositorioJson() throws ArquivoInvalidoException {
		this(CAMINHO_PADRAO);
	}

	public MedicoRepositorioJson(String caminhoArquivo) throws ArquivoInvalidoException {
		this.caminhoArquivo = (caminhoArquivo == null || caminhoArquivo.isBlank()) ? CAMINHO_PADRAO : caminhoArquivo;
		this.medicos = new ArrayList<>();
		this.gson = new GsonBuilder().setPrettyPrinting().create();
		carregar();
	}
	 
	@Override
	public void cadastrar(Medico entidade) {
		if (entidade == null) {
			throw new IllegalArgumentException("O campo médico não pode ser vazio.");
		}
		if (buscar(entidade.getCpf()) != null || buscar(entidade.getCrm()) != null) {
			throw new IllegalArgumentException("O médico com esse CPF ou CRM já está cadastrado.");
		}
		medicos.add(entidade);
		salvar();
	}

	@Override
	public List<Medico> listar() {
		return new ArrayList<>(medicos);
	}

	@Override
	public Medico buscar(String identificador) {
		if (identificador == null || identificador.isBlank()) {
			return null;
		}
		return medicos.stream()
				.filter(m -> identificador.equalsIgnoreCase(m.getCpf()) || identificador.equalsIgnoreCase(m.getCrm()))
                .findFirst()
                .orElse(null);
	}

	@Override
	public void atualizar(Medico entidade) {
		if (entidade == null) {
			throw new IllegalArgumentException("O campo médico não pode ser nulo.");
		} 
		Medico existente = buscar(entidade.getCpf());
		if (existente == null) {
			existente = buscar(entidade.getCrm());
		}
		if (existente == null) {
			throw new IllegalArgumentException("Médico não encontrado para atualização.");
		}
		int index = medicos.indexOf(existente);
		medicos.set(index, entidade);
		salvar();		
	}

	@Override
	public void excluir(String identificador) {
		if (identificador == null || identificador.isBlank()) {
			throw new IllegalArgumentException("Identificador não pode ser nulo ou vazio.");
		}
		Medico existente = buscar(identificador);
		if (existente == null) {
			throw new IllegalArgumentException("Médico não encontrado para exclusão.");
		}
		medicos.remove(existente);
		salvar();
	}

	@Override
	public void carregar() throws ArquivoInvalidoException {
		File arquivo = new File(caminhoArquivo);
		
		if (!arquivo.exists()) {
			return;
		}
		
		List<Medico> carregados = new ArrayList<>();
		try (FileReader reader = new FileReader(arquivo)) {
			JsonElement raiz = JsonParser.parseReader(reader);
			
			if (raiz == null || !raiz.isJsonArray()) {
				throw new ArquivoInvalidoException("O arquivo de médicos deve conter uma lista JSON: " + caminhoArquivo);
			}
			
			JsonArray array = raiz.getAsJsonArray();
			for (JsonElement elemento : array) {
				if (!elemento.isJsonObject()) {
					throw new ArquivoInvalidoException("Elemento inválido na lista de médicos: " + elemento);
				}
				JsonObject obj = elemento.getAsJsonObject();
				
				String especialidade = getCampoString(obj, "especialidade");
				String nome = getCampoString(obj, "nome");
				String cpf = getCampoString(obj, "cpf");
				String telefone = getCampoString(obj, "telefone");
				String crm = getCampoString(obj, "crm");
				
				if (especialidade.isBlank() || nome.isBlank() || crm.isBlank()) {
					throw new ArquivoInvalidoException("Médico com campos obrigatórios ausentes no arquivo: " + obj);
				}

				for (Medico m : carregados) {
					if (m.getCrm().equalsIgnoreCase(crm.trim())) {
						throw new ArquivoInvalidoException("Arquivo de médicos contém CRM duplicado: " + crm);
					}
					if (!cpf.isBlank() && m.getCpf().equalsIgnoreCase(cpf.trim())) {
						throw new ArquivoInvalidoException("Arquivo de médicos contém CPF duplicado: " + cpf);
					}
				}
				
				Medico medico = MedicoFactory.criar(especialidade, nome, cpf, telefone, crm);
				carregados.add(medico);
			}
		} catch (JsonParseException | IOException | IllegalArgumentException | MedicoNaoEncontradoException e) {
			throw new ArquivoInvalidoException("Arquivo de médicos corrompido ou inválido: " + caminhoArquivo, e);
		}

		medicos.clear();
		medicos.addAll(carregados);
	}

	private String getCampoString(JsonObject obj, String campo) {
		return (obj.has(campo) && !obj.get(campo).isJsonNull()) ? obj.get(campo).getAsString() : "";
    }
	
	@Override
    public void salvar() {
        File arquivo = new File(caminhoArquivo);
        File diretorioPai = arquivo.getParentFile();

        if (diretorioPai != null && !diretorioPai.exists()) {
            diretorioPai.mkdirs();
        }

        try (FileWriter writer = new FileWriter(arquivo)) {
            gson.toJson(medicos, writer);
        } catch (IOException e) {
            System.err.println("Erro ao salvar médicos no arquivo: " + e.getMessage());
        }
    }
}
