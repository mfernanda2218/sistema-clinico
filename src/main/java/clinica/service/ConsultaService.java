package clinica.service;

import clinica.exception.ConvenioInvalidoException;
import clinica.exception.ConsultaNaoEncontradaException;
import clinica.exception.HorarioIndisponivelException;
import clinica.exception.MedicoNaoEncontradoException;
import clinica.exception.PacienteNaoEncontradoException;
import clinica.interfaces.EstrategiaPreco;
import clinica.interfaces.Repositorio;
import clinica.model.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ConsultaService {
    private final Repositorio<Consulta> repositorio;
    private final PacienteService pacientes;
    private final MedicoService medicos;
    private final double descontoConvenio;

    public ConsultaService(Repositorio<Consulta> repositorio, PacienteService pacientes,
                           MedicoService medicos, double descontoConvenio) {
        if (repositorio == null || pacientes == null || medicos == null) {
            throw new IllegalArgumentException("Repositório e services são obrigatórios.");
        }
        new PrecoConvenio(descontoConvenio);
        this.repositorio = repositorio;
        this.pacientes = pacientes;
        this.medicos = medicos;
        this.descontoConvenio = descontoConvenio;
    }

    public Consulta agendar(String cpfPaciente, String crmMedico, LocalDateTime dataHora, String tipo)
            throws PacienteNaoEncontradoException, MedicoNaoEncontradoException,
            ConvenioInvalidoException, HorarioIndisponivelException {
        Paciente paciente = pacientes.buscarPorCpf(cpfPaciente);
        Medico medico = medicos.buscarPorCrm(crmMedico);
        paciente.setConvenio(paciente.getConvenio());
        EstrategiaPreco estrategia = new PrecoParticular();
        if (!paciente.isParticular()) {
            estrategia = new PrecoConvenio(descontoConvenio);
        }
        Consulta consulta = new Consulta(paciente, medico, dataHora, tipo, medico.getValorConsulta(), estrategia);
        for (Consulta existente : repositorio.listar()) {
            if (medico.getCrm().equals(existente.getCrmMedico()) && dataHora.equals(existente.getDataHora())) {
                throw new HorarioIndisponivelException("Médico " + medico.getNome() + " (CRM "
                        + medico.getCrm() + ") já possui consulta no horário " + dataHora);
            }
        }
        medico.reservar(consulta);
        try {
            repositorio.cadastrar(consulta);
        } catch (RuntimeException e) {
            medico.liberar(dataHora);
            throw e;
        }
        return consulta;
    }

    public List<Consulta> listarAgenda(String crm) throws MedicoNaoEncontradoException {
        Medico medico = medicos.buscarPorCrm(crm);
        TreeMap<LocalDateTime, Consulta> agenda = medico.getAgenda();
        for (Consulta consulta : repositorio.listar()) {
            if (medico.getCrm().equals(consulta.getCrmMedico())) {
                agenda.put(consulta.getDataHora(), consulta);
            }
        }
        return new ArrayList<>(agenda.values());
    }

    public List<Consulta> listar() {
        return new ArrayList<>(repositorio.listar());
    }

    public Consulta buscarPorId(String id) throws ConsultaNaoEncontradaException {
        Consulta consulta = repositorio.buscar(id);
        if (consulta == null) {
            throw new ConsultaNaoEncontradaException("Consulta não encontrada: " + id);
        }
        return consulta;
    }

    public List<Consulta> listarPorPaciente(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF é obrigatório.");
        }
        String cpfLimpo = cpf.replaceAll("\\D", "");
        List<Consulta> resultado = new ArrayList<>();
        for (Consulta consulta : repositorio.listar()) {
            if (consulta.getCpfPaciente().equals(cpfLimpo)) {
                resultado.add(consulta);
            }
        }
        return resultado;
    }

    public List<Consulta> listarPorMedico(String crm) {
        if (crm == null || crm.isBlank()) {
            throw new IllegalArgumentException("CRM é obrigatório.");
        }
        List<Consulta> resultado = new ArrayList<>();
        for (Consulta consulta : repositorio.listar()) {
            if (consulta.getCrmMedico().equals(crm.trim())) {
                resultado.add(consulta);
            }
        }
        return resultado;
    }

    public List<Consulta> listarPorData(LocalDate data) {
        if (data == null) {
            throw new IllegalArgumentException("Data é obrigatória.");
        }
        List<Consulta> resultado = new ArrayList<>();
        for (Consulta consulta : repositorio.listar()) {
            if (consulta.getDataHora().toLocalDate().equals(data)) {
                resultado.add(consulta);
            }
        }
        return resultado;
    }

    public void remarcar(String id, LocalDateTime novoHorario)
            throws ConsultaNaoEncontradaException, HorarioIndisponivelException {
        Consulta antiga = buscarPorId(id);
        Consulta nova = antiga.copiarParaHorario(novoHorario);
        Medico medico = antiga.getMedico();
        for (Consulta existente : listarPorMedico(medico.getCrm())) {
            if (!existente.getId().equals(id) && existente.getDataHora().equals(novoHorario)) {
                throw new HorarioIndisponivelException("Médico " + medico.getNome()
                        + " (CRM " + medico.getCrm() + ") já possui consulta no horário " + novoHorario);
            }
        }
        Consulta reserva = medico.getAgenda().get(novoHorario);
        if (reserva != null && !reserva.getId().equals(id)) {
            throw new HorarioIndisponivelException("Médico " + medico.getNome()
                    + " (CRM " + medico.getCrm() + ") já possui consulta no horário " + novoHorario);
        }
        repositorio.atualizar(nova);
        Consulta reservaAntiga = medico.getAgenda().get(antiga.getDataHora());
        if (reservaAntiga != null && reservaAntiga.getId().equals(id)) {
            medico.liberar(antiga.getDataHora());
        }
        medico.reservar(nova);
    }

    public void cancelar(String id) throws ConsultaNaoEncontradaException {
        Consulta consulta = buscarPorId(id);
        repositorio.excluir(id);
        Medico medico = consulta.getMedico();
        Consulta reserva = medico.getAgenda().get(consulta.getDataHora());
        if (reserva != null && reserva.getId().equals(id)) {
            medico.liberar(consulta.getDataHora());
        }
    }
}
