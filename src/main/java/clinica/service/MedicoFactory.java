package clinica.service;

import clinica.exception.MedicoNaoEncontradoException;
import clinica.model.Cardiologista;
import clinica.model.Clinico;
import clinica.model.Pediatra;
import clinica.model.Medico;


public class MedicoFactory {
	private MedicoFactory() {
		
	}
	
	public static Medico criar(String especialidade, String nome, String cpf, String telefone, String crm) throws MedicoNaoEncontradoException {
		if (especialidade == null || especialidade.isBlank()) {
			throw new IllegalArgumentException("Campo especialidade está vazio");
		}
		
		switch (especialidade.trim().toLowerCase()) {
			case "cardiologista":
			case "cardiologia":
				return new Cardiologista(nome, cpf, telefone, crm);
				
			case "pediatra":
			case "pediatria":
				return new Pediatra(nome, cpf, telefone, crm);
			
			case "clinico":
			case "clínico":
				return new Clinico(nome, cpf, telefone, crm);
				
			default:
				throw new IllegalArgumentException("Especialidade desconhecida: " + especialidade);
			
		
		}
	}
 
}
