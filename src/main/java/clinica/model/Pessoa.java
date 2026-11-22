package clinica.model;

import java.util.regex.Pattern;

public abstract class Pessoa {

    private static final Pattern TELEFONE_PATTERN = Pattern.compile(
            "\\(\\d{2}\\)\\s?\\d{4,5}-\\d{4}|\\d{10,11}");

    private String nome;
    private String cpf;
    private String telefone;

    public Pessoa(String nome, String cpf, String telefone) {
        setNome(nome);
        setCpf(cpf);
        setTelefone(telefone);
    }

    // Getters
    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    // Setters com validação
    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome não pode ser vazio.");
        }
        this.nome = nome.trim();
    }

    public static boolean cpfValido(String cpf) {
        if (cpf == null) {
            return false;
        }
        String digitos = cpf.replaceAll("\\D", "");
        if (digitos.length() != 11 || digitos.chars().distinct().count() == 1) {
            return false;
        }
        for (int i = 9; i < 11; i++) {
            int soma = 0;
            for (int j = 0; j < i; j++) {
                soma += (digitos.charAt(j) - '0') * (i + 1 - j);
            }
            int digito = (soma * 10) % 11;
            if (digito == 10) {
                digito = 0;
            }
            if (digito != digitos.charAt(i) - '0') {
                return false;
            }
        }
        return true;
    }

    public void setCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF não pode ser vazio.");
        }
        // Remove caracteres não numéricos
        String cpfLimpo = cpf.replaceAll("\\D", "");
        if (!cpfValido(cpfLimpo)) {
            throw new IllegalArgumentException("CPF inválido: informe 11 dígitos com dígitos verificadores corretos.");
        }
        if (this.cpf != null && !this.cpf.equals(cpfLimpo)) {
            throw new IllegalStateException("CPF não pode ser alterado após o cadastro.");
        }
        this.cpf = cpfLimpo;
    }

    public static boolean telefoneValido(String telefone) {
        return telefone != null && TELEFONE_PATTERN.matcher(telefone.trim()).matches();
    }

    public void setTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            throw new IllegalArgumentException("Telefone não pode ser vazio.");
        }
        String tel = telefone.trim();
        if (!telefoneValido(tel)) {
            throw new IllegalArgumentException(
                    "Telefone inválido. Use (XX)XXXXX-XXXX ou 10-11 dígitos.");
        }
        this.telefone = tel;
    }

    // Método abstrato (polimorfismo)
    public abstract String getDescricaoPerfil();

    @Override
    public String toString() {
        return String.format("Nome: %s | CPF: %s | Telefone: %s", nome, cpf, telefone);
    }
}
