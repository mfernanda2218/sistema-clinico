package clinica.util;

import clinica.exception.HorarioIndisponivelException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;


public final class AlertaUtil {
	
	private AlertaUtil() {
		//evita instanciação
	}
	public static void erro(String titulo, String mensagem) {
		exibir(Alert.AlertType.ERROR, titulo, null, tratarMensagem(mensagem));
	}

	public static void erro(String titulo, Exception e) {
        String cabecalho = null;
        if (e instanceof HorarioIndisponivelException) {
            cabecalho = "Horário indisponível";
        }
        exibir(Alert.AlertType.ERROR, titulo, cabecalho, tratarMensagem(e.getMessage()));
    }

    public static void aviso(String mensagem) {
        exibir(Alert.AlertType.WARNING, "Atenção", null, tratarMensagem(mensagem));
    }

    public static void info(String titulo, String mensagem) {
        exibir(Alert.AlertType.INFORMATION, titulo, null, tratarMensagem(mensagem));
    }

    public static boolean confirmar(String titulo, String cabecalho, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(mensagem);

        ButtonType resultado = alert.showAndWait().orElse(null);
        return resultado == ButtonType.OK;
    }

    private static void exibir(Alert.AlertType tipo, String titulo, String cabecalho, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private static String tratarMensagem(String mensagem) {
        if (mensagem == null || mensagem.isBlank()) {
            return "Ocorreu um erro inesperado. Verifique os dados e tente novamente.";
        }
        return mensagem;
    }
}
