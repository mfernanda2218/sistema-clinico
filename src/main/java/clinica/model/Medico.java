package clinica.model;

import clinica.interfaces.Agendavel;
import java.time.LocalDateTime;
import java.util.TreeMap;
import clinica.exception.MedicoNaoEncontradoException;

public abstract class Medico extends Pessoa implements Agendavel {
    private String crm;
    private final TreeMap<LocalDateTime, Consulta> agenda;

    public Medico(String nome, String cpf, String telefone, String crm)
            throws MedicoNaoEncontradoException {
        super(nome, cpf, telefone);
        setCrm(crm);
        this.agenda = new TreeMap<>();
    }

    public void setCrm(String crm) throws MedicoNaoEncontradoException {
        if (crm == null || crm.isBlank()) {
            throw new MedicoNaoEncontradoException("CRM não encontrado ou inválido.");
        }
        this.crm = crm.trim();
    }

    public String getCrm() {
        return crm;
    }

    public abstract String getEspecialidade();

    public abstract double getValorConsulta();

    @Override
    public boolean estaDisponivel(LocalDateTime horario) {
        return !agenda.containsKey(horario);
    }

    @Override
    public void reservar(Consulta consulta) {
        LocalDateTime horario = consulta.getHorario();
        if (!estaDisponivel(horario)) {
            throw new IllegalStateException("Horário já ocupado: " + horario);
        }
        agenda.put(horario, consulta);
    }

    public void liberar(LocalDateTime horario) {
        agenda.remove(horario);
    }

    public TreeMap<LocalDateTime, Consulta> getAgenda() {
        return agenda;
    }

    @Override
    public String getDescricaoPerfil() {
        return String.format("Médico: %s | CRM: %s | Especialidade: %s",
                getNome(), crm, getEspecialidade());
    }
}