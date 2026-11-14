package portas;

/**
 * Mensagem que o jogo devolve ao front-end (ex.: "dano causado", "turno do inimigo").
 * O tipo é um texto livre para não prender o cérebro ao enum de nenhum módulo
 * (o motor de batalha usa nomes como BATALHA_INICIADA, TURNO_JOGADOR, ERRO_INTEGRACAO...).
 */
public final class EventoDoJogo {
    private final String tipo;
    private final String texto;

    public EventoDoJogo(String tipo, String texto) {
        this.tipo = tipo == null ? "MENSAGEM" : tipo;
        this.texto = texto == null ? "" : texto;
    }

    public String getTipo() {
        return tipo;
    }

    public String getTexto() {
        return texto;
    }

    @Override
    public String toString() {
        return "[" + tipo + "] " + texto;
    }
}
