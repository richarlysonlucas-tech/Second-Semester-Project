package batalha.contratos;

/** Classifica um {@link EventoBatalha} para que a interface gráfica saiba como apresentá-lo. */
public enum TipoEvento {
    BATALHA_INICIADA,
    TURNO_JOGADOR,
    TURNO_INIMIGO,
    MENU_ALTERADO,
    ACAO_INVALIDA,
    FUGA,
    BATALHA_ENCERRADA,
    /**
     * Um módulo externo devolveu algo inesperado (resultado nulo, evento nulo, ação da IA inválida...).
     * Indica problema de INTEGRAÇÃO entre módulos, diferente de {@link #ACAO_INVALIDA}, que é um erro
     * de uso (ex.: ação fora do turno). A interface pode registrar ou destacar esse evento.
     */
    ERRO_INTEGRACAO,
    /** Mensagem genérica, normalmente gerada pelos módulos externos (dano, cura, buff...). */
    MENSAGEM
}
