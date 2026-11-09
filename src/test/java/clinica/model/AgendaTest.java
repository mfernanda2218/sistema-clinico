package clinica.model;

import com.google.gson.Gson;
import clinica.exception.HorarioIndisponivelException;
import org.junit.Before;
import org.junit.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.Assert.*;

public class AgendaTest {
    private Medico medico;
    private Paciente paciente;
    private Consulta consulta;
    private LocalDateTime horario;

    @Before
    public void preparar() throws Exception {
        medico = new Clinico("Ana", "12345678900", "9999", "123");
        paciente = new Paciente("Bia", "98765432100", "9999", LocalDate.of(2000, 1, 1));
        horario = LocalDateTime.now().plusDays(1);
        consulta = new Consulta(paciente, medico, horario, "Rotina", 150);
        medico.reservar(consulta);
    }

    @Test
    public void protegeAgenda() {
        medico.getAgenda().clear();
        assertFalse(medico.estaDisponivel(horario));
    }

    @Test
    public void remarcaReserva() throws Exception {
        LocalDateTime novoHorario = horario.plusHours(1);
        consulta.setDataHora(novoHorario);
        assertTrue(medico.estaDisponivel(horario));
        assertSame(consulta, medico.getAgenda().get(novoHorario));
        assertEquals(novoHorario, consulta.getDataHora());
        consulta.setDataHora(novoHorario);
        assertEquals(1, medico.getAgenda().size());
    }

    @Test
    public void preservaReservaQuandoHaConflito() throws Exception {
        LocalDateTime ocupado = horario.plusHours(1);
        Consulta outra = new Consulta(paciente, medico, ocupado, "Retorno", 100);
        medico.reservar(outra);
        assertThrows(HorarioIndisponivelException.class, () -> consulta.setDataHora(ocupado));
        assertEquals(horario, consulta.getDataHora());
        assertSame(consulta, medico.getAgenda().get(horario));
        assertSame(outra, medico.getAgenda().get(ocupado));
    }

    @Test
    public void rejeitaMedicoIncorretoETrocaComReserva() throws Exception {
        Medico outro = new Pediatra("Caio", "11122233344", "9999", "456");
        assertThrows(IllegalArgumentException.class, () -> outro.reservar(consulta));
        assertThrows(IllegalStateException.class, () -> consulta.setMedico(outro));
        assertSame(medico, consulta.getMedico());
        medico.liberar(horario);
        consulta.setMedico(outro);
        outro.reservar(consulta);
        assertEquals("456", consulta.getCrmMedico());
        assertSame(consulta, outro.getAgenda().get(horario));
    }

    @Test
    public void preservaIdentificadores() throws Exception {
        assertThrows(IllegalStateException.class, () -> paciente.setCpf("11122233344"));
        assertThrows(IllegalStateException.class, () -> medico.setCrm("456"));
        paciente.setCpf("987.654.321-00");
        medico.setCrm(" 123 ");
        assertEquals(paciente.getCpf(), consulta.getCpfPaciente());
        assertEquals(medico.getCrm(), consulta.getCrmMedico());
    }

    @Test
    public void naoGravaAgendaNoJson() {
        assertFalse(new Gson().toJson(medico).contains("agenda"));
    }

    @Test
    public void rejeitaDadosInvalidosSemPerderReserva() {
        assertThrows(IllegalArgumentException.class, () -> medico.reservar(null));
        assertThrows(IllegalArgumentException.class, () -> medico.estaDisponivel(null));
        assertThrows(IllegalArgumentException.class, () -> medico.liberar(null));
        assertThrows(IllegalArgumentException.class, () -> consulta.setDataHora(null));
        assertThrows(IllegalArgumentException.class, () -> consulta.setDataHora(LocalDateTime.now().minusDays(1)));
        assertSame(consulta, medico.getAgenda().get(horario));
    }
    @Test
    public void informaMedicoEHorarioNoConflito() throws Exception {
        Consulta outra = new Consulta(paciente, medico, horario, "Retorno", 100);
        HorarioIndisponivelException erro = assertThrows(HorarioIndisponivelException.class,
                () -> medico.reservar(outra));
        assertTrue(erro.getMessage().contains(medico.getNome()));
        assertTrue(erro.getMessage().contains(medico.getCrm()));
        assertTrue(erro.getMessage().contains(horario.toString()));
        assertSame(consulta, medico.getAgenda().get(horario));
        assertEquals(1, medico.getAgenda().size());
    }
}
