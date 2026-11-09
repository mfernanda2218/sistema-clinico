package clinica.model;

import clinica.interfaces.EstrategiaPreco;

public class PrecoConvenio implements EstrategiaPreco {
    private double percentualDesconto;

    public PrecoConvenio(double percentualDesconto) {
        setPercentualDesconto(percentualDesconto);
    }

    public double getPercentualDesconto() {
        return percentualDesconto;
    }

    public void setPercentualDesconto(double percentualDesconto) {
        if (!Double.isFinite(percentualDesconto) || percentualDesconto < 0 || percentualDesconto > 100) {
            throw new IllegalArgumentException("Desconto deve estar entre 0 e 100.");
        }
        this.percentualDesconto = percentualDesconto;
    }

    @Override
    public double calcular(double valorBase) {
        if (!Double.isFinite(valorBase) || valorBase < 0) {
            throw new IllegalArgumentException("Valor base deve ser válido e não negativo.");
        }
        return valorBase * (1 - percentualDesconto / 100);
    }

    @Override
    public String getNome() {
        return "Convênio";
    }
}
