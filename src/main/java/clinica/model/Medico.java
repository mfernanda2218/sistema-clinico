package clinica.model;

import clinica.interfaces.Agendavel;
import clinica.exception.HorarioIndisponivelException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.TreeMap;
import java.util.regex.Pattern;

public abstract class Medico extends Pessoa implements Agendavel {
    private static final Pattern CRM_PATTERN = Pattern.compile("\\d{4,6}(-[A-Z]{2})?");

    private String crm;
    private final transient TreeMap<LocalDateTime, Consulta> agenda;

    public Medico(String nome, String cpf, String telefone, String crm) {
        super(nome, cpf, telefone);
        setCrm(crm);
        this.agenda = new TreeMap<>();
    }

    public static boolean crmValido(String crm) {
        return crm != null
                && CRM_PATTERN.matcher(crm.trim().toUpperCase(Locale.ROOT)).matches();
    }

    public void setCrm(String crm) {
        if (crm == null || crm.isBlank()) {
            throw new IllegalArgumentException("CRM não pode ser vazio.");
        }
        String crmLimpo = crm.trim();
        if (!crmValido(crmLimpo)) {
            throw new IllegalArgumentException(
                    "CRM inválido. Use de 4 a 6 dígitos, opcionalmente seguido da UF (ex.: 123456-SP).");
        }
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
        LocalDateTime horario = consulta.getDataHora();
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

    public void registrarReserva(Consulta consulta) {
        if (consulta == null || consulta.getMedico() != this) {
            throw new IllegalArgumentException("A consulta deve pertencer a este médico.");
        }
        agenda.values().removeIf(c -> c.getId().equals(consulta.getId()));
        agenda.put(consulta.getDataHora(), consulta);
    }

    public void limparAgenda() {
        agenda.clear();
    }

    public boolean possuiReserva(Consulta consulta) {
        return agenda.containsValue(consulta);
    }

    public void atualizarHorario(Consulta consulta, LocalDateTime novoHorario) throws HorarioIndisponivelException {
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

    public boolean temConflito(Consulta novaConsulta) {
    LocalDateTime inicioNovo = novaConsulta.getDataHora();
    LocalDateTime fimNovo = novaConsulta.getDataHoraFim();
}
    for (Consulta existente : this.agenda) {
    if (existente.getId().equals(novaConsulta.getId())) {
            continue;
      }
    LocalDateTime inicioExistente = existente.getDataHora();
        LocalDateTime fimExistente = existente.getDataHoraFim();
    boolean sobrepoe = inicioNovo.isBefore(fimExistente) && fimNovo.isAfter(inicioExistente);
        if (sobrepoe) {
            return true;
        }
    }
    return false;
} 
}
