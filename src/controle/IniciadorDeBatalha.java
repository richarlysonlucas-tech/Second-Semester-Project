package controle;

import modelo.Encontro;

/**
 * Contrato usado pelo sistema de encontros para avisar o sistema de batalhas.
 * A batalha poderá receber uma implementação real sem alterar esta interface.
 */
public interface IniciadorDeBatalha {
    void iniciar(Encontro encontro);
}
