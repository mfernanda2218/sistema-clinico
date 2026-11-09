package clinica.model;

import clinica.interfaces.EstrategiaPreco;

public class PrecoParticular implements EstrategiaPreco {
    @Override
    public double calcular(double valorBase) {
        if (!Double.isFinite(valorBase) || valorBase < 0) {
            throw new IllegalArgumentException("Valor base deve ser válido e não negativo.");
        }
        return valorBase;
    }

    @Override
    public String getNome() {
        return "Particular";
    }
}
