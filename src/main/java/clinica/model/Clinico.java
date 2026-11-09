package clinica.model;

import clinica.exception.MedicoNaoEncontradoException;

public class Clinico extends Medico {
	public Clinico (String nome, String cpf, String telefone, String crm) throws MedicoNaoEncontradoException {
		super(nome, cpf, telefone, crm);
	}

	@Override
	public String getEspecialidade() {
		return "Clínico";
	}

	@Override
	public double getValorConsulta() {
		return valorConsulta; 
	}
}
