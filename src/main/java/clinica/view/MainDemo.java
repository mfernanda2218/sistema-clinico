package clinica.view;

import clinica.model.*;
import clinica.repository.*;
import clinica.service.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MainDemo {
	public static void main(String[] args) {
		try {
			// Instanciando os repositórios e services
			System.out.println("=== 1. INICIANDO SISTEMA E CADASTRANDO ===");
			PacienteService pacienteService = new PacienteService(new PacienteRepositorioJson());
			MedicoService medicoService = new MedicoService(new MedicoRepositorioJson());
			ConsultaService consultaService = new ConsultaService(
			        new ConsultaRepositorioJson(), pacienteService, medicoService, 0.20);
			
			// Cadastrando paciente e médico
			Paciente p1 = new Paciente("Maria Fernanda", "12345678900", "88888888", LocalDate.of(2000, 5, 10), "Unimed");
			Medico m1 = new Cardiologista("Luan Vilela", "10000000000", "99999999", "CRM123");
			
			pacienteService.cadastrar(p1);
			medicoService.cadastrar(m1);
			System.out.println("Paciente e Médico cadastrados com sucesso!");
			
			// Agendar consultas e validar cobranças
			System.out.println("\n=== 2. AGENDANDO E COBRANDO ===");
			LocalDateTime horario1 = LocalDateTime.of(2026, 11, 20, 14, 0);
			LocalDateTime horario2 = LocalDateTime.of(2026, 11, 20, 15, 0);
			
			Consulta c1 = consultaService.agendar("12345678900", "CRM123", horario1, "Rotina");
			Consulta c2 = consultaService.agendar("12345678900", "CRM123", horario2, "Retorno");
			
			// Se houver um método getValorFinal() ou calcularValor() na Consulta, use-o aqui para mostrar o desconto!
			System.out.println("Consulta 1 agendada! Valor base: R$ " + c1.getValorBase());
			System.out.println("Consulta 2 agendada! ID: " + c2.getId());
			
			// Cancelamento de consulta
			System.out.println("\n=== 3. CANCELANDO A SEGUNDA CONSULTA ===");
			consultaService.cancelar(c2.getId());
			System.out.println("Consultas ativas antes de reiniciar: " + consultaService.listar().size());
			
			// Reiniciar e recarregar
			System.out.println("\n=== 4. SIMULANDO REINÍCIO DO PROGRAMA ===");
			// Criando novas instâncias para forçar a leitura do arquivo JSON
			PacienteService novoPacienteService = new PacienteService(new PacienteRepositorioJson());
			MedicoService novoMedicoService = new MedicoService(new MedicoRepositorioJson());
			ConsultaService novoConsultaService = new ConsultaService(
			        new ConsultaRepositorioJson(), novoPacienteService, novoMedicoService, 0.20);
			
			System.out.println("Paciente recarregado: " + novoPacienteService.buscarPorCpf("12345678900").getNome());
			System.out.println("Médico recarregado: " + novoMedicoService.buscarPorCrm("CRM123").getNome());
			System.out.println("Consultas carregadas do JSON: " + novoConsultaService.listar().size());	
			
		} catch (Exception e) {
			System.err.println("Erro durante o fluxo: " + e.getMessage());
			e.printStackTrace();
		}
	}
}