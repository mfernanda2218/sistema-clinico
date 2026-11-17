package clinica.service;

import clinica.exception.ConvenioInvalidoException;
import clinica.exception.HorarioIndisponivelException;
import clinica.exception.MedicoNaoEncontradoException;
import clinica.exception.PacienteNaoEncontradoException;
import clinica.interfaces.EstrategiaPreco;
import clinica.interfaces.Repositorio;
import clinica.model.*;
import java.time.LocalDateTime;
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
}
