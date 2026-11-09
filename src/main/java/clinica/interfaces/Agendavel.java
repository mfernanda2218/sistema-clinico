package clinica.interfaces;

import clinica.model.Consulta;
import clinica.exception.HorarioIndisponivelException;
import java.time.LocalDateTime;
import java.util.TreeMap;

public interface Agendavel {
    boolean estaDisponivel(LocalDateTime horario);
    void reservar(Consulta consulta) throws HorarioIndisponivelException;
    void liberar(LocalDateTime horario);
    TreeMap<LocalDateTime, Consulta> getAgenda();
}
