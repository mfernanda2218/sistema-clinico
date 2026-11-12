package clinica.model;

import clinica.interfaces.Agendavel;
import clinica.exception.HorarioIndisponivelException;
import java.time.LocalDateTime;
import java.util.TreeMap;
import clinica.exception.MedicoNaoEncontradoException;

public abstract class Medico extends Pessoa implements Agendavel {
    private String crm;
    private final transient TreeMap<LocalDateTime, Consulta> agenda;

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
        String crmLimpo = crm.trim();
        if (this.crm != null && !this.crm.equals(crmLimpo)) {
            throw new IllegalStateException("CRM não pode ser alterado após o cadastro.");
        }
        this.crm = crmLimpo;
    }

    public String getCrm() {
        return crm;
    }

    public abstract String getEspecialidade();

    public abstract double getValorConsulta();

    @Override
    public boolean estaDisponivel(LocalDateTime horario) {
        if (horario == null) {
            throw new IllegalArgumentException("Horário é obrigatório.");
        }
        return !agenda.containsKey(horario);
    }

    @Override
    public void reservar(Consulta consulta) throws HorarioIndisponivelException {
        if (consulta == null || consulta.getMedico() != this) {
            throw new IllegalArgumentException("A consulta deve pertencer a este médico.");
        }
        LocalDateTime horario = consulta.getHorario();
        if (horario == null || !horario.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Horário deve estar no futuro.");
        }
        if (!estaDisponivel(horario)) {
            throw new HorarioIndisponivelException("Médico " + getNome() + " (CRM " + crm + ") já possui consulta no horário " + horario);
        }
        agenda.put(horario, consulta);
    }

    public void liberar(LocalDateTime horario) {
        if (horario == null) {
            throw new IllegalArgumentException("Horário é obrigatório.");
        }
        agenda.remove(horario);
    }

    boolean possuiReserva(Consulta consulta) {
        return agenda.containsValue(consulta);
    }

    void atualizarHorario(Consulta consulta, LocalDateTime novoHorario) throws HorarioIndisponivelException {
        if (!possuiReserva(consulta)) {
            return;
        }
        Consulta consultaNoHorario = agenda.get(novoHorario);
        if (consultaNoHorario != null && consultaNoHorario != consulta) {
            throw new HorarioIndisponivelException("Médico " + getNome() + " (CRM " + crm + ") já possui consulta no horário " + novoHorario);
        }
        agenda.remove(consulta.getDataHora());
        agenda.put(novoHorario, consulta);
    }

    public TreeMap<LocalDateTime, Consulta> getAgenda() {
        return new TreeMap<>(agenda);
    }

    @Override
    public String getDescricaoPerfil() {
        return String.format("Médico: %s | CRM: %s | Especialidade: %s",
                getNome(), crm, getEspecialidade());
    }
}
