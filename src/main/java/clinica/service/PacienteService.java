package clinica.service;

import clinica.exception.PacienteNaoEncontradoException;
import clinica.interfaces.Repositorio;
import clinica.model.Paciente;
import clinica.repository.PacienteRepositorioJson;

import java.util.List;

public class PacienteService {

    private final Repositorio<Paciente> repositorio;

    public PacienteService() {
        this(new PacienteRepositorioJson());
    }

    public PacienteService(Repositorio<Paciente> repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("Repositório não pode ser nulo.");
        }
        this.repositorio = repositorio;
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

        try {
            repositorio.excluir(cpf);
        } catch (IllegalArgumentException e) {
            throw new PacienteNaoEncontradoException("Paciente não encontrado para o CPF: " + cpf);
        }
    }
}