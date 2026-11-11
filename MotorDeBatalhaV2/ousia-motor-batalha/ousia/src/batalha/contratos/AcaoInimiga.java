package batalha.contratos;

import java.util.Objects;

/** Decisão tomada pela IA para o turno do inimigo. */
public final class AcaoInimiga {

    private final TipoAtaque tipo;
    private final String idAtaque;

    public AcaoInimiga(TipoAtaque tipo, String idAtaque) {
        this.tipo = Objects.requireNonNull(tipo, "tipo");
        this.idAtaque = Objects.requireNonNull(idAtaque, "idAtaque");
    }

    public TipoAtaque getTipo() { return tipo; }

    public String getIdAtaque() { return idAtaque; }
}
