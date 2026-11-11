package clinica.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
import java.util.Optional;

public class MedicoRepositorioJson implements Repositorio<Medico> {
	
	 private static final String CAMINHO_ARQUIVO = "dados/medicos.json";
	 private final List<Medico> medicos;
	 private final Gson gson;
	 
	 public MedicoRepositorioJson() {
		 this.medicos = new ArrayList<>();
		 this.gson = new GsonBuilder().setPrettyPrinting().create();
		 carregar();
	 }
	 
	@Override
	public void cadastrar(Medico entidade) {
		if (entidade == null) {
			throw new IllegalArgumentException("O campo médico não pode ser vazio.");
		}
		if (buscar(entidade.getCpf()) != null) {
			throw new IllegalArgumentException("O médico com esse cpf já está cadastrado.");
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
			throw new IllegalArgumentException("Médico não encontrado para atualização.");
		}
		int index = medicos.indexOf(existente);
		medicos.set(index, existente);
		salvar();		
	}

	@Override
	public void excluir(String identificador) {
		Medico existente = buscar(identificador);
		if(identificador == null) {
			throw new IllegalArgumentException("Médico não encontrado para exclusão");
		}
		medicos.remove(existente);
		salvar();
		
	}

	@Override
	public void carregar() {
		medicos.clear();
		File arquivo = new File(CAMINHO_ARQUIVO);
		
		if (!arquivo.exists()) {
			return;
		}
		
		try (FileReader reader = new FileReader(arquivo)) {
			JsonElement raiz = JsonParser.parseReader(reader);
			
			if(raiz == null || !raiz.isJsonArray()) {
				return;
			}
			
			JsonArray array = raiz.getAsJsonArray();
			for (JsonElement elemento: array) {
				if (elemento.isJsonObject()) {
					JsonObject obj = elemento.getAsJsonObject();
					
					String especialidade = getCampoString(obj, "especialidade");
					String nome = getCampoString(obj, "nome");
					String cpf = getCampoString(obj, "cpf");
					String telefone = getCampoString(obj, "telefone");
					String crm = getCampoString(obj, "crm");
					
					Medico medico = MedicoFactory.criar(especialidade, nome, cpf, telefone, crm);
					medicos.add(medico);
				}
			}
		} catch (IOException | MedicoNaoEncontradoException e) {
			System.err.println("Erro ao carregar o arquivo " + e.getMessage());
		}
	}

	private String getCampoString(JsonObject obj, String campo) {
		return (obj.has(campo) && !obj.get(campo).isJsonNull()) ? obj.get(campo).getAsString() : "";
    }
	
	
	@Override
    public void salvar() {
        File arquivo = new File(CAMINHO_ARQUIVO);
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
