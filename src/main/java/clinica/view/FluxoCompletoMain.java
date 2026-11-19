package clinica.view;

import clinica.interfaces.Repositorio;
import clinica.model.*;
import clinica.service.*;
import clinica.repository.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

public class FluxoCompletoMain {
	public static void main(String[] args) {
		try {
			//instanciando os repositorios e services
			System.out.println("=== 1. INICIANDO SISTEMA E CADASTRANDO ===");
			PacienteService pacienteService = new PacienteService((new PacienteRepositorioJson()));
			MedicoService medicoService = new MedicoService(new MedicoRepositorioJson());
			ConsultaService consultaService = new ConsultaService(
			        new ConsultaRepositorioJson(), pacienteService, medicoService, 0.20);
			//cadastrando medico e paciente
			Paciente p1 = new Paciente("Maria Fernanda", "12345678900", "88888888", LocalDate.of(2000, 5, 10), "Unimed");
			Medico m1 = new Cardiologista("Luan Vilela", "10000000000", "99999999", "CRM123");
			
			pacienteService.cadastrar(p1);
			//modificar o construtor em medicoservice
			medicoService.cadastrar(m1);
			
			//agendar consultas e validar cobranças
			System.out.println("=== 2. AGENDANDO E COBRANDO ===");
			LocalDateTime horario1 = LocalDateTime.of(2026, 11, 20, 14, 0);
			LocalDateTime horario2 = LocalDateTime.of(2026, 11, 20, 15, 0);
			
			Consulta c1 = consultaService.agendar("12345678900", "CRM123", horario1, "Rotina");
			Consulta c2 = consultaService.agendar("12345678900", "CRM123", horario2, "Retorno");
			
			System.out.println("Consulta agendada! Valor cobrado: R$ " + c1.getValorBase());
			
			//cancelamento de consulta
			System.out.println("=== 3. CANCELANDO A SEGUNDA CONSULTA ===");
			consultaService.cancelar(c2.getId());
			System.out.println("Consultas ativas antes de reiniciar: " + consultaService.listar().size());
			
			//reiniciar e recarregar
			System.out.println("\n=== 4. SIMULANDO REINÍCIO DO PROGRAMA ===");
			//criando novas instrancias para forçar a leitura do arquivo JSON
			PacienteService novoPacienteService = new PacienteService(new PacienteRepositorioJson());
			MedicoService novoMedicoService = new MedicoService(new MedicoRepositorioJson());
			ConsultaService novoConsultaService = new ConsultaService(new ConsultaRepositorioJson(), novoPacienteService, novoMedicoService, 0.20);
			
			System.out.println("Paciente recarregado: " + novoPacienteService.buscarPorCpf("12345678900").getNome());
			System.out.println("Medico carregado: " + novoMedicoService.buscarPorCrm("CRM123").getNome());
			System.out.println("Consultas carregadas: " + novoConsultaService.listar().size());	
		} catch(Exception e) {
			System.err.println("Erro durante o fluxo: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
