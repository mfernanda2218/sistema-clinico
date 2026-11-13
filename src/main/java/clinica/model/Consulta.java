package clinica.model;

import clinica.interfaces.Cobravel;
import clinica.exception.HorarioIndisponivelException;
import clinica.interfaces.EstrategiaPreco;
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
    private transient EstrategiaPreco estrategia;

    public Consulta(Paciente paciente, Medico medico, LocalDateTime dataHora,
                    String tipo, double valorBase) throws HorarioIndisponivelException {
        this(paciente, medico, dataHora, tipo, valorBase, new PrecoParticular());
    }

    public Consulta(Paciente paciente, Medico medico, LocalDateTime dataHora,
                    String tipo, double valorBase, EstrategiaPreco estrategia) throws HorarioIndisponivelException {
        this.id = UUID.randomUUID().toString();
    	setPaciente(paciente);
    	setDataHora(dataHora);   
    	setMedico(medico);      
    	setTipo(tipo);
    	setValorBase(valorBase);
    	setEstrategia(estrategia);
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
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        if (medico == null) {
            throw new IllegalArgumentException("Médico é obrigatório.");
        }
        if (this.medico != null && this.medico != medico) {
            this.medico.liberar(this.dataHora);
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

     public void setDataHora(LocalDateTime dataHora) throws HorarioIndisponivelException {
        if (dataHora == null || !dataHora.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Data e hora devem estar no futuro.");
        }
        if (medico != null && medico.possuiReserva(this)) {
            medico.atualizarHorario(this, dataHora);
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
        return estrategia.calcular(valorBase);
    }

    public void setEstrategia(EstrategiaPreco estrategia) {
        if (estrategia == null) {
            throw new IllegalArgumentException("Estratégia de preço é obrigatória.");
        }
        this.estrategia = estrategia;
        this.formaCobranca = estrategia.getNome();
    }

    @Override
    public String getFormaCobranca() {
        return formaCobranca;
    }
}
