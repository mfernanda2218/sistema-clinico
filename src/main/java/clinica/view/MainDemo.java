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
			PacienteRepositorioJson pacientes = new PacienteRepositorioJson();
            MedicoRepositorioJson medicos = new MedicoRepositorioJson();
            PacienteService pacienteService = new PacienteService(pacientes);
			MedicoService medicoService = new MedicoService(medicos);
			ConsultaService consultaService = new ConsultaService(
			        new ConsultaRepositorioJson(pacientes, medicos), pacienteService, medicoService, 20);
			
			// Cadastrando paciente e médico
			Paciente p1 = new Paciente("Maria Fernanda", "52998224725", "88888888", LocalDate.of(2000, 5, 10), "Unimed");
			Medico m1 = new Cardiologista("Luan Vilela", "23456789092", "99999999", "CRM123");
			
			pacienteService.cadastrar(p1);
			medicoService.cadastrar(m1.getEspecialidade(), m1.getNome(), m1.getCpf(), m1.getTelefone(), m1.getCrm());
			System.out.println("Paciente e Médico cadastrados com sucesso!");
			
			// Agendar consultas e validar cobranças
			System.out.println("\n=== 2. AGENDANDO E COBRANDO ===");
			LocalDateTime horario1 = LocalDate.now().plusDays(1).atTime(14, 0);
			LocalDateTime horario2 = LocalDate.now().plusDays(1).atTime(15, 0);
			
			Consulta c1 = consultaService.agendar("52998224725", "CRM123", horario1, "Rotina");
			Consulta c2 = consultaService.agendar("52998224725", "CRM123", horario2, "Retorno");
			
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
			PacienteRepositorioJson novosPacientes = new PacienteRepositorioJson();
            MedicoRepositorioJson novosMedicos = new MedicoRepositorioJson();
            PacienteService novoPacienteService = new PacienteService(novosPacientes);
			MedicoService novoMedicoService = new MedicoService(novosMedicos);
			ConsultaService novoConsultaService = new ConsultaService(
			        new ConsultaRepositorioJson(novosPacientes, novosMedicos), novoPacienteService, novoMedicoService, 20);
			
			System.out.println("Paciente recarregado: " + novoPacienteService.buscarPorCpf("52998224725").getNome());
			System.out.println("Médico recarregado: " + novoMedicoService.buscarPorCrm("CRM123").getNome());
			System.out.println("Consultas carregadas do JSON: " + novoConsultaService.listar().size());	
			
		} catch (Exception e) {
			System.err.println("Erro durante o fluxo: " + e.getMessage());
			e.printStackTrace();
		}
	}
}