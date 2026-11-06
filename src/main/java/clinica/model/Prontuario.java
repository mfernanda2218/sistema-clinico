package clinica.model;

import java.util.ArrayList;
import java.util.List;

public class Prontuario {

    private final List<Registro> registros;
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
        return new ArrayList<>(registros);
    }
}
