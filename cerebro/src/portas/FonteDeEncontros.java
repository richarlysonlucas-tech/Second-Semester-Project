package portas;

import java.util.List;

/**
 * Porta de entrada do módulo de encontros. Tem a mesma assinatura de
 * ControladorEncontro.verificarPresenca(...), então a ligação com o código real é uma linha:
 *
 * <pre>FonteDeEncontros fonte = (j, x, y, a, acoes) -&gt; controlador.verificarPresenca(j, x, y, a, acoes);</pre>
 *
 * Devolve o JSON do contrato do front-end ("acao": SEM_ENCONTRO, INICIAR_BATALHA ou ENTRADA_INVALIDA).
 */
public interface FonteDeEncontros {
    String verificarPresenca(String jogadorId, double posicaoX, double posicaoY,
            String areaId, List<String> acoesRealizadas);
}
