package batalha.contratos;

/** Resposta do módulo de regras/cálculos a um pedido de fuga. */
public enum ResultadoFuga {
    /** Não existe possibilidade de fuga nesta batalha (ex.: chefe). Não consome turno. */
    NAO_PERMITIDA,
    /** A fuga era possível, mas falhou. Consome o turno. */
    FALHOU,
    /** A fuga deu certo. A batalha termina com estado FUGA. */
    SUCESSO
}
