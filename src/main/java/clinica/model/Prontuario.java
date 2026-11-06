package clinica.model;

import java.util.ArrayList;
import java.util.List;

public class Prontuario {

    private final List<Registro> registros;

    // Apenas classes do pacote podem criar um prontuario.
    Prontuario() {
        registros = new ArrayList<>();
    }

    public void adicionarRegistro(Registro registro) {
        if (registro == null) {
            throw new IllegalArgumentException("Registro nao pode ser nulo.");
        }
        registros.add(registro);
    }

    public List<Registro> getRegistros() {
        // A copia impede que a lista interna seja alterada por fora.
        return new ArrayList<>(registros);
    }
}
