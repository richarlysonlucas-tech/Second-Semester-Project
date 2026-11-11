package batalha;

/** Estado geral da batalha. Depois de sair de EM_ANDAMENTO, o estado nunca mais muda. */
public enum EstadoBatalha {
    EM_ANDAMENTO,
    VITORIA,
    DERROTA,
    FUGA
}
