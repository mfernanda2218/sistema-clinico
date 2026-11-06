package clinica.model;

import clinica.exception.ConvenioInvalidoException;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Paciente extends Pessoa {

    private static final List<String> CONVENIOS_ACEITOS = List.of("Unimed");

    private LocalDate dataNascimento;
    private final Prontuario prontuario;
    private String convenio;
    public Paciente(String nome, String cpf, String telefone,
                    LocalDate dataNascimento, String convenio) throws ConvenioInvalidoException {
        super(nome, cpf, telefone);
        setDataNascimento(dataNascimento);
        setConvenio(convenio);
        this.prontuario = new Prontuario();
    }
    public Paciente(String nome, String cpf, String telefone,
                    LocalDate dataNascimento) {
        super(nome, cpf, telefone);
        setDataNascimento(dataNascimento);
        this.convenio = null;
        this.prontuario = new Prontuario();
    }
    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public Prontuario getProntuario() {
        return prontuario;
    }

    public String getConvenio() {
        return convenio;
    }

    public boolean isParticular() {
        return convenio == null;
    }
    public void setDataNascimento(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            throw new IllegalArgumentException("Data de nascimento não pode ser nula.");
        }
        if (dataNascimento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de nascimento não pode ser futura.");
        }
        this.dataNascimento = dataNascimento;
    }

    public void setConvenio(String convenio) throws ConvenioInvalidoException {
        if (convenio == null) {
            this.convenio = null;
            return;
        }
        if (convenio.isBlank()) {
            throw new ConvenioInvalidoException("Convenio nao pode ser vazio. Use null para particular.");
        }
        String nomeConvenio = convenio.trim();
        for (String aceito : CONVENIOS_ACEITOS) {
            if (aceito.equalsIgnoreCase(nomeConvenio)) {
                this.convenio = aceito;
                return;
            }
        }
        throw new ConvenioInvalidoException("Convenio nao aceito: " + nomeConvenio
                + ". Convenios aceitos: " + CONVENIOS_ACEITOS);
    }
    @Override
    public String getDescricaoPerfil() {
        String tipo = isParticular() ? "Particular" : "Convênio: " + convenio;
        return String.format("Paciente: %s | %s | Nascimento: %s",
                getNome(),
                tipo,
                dataNascimento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    @Override
    public String toString() {
        return getDescricaoPerfil() + " | CPF: " + getCpf() + " | Tel: " + getTelefone();
    }


}
