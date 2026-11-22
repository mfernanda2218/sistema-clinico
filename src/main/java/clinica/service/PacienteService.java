package clinica.service;

import clinica.exception.ArquivoInvalidoException;
import clinica.exception.PacienteNaoEncontradoException;
import clinica.interfaces.Repositorio;
import clinica.model.Paciente;
import clinica.model.Consulta;
import clinica.repository.PacienteRepositorioJson;

import java.util.List;

public class PacienteService {

    private final Repositorio<Paciente> repositorio;
    private final Repositorio<Consulta> consultas;

    public PacienteService() throws ArquivoInvalidoException {
        this(new PacienteRepositorioJson());
    }

    public PacienteService(Repositorio<Paciente> repositorio) {
        this(repositorio, null);
    }

    public PacienteService(Repositorio<Paciente> repositorio, Repositorio<Consulta> consultas) {
        if (repositorio == null) {
            throw new IllegalArgumentException("Repositório não pode ser nulo.");
        }
        this.repositorio = repositorio;
        this.consultas = consultas;
    }

    public void cadastrar(Paciente paciente) {
        if (paciente == null) {
            throw new IllegalArgumentException("Paciente não pode ser nulo.");
        }
        repositorio.cadastrar(paciente);
    }

    public List<Paciente> listar() {
        return repositorio.listar();
    }

    public Paciente buscarPorCpf(String cpf) throws PacienteNaoEncontradoException {
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF não pode ser nulo ou vazio.");
        }

        Paciente paciente = repositorio.buscar(cpf);
        if (paciente == null) {
            throw new PacienteNaoEncontradoException("Paciente não encontrado para o CPF: " + cpf);
        }

        return paciente;
    }

    public void atualizar(Paciente paciente) throws PacienteNaoEncontradoException {
        if (paciente == null || paciente.getCpf() == null || paciente.getCpf().isBlank()) {
            throw new IllegalArgumentException("Paciente ou CPF não pode ser nulo.");
        }

        try {
            repositorio.atualizar(paciente);
        } catch (IllegalArgumentException e) {
            throw new PacienteNaoEncontradoException("Paciente não encontrado para o CPF: " + paciente.getCpf());
        }
    }

    public void excluir(String cpf) throws PacienteNaoEncontradoException {
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF não pode ser nulo ou vazio.");
        }

        Paciente paciente = buscarPorCpf(cpf);
        if (consultas != null && consultas.listar().stream()
                .anyMatch(c -> paciente.getCpf().equals(c.getCpfPaciente()))) {
            throw new IllegalStateException("Não é possível excluir o paciente pois ele possui consultas registradas.");
        }
        try {
            repositorio.excluir(cpf);
        } catch (IllegalArgumentException e) {
            throw new PacienteNaoEncontradoException("Paciente não encontrado para o CPF: " + cpf);
        }
    }
}
