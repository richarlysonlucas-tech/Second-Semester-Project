package batalha.contratos;

/**
 * Contrato mínimo que o motor exige de um personagem (jogador ou inimigo).
 * Implementado pelo módulo de personagens. O motor NÃO conhece atributos.
 */
public interface Combatente {

    /** Nome para exibição. */
    String getNome();

    /** @return true se o combatente ainda pode lutar. Usado para decidir vitória/derrota. */
    boolean estaVivo();
}
