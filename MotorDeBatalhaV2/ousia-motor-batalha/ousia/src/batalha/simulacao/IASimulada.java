package batalha.simulacao;

import batalha.contratos.AcaoInimiga;
import batalha.contratos.Combatente;
import batalha.contratos.IAInimigo;
import batalha.contratos.TipoAtaque;

/** STUB da IA: o inimigo sempre usa o ataque físico "golpe". */
public class IASimulada implements IAInimigo {

    private int chamadas = 0;

    @Override
    public AcaoInimiga escolherAcao(Combatente inimigo, Combatente jogador) {
        chamadas++;
        return new AcaoInimiga(TipoAtaque.FISICO, "golpe");
    }

    public int getChamadas() { return chamadas; }
}
