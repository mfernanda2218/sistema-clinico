package clinica.service;

import clinica.exception.*;
import clinica.interfaces.Repositorio;
import clinica.model.*;
import org.junit.Before;
import org.junit.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class ConsultaServiceTest {
    private static class Memoria<T> implements Repositorio<T> {
        private final List<T> itens = new ArrayList<>();
        private boolean falhar;
        public void cadastrar(T item) {
            if (falhar) { throw new IllegalStateException("Falha ao salvar"); }
            itens.add(item);
        }
        public List<T> listar() { return new ArrayList<>(itens); }
        public T buscar(String id) {
            for (T item : itens) {
                if (item instanceof Paciente p && p.getCpf().equals(id)) { return item; }
                if (item instanceof Medico m && m.getCrm().equals(id)) { return item; }
            }
            return null;
        }
        public void atualizar(T item) { throw new UnsupportedOperationException(); }
        public void excluir(String id) { throw new UnsupportedOperationException(); }
    }

    private Memoria<Consulta> consultas;
    private Memoria<Medico> medicos;
    private Memoria<Paciente> pacientes;
    private ConsultaService service;
    private Paciente paciente;
    private Medico medico;
    private LocalDateTime horario;

    @Before
    public void preparar() throws Exception {
        consultas = new Memoria<>();
        medicos = new Memoria<>();
        pacientes = new Memoria<>();
        paciente = new Paciente("Ana", "12345678900", "9999", LocalDate.of(2000, 1, 1), "Unimed");
        medico = new Clinico("Bia", "98765432100", "9999", "123");
        pacientes.cadastrar(paciente);
        medicos.cadastrar(medico);
        service = new ConsultaService(consultas, new PacienteService(pacientes), new MedicoService(medicos), 20);
        horario = LocalDateTime.now().plusDays(1);
    }

    @Test
    public void agendaComObjetosERejeitaConflito() throws Exception {
        Consulta consulta = service.agendar(paciente.getCpf(), medico.getCrm(), horario, "Rotina");
        assertSame(paciente, consulta.getPaciente());
        assertSame(medico, consulta.getMedico());
        assertEquals(120, consulta.calcularValor(), 0.001);
        assertThrows(HorarioIndisponivelException.class,
                () -> service.agendar(paciente.getCpf(), medico.getCrm(), horario, "Retorno"));
        assertEquals(1, consultas.listar().size());
        Medico outro = new Pediatra("Caio", "11122233344", "9999", "456");
        medicos.cadastrar(outro);
        service.agendar(paciente.getCpf(), outro.getCrm(), horario, "Rotina");
        assertEquals(2, consultas.listar().size());
    }

    @Test
    public void listaEmOrdemSemExporAgenda() throws Exception {
        service.agendar(paciente.getCpf(), medico.getCrm(), horario.plusHours(2), "Rotina");
        service.agendar(paciente.getCpf(), medico.getCrm(), horario, "Rotina");
        List<Consulta> lista = service.listarAgenda(medico.getCrm());
        assertEquals(horario, lista.get(0).getDataHora());
        lista.clear();
        assertEquals(2, service.listarAgenda(medico.getCrm()).size());
    }

    @Test
    public void exigeCadastrosExistentes() {
        assertThrows(PacienteNaoEncontradoException.class, () -> service.agendar("00000000000", "123", horario, "Rotina"));
        assertThrows(MedicoNaoEncontradoException.class, () -> service.agendar(paciente.getCpf(), "000", horario, "Rotina"));
        assertTrue(consultas.listar().isEmpty());
    }

    @Test
    public void liberaHorarioQuandoGravacaoFalha() {
        consultas.falhar = true;
        assertThrows(IllegalStateException.class, () -> service.agendar(paciente.getCpf(), "123", horario, "Rotina"));
        assertTrue(medico.estaDisponivel(horario));
    }

    @Test
    public void verificaConsultasCarregadasMesmoSemAgendaEmMemoria() throws Exception {
        consultas.cadastrar(new Consulta(paciente, medico, horario, "Rotina", 150));
        assertTrue(medico.getAgenda().isEmpty());
        assertThrows(HorarioIndisponivelException.class, () -> service.agendar(paciente.getCpf(), "123", horario, "Rotina"));
        assertEquals(1, service.listarAgenda("123").size());
    }
}
