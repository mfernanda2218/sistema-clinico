package clinica.interfaces;

import clinica.exception.ArquivoInvalidoException;
import java.util.List;

public interface Repositorio<T> {

    void cadastrar(T entidade);
    List<T> listar();
    T buscar (String identificador);
    void atualizar(T entidade);
    void excluir(String identificador);

    default void carregar() throws ArquivoInvalidoException {};
    default void salvar(){};
}
