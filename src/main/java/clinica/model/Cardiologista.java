package clinica.model;

import clinica.exception.MedicoNaoEncontradoException;

public class Cardiologista extends Medico{
	public Cardiologista(String nome, String cpf, String telefone, String crm) throws MedicoNaoEncontradoException {
		super(nome, cpf, telefone, crm);
	}

	@Override
	public String getEspecialidade() {
		return "Cardiologista";
	}

	@Override
	public double getValorConsulta() {
		return getValorConsulta();
	}
	
	

}
