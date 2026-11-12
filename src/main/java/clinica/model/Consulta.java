package clinica.model;

import clinica.interfaces.Cobravel;
import java.time.LocalDateTime;
import java.util.UUID;

public class Consulta implements Cobravel {
    private final String id;
    private transient Paciente paciente;
    private transient Medico medico;
    private String cpfPaciente;
    private String crmMedico;
    private LocalDateTime dataHora;
    private String tipo;
    private double valorBase;
    private String formaCobranca;

    public Consulta(Paciente paciente, Medico medico, LocalDateTime dataHora,
                    String tipo, double valorBase) {
        this.id = UUID.randomUUID().toString();
        setPaciente(paciente);
        setMedico(medico);
        setDataHora(dataHora);
        setTipo(tipo);
        setValorBase(valorBase);
    }

    public String getId() {
        return id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        if (paciente == null) {
            throw new IllegalArgumentException("Paciente é obrigatório.");
        }
        this.paciente = paciente;
        this.cpfPaciente = paciente.getCpf();
        this.formaCobranca = paciente.isParticular() ? "Particular" : "Convênio";
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        if (medico == null) {
            throw new IllegalArgumentException("Médico é obrigatório.");
        }
        this.medico = medico;
        this.crmMedico = medico.getCrm();
    }

    public String getCpfPaciente() {
        return cpfPaciente;
    }

    public String getCrmMedico() {
        return crmMedico;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public LocalDateTime getHorario() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        if (dataHora == null || !dataHora.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Data e hora devem estar no futuro.");
        }
        this.dataHora = dataHora;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("Tipo da consulta é obrigatório.");
        }
        this.tipo = tipo.trim();
    }

    public double getValorBase() {
        return valorBase;
    }

    public void setValorBase(double valorBase) {
        if (!Double.isFinite(valorBase) || valorBase < 0) {
            throw new IllegalArgumentException("Valor deve ser um número válido maior ou igual a zero.");
        }
        this.valorBase = valorBase;
    }

    @Override
    public double calcularValor() {
        return valorBase;
    }

    @Override
    public String getFormaCobranca() {
        return formaCobranca;
    }
}
