package clinica.repository;

import clinica.interfaces.Repositorio;
import clinica.model.Paciente;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PacienteRepositorioMemoria implements Repositorio<Paciente> {

    private static final PacienteRepositorioMemoria INSTANCIA = new PacienteRepositorioMemoria();
    private final ArrayList<Paciente> pacientes;
    private final HashMap<String, Paciente> pacientesPorCpf;

    private PacienteRepositorioMemoria() {
        this.pacientes = new ArrayList<>();
        this.pacientesPorCpf = new HashMap<>();
    }

    public static PacienteRepositorioMemoria getInstancia() {
        return INSTANCIA;
    }

    @Override
    public void cadastrar(Paciente paciente) {
        if (paciente == null || paciente.getCpf() == null) {
            throw new IllegalArgumentException("Paciente ou CPF não pode ser nulo.");
        }

        String cpf = paciente.getCpf();

        if (pacientesPorCpf.containsKey(cpf)) {
            throw new IllegalArgumentException("Paciente com CPF já cadastrado.");
        }

        pacientes.add(paciente);
        pacientesPorCpf.put(cpf, paciente);
    }

    @Override
    public List<Paciente> listar() {
        return new ArrayList<>(pacientes);
    }

    @Override
    public Paciente buscar(String cpf) {
        return pacientesPorCpf.get(cpf);
    }

    @Override
    public void atualizar(Paciente paciente) {
        if (paciente == null || paciente.getCpf() == null) {
            throw new IllegalArgumentException("Paciente ou CPF não pode ser nulo.");
        }

        String cpf = paciente.getCpf();
        Paciente antigo = pacientesPorCpf.get(cpf);

        if (antigo == null) {
            throw new IllegalArgumentException("Paciente não encontrado.");
        }

        int indice = pacientes.indexOf(antigo);
        pacientes.set(indice, paciente);
        pacientesPorCpf.put(cpf, paciente);
    }

    @Override
    public void excluir(String cpf) {
        if (cpf == null) {
            throw new IllegalArgumentException("CPF não pode ser nulo.");
        }

        Paciente paciente = pacientesPorCpf.remove(cpf);

        if (paciente == null) {
            throw new IllegalArgumentException("Paciente não encontrado.");
        }

        pacientes.remove(paciente);
    }
}