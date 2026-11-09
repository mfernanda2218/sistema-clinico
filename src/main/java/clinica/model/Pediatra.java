package clinica.model;

import clinica.exception.MedicoNaoEncontradoException;

public class Pediatra extends Medico {
	public Pediatra(String nome, String cpf, String telefone, String crm) throws MedicoNaoEncontradoException{
		super(nome, cpf, telefone, crm);
	}

	@Override
	public String getEspecialidade() {
		return "Pediatra";
	}

	@Override
	public double getValorConsulta() {
		return valorConsulta;
	}
}
