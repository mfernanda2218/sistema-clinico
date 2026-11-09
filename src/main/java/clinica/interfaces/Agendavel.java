package clinica.interfaces;

import java.time.LocalDateTime;
import java.util.TreeMap;

import clinica.model.Consulta;

public interface Agendavel {

    boolean estaDisponivel(LocalDateTime horario);

    void reservar(Consulta consulta);

    void liberar(LocalDateTime horario);

    TreeMap<LocalDateTime, Consulta> getAgenda();
}