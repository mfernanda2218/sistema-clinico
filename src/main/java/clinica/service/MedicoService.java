package clinica.service;

import clinica.exception.MedicoNaoEncontradoException;
import clinica.interfaces.Repositorio;
import clinica.model.Consulta;
import clinica.model.Medico;
import clinica.repository.MedicoRepositorioJson;
import clinica.service.MedicoFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class MedicoService {
	
	private final Repositorio<Medico> repositorio;
	private final Repositorio<Consulta> repositorioConsulta;

	public MedicoService() {
		this(new MedicoRepositorioJson(), null);
	}

	public MedicoService(Repositorio<Medico> repositorio) {
        this(repositorio, null);
    }
	
	public MedicoService(Repositorio<Medico> repositorio, Repositorio<Consulta> repositorioConsulta) {
        if (repositorio == null) {
            throw new IllegalArgumentException("O repositório de médicos não pode ser nulo.");
        }
        this.repositorio = repositorio;
        this.repositorioConsulta = repositorioConsulta;
    }
	
	public Medico cadastrar(String especialidade, String nome, String cpf, String telefone, String crm) throws MedicoNaoEncontradoException {
        if (crm == null || crm.isBlank()) {
            throw new IllegalArgumentException("O campo CRM não pode ser vazio.");
        }

        
        if (repositorio.buscar(crm.trim()) != null) {
            throw new IllegalArgumentException("Já existe um médico cadastrado com o CRM: " + crm);
        }

       
        Medico novoMedico = MedicoFactory.criar(especialidade, nome, cpf, telefone, crm);
        repositorio.cadastrar(novoMedico);
        return novoMedico;
    }
	
	 public List<Medico> listar() {
	        return repositorio.listar();
	    }
	 
	 public Medico buscarPorCrm(String crm) throws MedicoNaoEncontradoException {
	        if (crm == null || crm.isBlank()) {
	            throw new IllegalArgumentException("O campo CRM não pode ser vazio.");
	        }

	        Medico medico = repositorio.buscar(crm.trim());
	        if (medico == null) {
	            throw new MedicoNaoEncontradoException("Médico não encontrado para o CRM: " + crm);
	        }

	        return medico;
	    }
	 
	 public List<Medico> buscarPorEspecialidade(String especialidade) {
	        if (especialidade == null || especialidade.isBlank()) {
	            throw new IllegalArgumentException("O campo especialidade não pode ser vazio.");
	        }

	        String termo = especialidade.trim().toLowerCase();

	        return repositorio.listar().stream()
	                .filter(m -> m.getEspecialidade() != null && m.getEspecialidade().trim().toLowerCase().contains(termo))
	                .collect(Collectors.toList());
	    } 
	 
	 public void atualizar(Medico medico) throws MedicoNaoEncontradoException {
	        if (medico == null || medico.getCrm() == null || medico.getCrm().isBlank()) {
	            throw new IllegalArgumentException("Médico ou CRM não podem ser nulos.");
	        }

	        if (repositorio.buscar(medico.getCrm()) == null) {
	            throw new MedicoNaoEncontradoException("Médico não encontrado para o CRM: " + medico.getCrm());
	        }

	        repositorio.atualizar(medico);
	    }
	 
	 public void excluir(String crm) throws MedicoNaoEncontradoException {
	        if (crm == null || crm.isBlank()) {
	            throw new IllegalArgumentException("CRM não pode ser vazio.");
	        }

	        Medico medico = buscarPorCrm(crm);

	        if (possuiConsultasAgendadas(medico.getCrm())) {
	            throw new IllegalStateException("Não é possível excluir o médico pois ele possui consultas futuras agendadas.");
	        }

	        repositorio.excluir(medico.getCrm());
	    }

	    private boolean possuiConsultasAgendadas(String crm) {
	        if (repositorioConsulta == null) {
	            return false;
	        }

	        LocalDateTime agora = LocalDateTime.now();
	        return repositorioConsulta.listar().stream()
	                .anyMatch(c -> crm.equalsIgnoreCase(c.getCrmMedico())
	                        && c.getDataHora() != null
	                        && c.getDataHora().isAfter(agora));
	    }
	}
	