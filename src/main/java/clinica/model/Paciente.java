package clinica.model;

import clinica.exception.ConvenioInvalidoException;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Paciente extends Pessoa {

    private static final List<String> CONVENIOS_ACEITOS = List.of("Unimed");

    private LocalDate dataNascimento;
    private String convenio; // null = particular

    // Construtor COM convênio
    public Paciente(String nome, String cpf, String telefone,
                    LocalDate dataNascimento, String convenio) throws ConvenioInvalidoException {
        super(nome, cpf, telefone);
        setDataNascimento(dataNascimento);
        setConvenio(convenio);
    }

    // Construtor SEM convênio (particular)
    public Paciente(String nome, String cpf, String telefone,
                    LocalDate dataNascimento) {
        super(nome, cpf, telefone);
        setDataNascimento(dataNascimento);
        this.convenio = null;
    }

    // Getters
    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getConvenio() {
        return convenio;
    }

    public boolean isParticular() {
        return convenio == null;
    }

    // Setters
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

    // Polimorfismo
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

    // Teste rapido
    /*
    public static void main(String[] args) {
        Paciente p1 = new Paciente("Maria Silva", "123.456.789-00", "(82) 99999-1111",
                           LocalDate.of(1995, 5, 20), "Unimed");

        Paciente p2 = new Paciente("João Santos", "98765432100", "82988887777",
                                LocalDate.of(1988, 12, 3)); // particular

        System.out.println(p1.getDescricaoPerfil());
        System.out.println(p2.getDescricaoPerfil());
        System.out.println(p1);
    }*/

}
