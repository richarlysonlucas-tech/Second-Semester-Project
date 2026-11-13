package estado;

/**
 * Estados globais do jogo. Só um deles está ativo por vez.
 */
public enum EstadoJogo {
    MENU,
    EXPLORACAO,
    BATALHA,
    HUB,
    GAME_OVER
}
