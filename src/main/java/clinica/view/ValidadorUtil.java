package clinica.view;

import clinica.model.Pessoa;
import javafx.scene.control.Control;

import java.time.LocalDate;

public final class ValidadorUtil {

    public static final String REGEX_CPF = "\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}";
    public static final String REGEX_CRM = "\\d{4,6}(-[A-Z]{2})?";
    public static final String REGEX_HORA = "([01]\\d|2[0-3]):[0-5]\\d";

    private ValidadorUtil() {}

    public static boolean isTextoValido(String valor) {
        return valor != null && !valor.isBlank();
    }

    public static boolean isCpfValido(String cpf) {
        return isTextoValido(cpf) && Pessoa.cpfValido(cpf);
    }

    public static boolean isCrmValido(String crm) {
        return isTextoValido(crm) && crm.trim().matches(REGEX_CRM);
    }

    public static boolean isDataNascimentoValida(LocalDate data) {
        return data != null && !data.isAfter(LocalDate.now());
    }

    public static boolean isDataAgendamentoValida(LocalDate data) {
        return data != null && !data.isBefore(LocalDate.now());
    }

    public static boolean isHoraValida(String hora) {
        return isTextoValido(hora) && hora.trim().matches(REGEX_HORA);
    }

    public static void marcarInvalido(Control campo, boolean invalido) {
        if (invalido) {
            campo.setStyle("-fx-border-color: #d32f2f; -fx-border-width: 1.5px; -fx-border-radius: 3px;");
        } else {
            campo.setStyle(null);
        }
    }
}