package clinica.model;

import java.time.LocalDate;

public class Registro {

    private LocalDate data;
    private String descricao;
    private String observacoes;

    public Registro(LocalDate data, String descricao, String observacoes) {
        setData(data);
        setDescricao(descricao);
        setObservacoes(observacoes);
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        if (data == null) {
            throw new IllegalArgumentException("Data nao pode ser nula.");
        }
        this.data = data;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descricao nao pode ser vazia.");
        }
        this.descricao = descricao.trim();
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes == null ? "" : observacoes.trim();
    }
}
