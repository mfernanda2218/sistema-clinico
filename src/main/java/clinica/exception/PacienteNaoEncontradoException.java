package clinica.exception;

/**
 * Deve ser lancada por PacienteService.buscarPorCpf quando o CPF nao existir.
 */
public class PacienteNaoEncontradoException extends Exception {

    public PacienteNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
