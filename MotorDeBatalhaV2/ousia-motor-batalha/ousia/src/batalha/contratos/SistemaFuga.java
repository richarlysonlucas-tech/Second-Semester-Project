package batalha.contratos;

/** Contrato da regra de fuga (módulo de regras/cálculos). */
public interface SistemaFuga {

    /** Decide se a fuga é permitida e, sendo, se foi bem-sucedida. */
    ResultadoFuga tentarFuga(Combatente jogador, Combatente inimigo);
}
