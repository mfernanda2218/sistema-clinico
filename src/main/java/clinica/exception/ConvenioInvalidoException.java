package clinica.exception;

/** Excecao lancada ao informar um convenio vazio ou nao aceito. */
public class ConvenioInvalidoException extends Exception {

    public ConvenioInvalidoException(String mensagem) {
        super(mensagem);
    }
}
