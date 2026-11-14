package portas;

/**
 * Uma intenção do jogador durante a batalha (o front envia, o cérebro encaminha ao motor).
 */
public final class ComandoBatalha {
    public enum Tipo {
        ABRIR_ATAQUES,
        ABRIR_MOCHILA,
        VOLTAR,
        ATACAR,
        USAR_ITEM,
        FUGIR,
        TURNO_INIMIGO
    }

    private final Tipo tipo;
    private final String categoria;
    private final String id;

    private ComandoBatalha(Tipo tipo, String categoria, String id) {
        this.tipo = tipo;
        this.categoria = categoria;
        this.id = id;
    }

    public static ComandoBatalha abrirAtaques() { return new ComandoBatalha(Tipo.ABRIR_ATAQUES, null, null); }
    public static ComandoBatalha abrirMochila() { return new ComandoBatalha(Tipo.ABRIR_MOCHILA, null, null); }
    public static ComandoBatalha voltar() { return new ComandoBatalha(Tipo.VOLTAR, null, null); }
    public static ComandoBatalha fugir() { return new ComandoBatalha(Tipo.FUGIR, null, null); }
    public static ComandoBatalha turnoInimigo() { return new ComandoBatalha(Tipo.TURNO_INIMIGO, null, null); }

    /** @param categoria "FISICO", "MAGICO" ou "BUFF" */
    public static ComandoBatalha atacar(String categoria, String idAtaque) {
        return new ComandoBatalha(Tipo.ATACAR, categoria, idAtaque);
    }

    public static ComandoBatalha usarItem(String idItem) {
        return new ComandoBatalha(Tipo.USAR_ITEM, null, idItem);
    }

    public Tipo getTipo() { return tipo; }
    public String getCategoria() { return categoria; }
    public String getId() { return id; }
}
