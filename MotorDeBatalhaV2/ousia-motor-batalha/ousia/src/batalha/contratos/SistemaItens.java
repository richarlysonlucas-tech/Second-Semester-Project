package batalha.contratos;

import java.util.List;

/** Contrato do módulo de itens e inventário. O motor não controla o inventário. */
public interface SistemaItens {

    /** Itens que o combatente pode usar em batalha (para o submenu da mochila). */
    List<OpcaoAcao> listarItens(Combatente usuario);

    /**
     * Usa um item e aplica seus efeitos.
     *
     * @param usuario  quem usa
     * @param oponente o outro combatente (o módulo decide o alvo real do item)
     * @param idItem   identificador do item escolhido
     */
    ResultadoAcao usar(Combatente usuario, Combatente oponente, String idItem);
}
