package clinica.model;

public class Pediatra extends Medico {
	public Pediatra(String nome, String cpf, String telefone, String crm){
		super(nome, cpf, telefone, crm);
	}

	@Override
	public String getEspecialidade() {
		return "Pediatra";
	}

	@Override
	public double getValorConsulta() {
		return 200.0;
	}
}
