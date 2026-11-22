package clinica.model;

public class Cardiologista extends Medico{
	public Cardiologista(String nome, String cpf, String telefone, String crm) {
		super(nome, cpf, telefone, crm);
	}

	@Override
	public String getEspecialidade() {
		return "Cardiologista";
	}

	@Override
	public double getValorConsulta() {
		return 300.0;
	}



}
