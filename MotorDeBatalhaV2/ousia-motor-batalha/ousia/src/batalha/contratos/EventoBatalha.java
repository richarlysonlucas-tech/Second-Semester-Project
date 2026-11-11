package batalha.contratos;

import java.util.Objects;

/**
 * Mensagem estruturada da batalha. O motor nunca imprime nada: ele devolve eventos,
 * e o terminal (testes) ou a interface 2D decidem como exibi-los.
 */
public final class EventoBatalha {

    private final TipoEvento tipo;
    private final String texto;

    public EventoBatalha(TipoEvento tipo, String texto) {
        this.tipo = Objects.requireNonNull(tipo, "tipo");
        this.texto = Objects.requireNonNull(texto, "texto");
    }

    public TipoEvento getTipo() { return tipo; }

    public String getTexto() { return texto; }

    @Override
    public String toString() { return "[" + tipo + "] " + texto; }
}
